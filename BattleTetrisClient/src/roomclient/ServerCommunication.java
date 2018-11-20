package roomclient;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.concurrent.ScheduledService;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import simpletetris.AudioPlayer;
import simpletetris.TetrisBag;
import simpletetris.TetrisFrame;
import simpletetris.TetrisKeyAdapter;

/**
 * A client to server communication
 * @author Jed Wang
 */
public class ServerCommunication {
    /**
     * The socket connection into this
     */
    private BufferedReader in;
    
    /**
     * The socket connection out of this
     * Should be private
     */
    protected PrintWriter out;
    
    /**
     * Whether this client is in a game
     */
    private boolean inGame;
    
    /**
     * The LobbyWindow for this client
     */
    private LobbyWindow lw;
    
    /**
     * The status of all players: (Name, Whether this client is busy)
     */
    private HashMap<String, Boolean> status;
    
    /**
     * My opponent's name.
     */
    private static String opponentName = null;
    
    /**
     * My name.
     */
    private static String myName = null;
    
    /**
     * Standard constructor.
     */
    public ServerCommunication() {
        inGame = false;
        status = new HashMap<>();
        
        lw = LobbyWindow.run(this);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        lw.setLocation((screenSize.width - lw.getWidth())/2, 
                (screenSize.height - lw.getHeight())/2);
        new Thread() {
            @Override
            public void run() {
                try {
                    run_();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }.start();
    }
    
    /**
     * Connects to the server then enters the processing loop
     * @throws IOException if something goes wrong
     */
    private void run_() throws IOException {
        // Make connection and initialize streams
        String serverAddress;
        Socket socket = null;
        do {
            serverAddress = getServerAddress();
            try {
                socket = new Socket(serverAddress, 9001);
            } catch (ConnectException | NoRouteToHostException | UnknownHostException ex) {
                Object[] options = {"Reenter IP Adress", "Exit"};
                int returned = JOptionPane.showOptionDialog(lw, ex.getMessage(), 
                        "Connection Error", JOptionPane.OK_CANCEL_OPTION, 
                        JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                if(returned != JOptionPane.OK_OPTION) {
                    lw.dispose();
                    System.exit(0);
                    return;
                }
            }
        } while(socket == null);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        TetrisFrame tFrame = null;
        
        // Process all messages from server, according to the protocol.
        
        int temp = 0;
        String _name;
        while(true) {
            // Reading input from the server
            String line = null;
            try {
                line = in.readLine();
            } catch (SocketException se) {
                JOptionPane.showMessageDialog(lw, 
                        "You have been disconnected from the server.", 
                        "Disconnected", JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
            if(line == null) {
                // Welp, looks like the server left
                return;
            }
            
            if(line.startsWith("NEWCLIENT")) {
                // add a client to the pool
                String[] data = line.substring(9).split(" ");
                
                String newClient = data[1];
                lw.addPlayer(newClient);
                status.put(newClient, false);
                
                if(Boolean.parseBoolean(data[0])) {
                    lw.addLobbyMessage(newClient + " has joined");
                }
            } else if(line.startsWith("REMOVECLIENT")) {
                // remove a client from the pool
                String toRemove = line.substring(12);
                lw.removePlayer(toRemove);
                status.remove(toRemove);
                lw.addLobbyMessage(toRemove + " has left");
            } else if(line.startsWith("BUSY")) {
                status.put(line.substring(4), true);
            } else if(line.startsWith("FREE")) {
                status.put(line.substring(4), false);
            } else if(line.startsWith("NLM")) {
                lw.addLobbyMessage(line.substring(3));
            } else if(line.startsWith("KICK")) {
                String reason = line.substring(4);
                JFrame active = (tFrame == null)?lw:tFrame;
                active.requestFocus();
                Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    System.exit(0);
                }, 5000, TimeUnit.MILLISECONDS);
                if("".equals(reason)) {
                    // no reason
                    JOptionPane.showMessageDialog(active, 
                            "You\'ve been kicked from the server.", 
                            "You\'ve been kicked", JOptionPane.ERROR_MESSAGE);
                } else {
                    // reason
                    JOptionPane.showMessageDialog(active, 
                            "You\'ve been kicked from the server.\n"
                                    + "Reason: " + reason, 
                            "You\'ve been kicked", JOptionPane.ERROR_MESSAGE);
                }
                System.exit(0);
            } else if(line.startsWith("BAN")) {
                String reason = line.substring(3);
                JFrame active = (tFrame == null)?lw:tFrame;
                active.requestFocus();
                Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    System.exit(0);
                }, 5000, TimeUnit.MILLISECONDS);
                if("".equals(reason)) {
                    // no reason
                    JOptionPane.showMessageDialog(active, 
                            "You\'ve been banned from the server.", 
                            "You\'ve been banned", JOptionPane.ERROR_MESSAGE);
                } else {
                    // reason
                    JOptionPane.showMessageDialog(active, 
                            "You\'ve been banned from the server.\n"
                                    + "Reason: " + reason, 
                            "You\'ve been banned", JOptionPane.ERROR_MESSAGE);
                }
                System.exit(0);
            } else {
                if(inGame) {
                    if(line.equals("EXIT")) {
                        System.err.println("The other person has left "
                                + "the match.");
                        inGame = false;
                        if(tFrame != null) {
                            JOptionPane.showMessageDialog(tFrame, 
                                    "The other person has left the match.", 
                                    "Disconnected", JOptionPane.PLAIN_MESSAGE);
                            tFrame.dispose();
                            tFrame = null;
                        }
                    } else if(line.equals("ST")) {
                        Dimension ss = 
                                Toolkit.getDefaultToolkit().getScreenSize();
                        tFrame = new TetrisFrame();
                        tFrame.setLocation(
                                (ss.width - tFrame.getWidth()) / 2, 
                                (ss.height - tFrame.getHeight()) / 2);
                        tFrame.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosed(WindowEvent e) {
                                System.err.println("Closin\'!");
                                super.windowClosing(e);
                                inGame = false;
                                AudioPlayer.stopBackgroundMusic();
                                ((TetrisFrame)e.getWindow()).terminate();
                                out.println("EXIT");
                            }
                        });
                        tFrame.setActionListener((ActionEvent e) -> {
                            out.println(e.getActionCommand());
                        });
                    } else {
                        if(line.startsWith("NB")) {
                            if(tFrame != null)
                                tFrame.opponent.addBag(line.substring(2));
                        } else if(line.startsWith("LOCK")) {
                            if(tFrame != null) {
                                String[] data = line.substring(4).split(" ");
                                tFrame.opponent.lockFalling(
                                        Integer.parseInt(data[0]), 
                                        Integer.parseInt(data[1]));
                            }
                        } else if(line.startsWith("M")) {
                            if(tFrame != null)
                                tFrame.opponent.executeAction(TetrisKeyAdapter.
                                        GameAction.fromShorthand(line.substring(1)));
                        } else if(line.startsWith("SB")) {
                            String[] bags = line.substring(2).split(" ");
                            TetrisBag.RAM_BAG_THIS = bags[0];
                            TetrisBag.RAM_BAG_THAT = bags[1];
                        } else if(line.startsWith("GL")) {
                            if(tFrame != null)
                                tFrame.opponent.dumpGarbage(line.substring(2));
                        } else if(line.startsWith("GC")) {
                            if(tFrame != null) {
                                switch(line.substring(2)) {
                                    case "P":
                                        tFrame.opponent.pauseGravity();
                                        break;
                                    case "R":
                                        tFrame.opponent.resumeGravity();
                                        break;
                                }
                            }
                        }
                    }
                } else {
                    if(line.startsWith("SUBMITNAME")) {
                        // submit your name, duh
                        _name = getName(temp++ == 0);
                        out.println(_name);
                        System.out.println(_name);
                        myName = _name;
                    } else if(line.startsWith("NAMEACCEPTED")) {
                        // the server has accepted your name
                        temp = 0;
                        // init stuff
                    } else if(line.startsWith("CHALLENGE_C")) {
                        // I'm being challenged!
                        String challenger = line.substring(11);
                        int choice = JOptionPane.showConfirmDialog(lw,
                                challenger + " has challenged you!\nDo you accept?",
                                "Challenge", JOptionPane.YES_NO_OPTION, 
                                JOptionPane.INFORMATION_MESSAGE);
                        // whether I accept the challenge
                        boolean accepted = choice == JOptionPane.YES_OPTION;
                        inGame = accepted;
                        
                        out.println("CHALLENGE_R" + challenger + " " + accepted);
                        if(accepted) {
                            opponentName = challenger;
                            out.println("SB");
                        }
                    } else if(line.startsWith("CHALLENGE_R")) {
                        String[] data = line.substring(11).split(" ");
                        inGame = Boolean.parseBoolean(data[0]);
                        if(inGame) {
                            opponentName = data[1];
                            System.out.println("Opponent: " + opponentName);
                            out.println("SB");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Sends a message to the lobby chat
     * @param toSend the message to send
     */
    public void sendLobbyMessage(String toSend) {
        out.println("NLM" + toSend);
    }
    
    /**
     * Challenges a player
     * @param player the player to challenge
     */
    public void challenge(String player) {
        out.println("CHALLENGE_C" + player);
    }
    
    /**
     * Determines whether a player is busy
     * @param player the player to request
     * @return whether the player is busy
     */
    public Boolean isPlayerBusy(String player) {
        if(status.containsKey(player)) {
            return status.get(player);
        } else return null;
    }
    
    /**
     * Prompt for and return the address of the server.
     */
    private String getServerAddress() {
        return JOptionPane.showInputDialog(
            lw,
            "Enter IP Address of the Server:",
            "Welcome to Socket Room",
            JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Prompt for and return the desired screen name.
     * @param again whether this method needs to state not to enter the same name again
     */
    private String getName(boolean again) {
        String s = null;
        do {
            s = JOptionPane.showInputDialog(
                lw,
                    again?"Choose a screen name (no spaces):":"Choose a different screen name (no spaces):",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
            if(s == null) System.exit(0);
        } while(s.contains(" ") || "".equals(s));
        return s;
    }
    
    /**
     * Exits the current game.
     */
    public void exitGame() {
        out.println("EXIT");
        inGame = false;
    }
    
    /**
     * Returns this client's name.
     * @return this client's name
     */
    public static String getMyName() {
        return myName;
    }

    /**
     * Returns this client's opponent's name.
     * Returns {@code null} if there is no opponent
     * @return this client's opponent's name
     */
    public static String getOpponentName() {
        return opponentName;
    }
}