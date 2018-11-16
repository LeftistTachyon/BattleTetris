package roomserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * A class that handles communication with clients
 * @author Jed Wang
 */
public class ClientCommunication {
    /**
     * The port to communicate over
     */
    public static final int PORT = 9001;
    
    /**
     * A map of all names of clients paired to their respective Handlers.
     */
    private final static HashMap<String, Handler> HANDLERS = new HashMap<>();
    
    /**
     * A Set of Handlers which are busy
     */
    private static Set<Handler> busy = new HashSet<>();
    
    /**
     * The ServerUI for this admin.
     */
    private static ServerUI serverUI;
    
    /**
     * Starts the serverUI.
     */
    public static void startServerUI() {
        serverUI = new ServerUI();
        serverUI.setVisible(true); /*Stopgap*/
    }
    
    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for a dealing with a single client
     * and broadcasting its messages.
     */
    public static class Handler extends Thread implements Comparable<Handler> {
        /**
         * This client's name
         */
        private String name;
        
        /**
         * This client's socket
         */
        public final Socket socket;
        
        /**
         * The opponent's handler
         */
        private Handler opponent;
        
        /**
         * Messaging to here
         */
        private BufferedReader in;
        
        /**
         * Message from here
         */
        private PrintWriter out;
        
        /**
         * Whether this client is in a game
         */
        private boolean inGame;
        
        /**
         * The beginning bags
         */
        private String loadedBagThis = null, loadedBagOpp = null;
        
        /**
         * The lock object used for synchronizing bag creation.
         */
        private static final Object BAG_LOCK = new Object();
        
        /**
         * A counter that lets games start together.
         */
        private int startCntr;
        
        /**
         * Constructs a handler thread, squirreling away the socket.
         * All the interesting work is done in the run method.
         * @param socket the socket that receives info from the client
         */
        public Handler(Socket socket) {
            this.socket = socket;
            inGame = false;
            opponent = null;
            startCntr = 0;
        }

        /**
         * Services this thread's client by repeatedly requesting a
         * screen name until a unique one has been submitted, then
         * acknowledges the name and registers the output stream for
         * the client in a global set, then repeatedly gets inputs and
         * broadcasts them.
         */
        @Override
        public void run() {
            try {
                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Request a name from this client.  Keep requesting until
                // a name is submitted that is not already used.  Note that
                // checking for the existence of a name and adding the name
                // must be done while locking the set of names.
                while(true) {
                    out.println("SUBMITNAME");
                    // notify("SUBMITNAME", false);
                    name = in.readLine();
                    // notify(name, true);
                    if(name == null) return;
                    if("".equals(name) || "null".equals(name)) continue;
                    synchronized(HANDLERS) {
                        if(!HANDLERS.containsKey(name)) {
                            HashSet<String> copy = new HashSet<>(HANDLERS.keySet());
                            for(Handler h : HANDLERS.values()) {
                                h.out.println("NEWCLIENTtrue " + name);
                            }
                            HANDLERS.put(name, this);
                            serverUI.addPlayer(name);
                            for(String s : copy) {
                                out.println("NEWCLIENTfalse " + s);
                            }
                            break;
                        }
                    }
                }
                
                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                // notify("NAMEACCEPTED", false);

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while(true) {
                    String line = in.readLine();
                    // notify(line, true);
                    if(line == null) {
                        return;
                    }
                    
                    println("\"" + line + "\"");
                    
                    // handle input
                    if(line.equals("PING")) {
                        out.println("PING");
                    } else if(line.startsWith("NLM")) {
                        String message = "NLM" + name + ": " + line.substring(3);
                        for(Handler h : HANDLERS.values()) {
                            h.out.println(message);
                        }
                    } else if(inGame) {
                        if(line.equals("EXIT")) {
                            inGame = false;
                            busy.remove(this);
                            for(Handler h : HANDLERS.values()) {
                                h.out.println("FREE" + name);
                            }
                            startCntr = 0;
                            if(opponent != null) {
                                opponent.out.println("EXIT");
                                opponent.inGame = false;
                                busy.remove(opponent);
                                for(Handler h : HANDLERS.values()) {
                                    h.out.println("FREE" + opponent.name);
                                }
                                
                                opponent.startCntr = 0;
                                opponent = null;
                            }
                        } else if(line.equals("SB")) {
                            synchronized(BAG_LOCK) {
                                startCntr++;
                                opponent.startCntr++;
                                if(startCntr != opponent.startCntr) 
                                    throw new IllegalStateException(
                                            "The startCntrs between two "
                                                    + "opponents are different.");
                                if(loadedBagOpp == null || loadedBagThis == null) {
                                    String thisBag = newBag(), thatBag = newBag();
                                    String toSend = "SB" + thisBag + " " + thatBag;
                                    out.println(toSend);
                                    System.out.print("SERVER to ");
                                    println(toSend);
                                    opponent.loadedBagOpp = thisBag;
                                    opponent.loadedBagThis = thatBag;
                                } else {
                                    String toSend = "SB" + loadedBagThis + 
                                            " " + loadedBagOpp;
                                    out.println(toSend);
                                    System.out.print("SERVER to ");
                                    println(toSend);
                                    loadedBagThis = null;
                                    loadedBagOpp = null;
                                }
                                if(startCntr == 2) {
                                    out.println("ST");
                                    opponent.out.println("ST");
                                }
                            }
                        } else opponent.out.println(line);
                    } else {
                        if(line.startsWith("CHALLENGE_C")) {
                            // Challenging for a match
                            String toChallenge = line.substring(11);
                            if(toChallenge.equals(name))
                                continue;
                            
                            if(HANDLERS.containsKey(toChallenge)) {
                                HANDLERS.get(toChallenge).
                                        out.println("CHALLENGE_C" + name);
                            } else System.err.println("Opponent " + toChallenge 
                                    + " not found (149)");
                        } else if(line.startsWith("CHALLENGE_R")) {
                            // Challenge response: accept or reject
                            Scanner temp = new Scanner(line.substring(11));
                            
                            // Accepted!
                            String other = temp.next();
                            if(HANDLERS.containsKey(other)) {
                                Handler otherH = HANDLERS.get(other);
                                if(temp.nextBoolean() && 
                                        !busy.contains(otherH)) {
                                    opponent = otherH;
                                    inGame = true;
                                    opponent.out.println("CHALLENGE_Rtrue " + name);
                                    opponent.opponent = this;
                                    opponent.inGame = true;
                                    for(Handler h : HANDLERS.values()) {
                                        h.out.println("BUSY" + name);
                                        h.out.println("BUSY" + opponent.name);
                                    }
                                    
                                    busy.add(opponent);
                                    busy.add(this);
                                } else {
                                    otherH.out.println("CHALLENGE_Rfalse " + name);
                                }
                            } else System.err.println("Opponent " + other + 
                                    " not found (168)");
                        }
                    }
                }
            } catch(IOException e) {
                println(e.toString());
            } finally {
                // This client is going down!  Remove its name and its print
                // writer from the sets, and close its socket.
                for(Handler h : HANDLERS.values()) {
                    h.out.println("REMOVECLIENT" + name);
                }
                if(opponent != null) {
                    opponent.out.println("EXIT");
                    opponent.inGame = false;
                    for(Handler h : HANDLERS.values()) {
                        h.out.println("FREE" + opponent.name);
                    }
                }
                if(HANDLERS != null) {
                    HANDLERS.remove(name);
                    serverUI.removePlayer(name);
                }
                out.close();
                try {
                    in.close();
                    socket.close();
                } catch(IOException e) {}
            }
        }
        
        /**
         * Generates a new bag of tetrominos
         * @return the order of the bag
         */
        private static String newBag() {
            ArrayList<String> all = new ArrayList<>(Arrays.asList(
                    new String[]{"T", "S", "L", "Z", "J", "I", "O"}));
            String output = "";
            while(!all.isEmpty()) {
                int r = (int) (Math.random() * all.size());
                output += all.remove(r);
            }
            return output;
        }
 
        @Override
        public String toString() {
            return name;
        }
        
        /**
         * Prints something with a carriage return afterwards
         * @param s a string to println
         */
        public void println(String s) {
            System.out.println(name + ": " + s);
        }

        @Override
        public int compareTo(Handler h) {
            return name.compareTo(h.name);
        }
        
        /**
         * Returns the client's name
         * @return the client's name 
         */
        public String getClientName() {
            return name;
        }
    }
    
    /**
     * Processes a command from the admin.
     * @param command the command to process; 
     * in the format <code>[body] [stuff]...</code>.
     * @return the message to print out for the admin's console
     */
    public static String processCommand(String command) {
        return "";
    }
    
    /**
     * Sends a message to all clients.
     * The "user" which "sends" the message is [ADMIN].
     * @param message the message to send
     */
    public static void distributeMessage(String message) {
        for(Handler h : HANDLERS.values()) {
            h.out.println(message);
        }
    }
}