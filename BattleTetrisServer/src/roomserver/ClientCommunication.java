package roomserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

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
    private static final Set<Handler> BUSY = new HashSet<>();
    
    /**
     * The ServerUI for this admin.
     */
    private static ServerUI serverUI;
    
    /**
     * A Set of banned names.
     */
    private static final HashMap<String, String> BANNED_NAMES = new HashMap<>();
    
    /**
     * A Set of banned IP addresses.
     */
    private static final HashMap<InetAddress, String> BANNED_IPS = new HashMap<>();
    
    /**
     * The maximum amount of players allowed on the server.
     * {@code -1} means any amount is fine.
     */
    private static int max_players = -1;
    
    /**
     * Starts the serverUI.
     */
    public static void startServerUI() {
        serverUI = ServerUI.run();
    }
    
    /**
     * No instantiation for you!
     */
    private ClientCommunication() {}
    
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
                
                // Check if this IP is banned
                InetAddress thisAdd = socket.getInetAddress();
                if(BANNED_IPS.containsKey(thisAdd)) {
                    String reason = BANNED_IPS.get(thisAdd);
                    if(reason == null) {
                        out.println("BAN");
                    } else {
                        out.println("BAN" + reason);
                    }
                    close();
                    return;
                }

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
                
                // Check if this name/user is banned
                if(BANNED_NAMES.containsKey(name)) {
                    String reason = BANNED_NAMES.get(name);
                    if(reason == null) {
                        out.println("BAN");
                    } else {
                        out.println("BAN" + reason);
                    }
                    close();
                    return;
                }
                
                // Now that a successful name has been chosen, add the
                // socket's print writer to the set of all writers so
                // this client can receive broadcast messages.
                out.println("NAMEACCEPTED");
                // notify("NAMEACCEPTED", false);

                // Accept messages from this client and broadcast them.
                // Ignore other clients that cannot be broadcasted to.
                while(true) {
                    String line = null;
                    try {
                        line = in.readLine();
                    } catch (SocketException se) {
                        serverUI.chatOut.println("<span style=\"color:blue;\">"+
                                name + " disconnected.</span>");
                    }
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
                        serverUI.chatOut.println(message.substring(3));
                    } else if(inGame) {
                        if(line.equals("EXIT")) {
                            inGame = false;
                            BUSY.remove(this);
                            for(Handler h : HANDLERS.values()) {
                                h.out.println("FREE" + name);
                            }
                            startCntr = 0;
                            if(opponent != null) {
                                opponent.out.println("EXIT");
                                opponent.inGame = false;
                                BUSY.remove(opponent);
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
                                        !BUSY.contains(otherH)) {
                                    opponent = otherH;
                                    inGame = true;
                                    opponent.out.println("CHALLENGE_Rtrue " + name);
                                    opponent.opponent = this;
                                    opponent.inGame = true;
                                    for(Handler h : HANDLERS.values()) {
                                        h.out.println("BUSY" + name);
                                        h.out.println("BUSY" + opponent.name);
                                    }
                                    
                                    BUSY.add(opponent);
                                    BUSY.add(this);
                                } else {
                                    otherH.out.println("CHALLENGE_Rfalse " + name);
                                }
                            } else System.err.println("Opponent " + other + 
                                    " not found (168)");
                        }
                    }
                }
            } catch(IOException e) {
                serverUI.printStackTrace(e);
            } finally {
                try {
                    // This client is going down!  Remove its name and its print
                    // writer from the sets, and close its socket.
                    close();
                } catch (IOException ex) {
                    ex.printStackTrace(serverUI.chatOut);
                }
            }
        }
        
        /**
         * Fully disposes this Handler
         * @throws IOException if something goes wrong
         */
        private void close() throws IOException {
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
            in.close();
            socket.close();
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
     * The String that is displayed when an unknown command is entered.
     */
    private static final String UNKNOWN_COMMAND = 
            "<span style=\"color:red;\">Unknown command. Try /help for a list "
            + "of commands.</span>";
    
    /**
     * The String that is displayed when a method is called with an 
     * incorrect amount of arguments.<br>
     * e.g. {@code nothing()} is called when the method signature is 
     * <code>public void nothing(int i){...}</code>
     */
    private static final String BAD_METHOD_CALL_1 = "<span style=\"color:red;\">"
            + "Bad command: actual and formal arguments differ in length.</span>";
    
    /**
     * The String that is displayed when a method is called with an incorrect
     * argument type.<br>
     * e.g. {@code nothing("Help")} is called when the method signature is 
     * <code>public void nothing(int i){...}</code>
     */
    private static final String BAD_METHOD_CALL_2 = "<span style=\"color:red;\">" 
            + "Bad command: incorrect argument type.</span>";
    
    /**
     * The String that is displayed when a method is called with a wrong 
     * value.<br>
     * In essence, this is a {@code IllegalArgumentException}.
     */
    private static final String BAD_METHOD_CALL_3 = "<span style=\"color:red;\">"
            + "Bad command: unexpected argument value.</span>";
    
    /**
     * The String that is displayed when the admin attempts to ban a 
     * user that has already been placed on the blacklist. 
     */
    private static final String ALREADY_BANNED_U = "<span style=\"color:red;\">"
            + "That user has already been banned.</span>";
    
    /**
     * The String that is displayed when the admin attempts to ban a 
     * name that has already been placed on the blacklist. 
     */
    private static final String ALREADY_BANNED_N = "<span style=\"color:red;\">"
            + "That name has already been banned.</span>";
    
    /**
     * The String that is displayed when the admin attempts to ban an 
     * IP address that has already been placed on the blacklist. 
     */
    private static final String ALREADY_BANNED_IP = "<span style=\"color:red;\">"
            + "That IP address has already been banned.</span>";
    
    /**
     * The String that is displayed when the admin attempts to remove 
     * a user/name from the blacklist that isn't on the blacklist.
     */
    private static final String NOT_BANNED_NU = "<span style=\"color:red;\">"
            + "That user/name is not on the blacklist.</span>";
    
    /**
     * The String that is displayed when the admin attempts to remove 
     * an IP address from the blacklist that isn't on the blacklist.
     */
    private static final String NOT_BANNED_IP = "<span style=\"color:red;\">"
            + "That IP address is not on the blacklist.</span>";
    
    /**
     * The String that is displayed when an invalid IP address is entered.
     */
    private static final String INVALID_IP = "<span style=\"color:red;\">"
            + "Invalid IP address.</span>";
    
    /**
     * The String that is displayed when a username that doesn't match 
     * any player is used as an argument to a method.
     */
    private static final String UNKNOWN_PLAYER = "<span style=\"color:red;\">"
            + "Unknown player: no player with that name is connected.</span>";
    
    /**
     * A TreeMap that stores all method stubs.
     */
    private static final TreeMap<String, String> COMMAND_TXT = new TreeMap<>();
    
    static {
        COMMAND_TXT.put("help", "/help [<i>page</i>|<i>command</i>]");
        COMMAND_TXT.put("?", "/? [<i>page</i>|<i>command</i>]");
        COMMAND_TXT.put("kick", "/kick &lt;<i>player</i>> [<i>reason</i>...]");
        COMMAND_TXT.put("ban", "/ban &lt;<i>player</i>> [<i>reason</i>...]");
        COMMAND_TXT.put("ban-ip", 
                "/ban-ip &lt;<i>address</i>|<i>player</i>> [<i>reason</i>...]");
        COMMAND_TXT.put("w", "/w &lt;<i>player</i>> &lt;<i>message</i>...>");
        COMMAND_TXT.put("tell", "/tell &lt;<i>player</i>> &lt;<i>message</i>...>");
        COMMAND_TXT.put("msg", "/msg &lt;<i>player</i>> &lt;<i>message</i>...>");
        COMMAND_TXT.put("banlist", "/banlist &lt;ips|players>");
        COMMAND_TXT.put("pardon", "/pardon &lt;<i>player</i>>");
        COMMAND_TXT.put("pardon-ip", "/pardon-ip &lt;<i>address</i>>");
        COMMAND_TXT.put("stop", "/stop");
        COMMAND_TXT.put("list", "/list");
        COMMAND_TXT.put("setmaxplayers", "/setmaxplayers &lt;<i>maxPlayers: int</i>|clear>");
    }
    
    /**
     * Processes a command from the admin.
     * @param command the command to process; 
     * in the format <code>[body] [stuff]...</code>.
     * @return the message to print out for the admin's console
     */
    public static String processCommand(String command) {
        String[] data = command.split(" ");
        switch(data[0]) {
            case "help":
            case "?":
                int page;
                if(data.length == 2) {
                    try {
                        page = Integer.parseInt(data[1]);
                    } catch (NumberFormatException nfe) {
                        page = -1;
                    }
                } else {
                    page = 1;
                }
                if(page == -1) {
                    return getHelp(data[1]);
                } else {
                    return getHelpPage(page);
                }
            case "kick":
                if(data.length == 2) {
                    String player2 = data[1];
                    if(!HANDLERS.containsKey(player2)) {
                        return UNKNOWN_PLAYER;
                    }
                    Handler toSend = HANDLERS.get(player2);
                    toSend.out.println("KICK");
                    return "<span style=\"color:green;\">Successfully kicked " 
                            + player2 + ".</span>";
                } else if(data.length > 2) {
                    String player = data[1], reason = "";
                    if(!HANDLERS.containsKey(player)) {
                        return UNKNOWN_PLAYER;
                    }
                    for(int i = 2; i < data.length; i++) {
                        reason += data[i] + " ";
                    }
                    Handler toSend = HANDLERS.get(player);
                    toSend.out.println("KICK" + reason.trim());
                    return "<span style=\"color:green;\">Successfully kicked " 
                            + player + ".</span>";
                } else return BAD_METHOD_CALL_1;
            case "ban":
                if(data.length == 2) {
                    String player2 = data[1];
                    if(!HANDLERS.containsKey(player2)) {
                        if(BANNED_NAMES.containsKey(player2)) {
                            return ALREADY_BANNED_N;
                        } else {
                            BANNED_NAMES.put(player2, null);
                            return "<span style=\"color:blue;\">Successfully added "
                                + player2 +" to the name blacklist.</span>";
                        }
                    }
                    Handler toSend = HANDLERS.get(player2);
                    toSend.out.println("BAN");
                    if(BANNED_NAMES.containsKey(player2)) {
                        return ALREADY_BANNED_U;
                    } else {
                        return "<span style=\"color:green;\">Successfully added "
                            + player2 +" to the user blacklist.</span>";
                    }
                } else if(data.length > 2) {
                    String player = data[1], reason = "";
                    if(!HANDLERS.containsKey(player)) {
                        if(BANNED_NAMES.containsKey(player)) {
                            return ALREADY_BANNED_N;
                        } else {
                            BANNED_NAMES.put(player, reason);
                            return "<span style=\"color:blue;\">Successfully added "
                                + player +" to the name blacklist.</span>";
                        }
                    }
                    for(int i = 2; i < data.length; i++) {
                        reason += data[i] + " ";
                    }
                    Handler toSend = HANDLERS.get(player);
                    toSend.out.println("BAN" + reason);
                    if(BANNED_NAMES.containsKey(player)) {
                        return ALREADY_BANNED_U;
                    } else {
                        BANNED_NAMES.put(player, reason);
                        return "<span style=\"color:green;\">Successfully added "
                            + player +" to the user blacklist.</span>";
                    }
                } else return BAD_METHOD_CALL_1;
            case "ban-ip":
                if(data.length == 2) {
                    String thing2 = data[1];
                    if (HANDLERS.containsKey(thing2)) {
                        Handler toBan = HANDLERS.get(thing2);
                        toBan.out.println("BAN");
                        InetAddress ia2 = toBan.socket.getInetAddress();
                        if(BANNED_IPS.containsKey(ia2)) {
                            return ALREADY_BANNED_IP;
                        } else {
                            BANNED_IPS.put(ia2, null);
                            return "<span style=\"color:green;\">Successfully added " 
                                + ia2.getHostAddress() + " to the IP blacklist.</span>";
                        }
                    } else {
                        try {
                            InetAddress ia = InetAddress.getByName(thing2);
                            String ip2 = ia.getHostAddress();
                            Handler toBan = null;
                            for(Handler h : HANDLERS.values()) {
                                if(ia.equals(h.socket.getInetAddress())) {
                                    toBan = h;
                                    break;
                                }
                            }
                            if(toBan == null) {
                                if(BANNED_IPS.containsKey(ia)) {
                                    return ALREADY_BANNED_IP;
                                } else {
                                    BANNED_IPS.put(ia, null);
                                    return "<span style=\"color:blue;\">Successfully added " 
                                        + ip2 + " to the IP blacklist.</span>";
                                }
                            } else {
                                toBan.out.println("BAN");
                                if(BANNED_IPS.containsKey(ia)) {
                                    return ALREADY_BANNED_IP;
                                } else {
                                    BANNED_IPS.put(ia, null);
                                    return "<span style=\"color:green;\">Successfully added " 
                                        + ip2 + " to the IP blacklist.</span>";
                                }
                            }
                        } catch (UnknownHostException ex) {
                            return INVALID_IP;
                        }
                    }
                } else if(data.length > 2) {
                    String thing = data[1], reason = "";
                    for(int i = 2; i < data.length; i++) {
                        reason += data[i] + " ";
                    }
                    if (HANDLERS.containsKey(thing)) {
                        Handler toBan = HANDLERS.get(thing);
                        toBan.out.println("BAN" + reason);
                        InetAddress ia2 = toBan.socket.getInetAddress();
                        if(BANNED_IPS.containsKey(ia2)) {
                            return ALREADY_BANNED_IP;
                        } else {
                            BANNED_IPS.put(ia2, reason);
                            return "<span style=\"color:green;\">Successfully added " 
                                + ia2.getHostAddress() + " to the IP blacklist.</span>";
                        }
                    } else {
                        try {
                            InetAddress ia = InetAddress.getByName(thing);
                            String ip2 = ia.getHostAddress();
                            Handler toBan = null;
                            for(Handler h : HANDLERS.values()) {
                                if(ip2.equals(h.socket.getInetAddress().getHostAddress())) {
                                    break;
                                }
                            }
                            if(toBan == null) {
                                if(BANNED_IPS.containsKey(ia)) {
                                    return ALREADY_BANNED_IP;
                                } else {
                                    BANNED_IPS.put(ia, reason);
                                    return "<span style=\"color:blue;\">Successfully added " 
                                        + ia.getHostAddress() + " to the IP blacklist.</span>";
                                }
                            } else {
                                toBan.out.println("BAN" + reason);
                                if(BANNED_IPS.containsKey(ia)) {
                                    return ALREADY_BANNED_IP;
                                } else {
                                    BANNED_IPS.put(ia, reason);
                                    return "<span style=\"color:green;\">Successfully added " 
                                        + ia.getHostAddress() + " to the IP blacklist.</span>";
                                }
                            }
                        } catch (UnknownHostException ex) {
                            return INVALID_IP;
                        }
                    }
                } else return BAD_METHOD_CALL_1;
            case "banlist":
                if(data.length == 2) {
                    switch(data[1]) {
                        case "ips":
                            String outputIP = "<span style=\"color:green;\">IP blacklist:</span><br>";
                            for(InetAddress inA : BANNED_IPS.keySet()) {
                                outputIP += inA.getHostAddress() + "<br>";
                            }
                            return outputIP;
                        case "players":
                            String outputP = "<span style=\"color:green;\">Player blacklist:</span><br>";
                            for(String s : BANNED_NAMES.keySet()) {
                                outputP += s + "<br>";
                            }
                            return outputP;
                        default:
                            return BAD_METHOD_CALL_3;
                    }
                } else return BAD_METHOD_CALL_1;
            case "w":
            case "tell":
            case "msg":
                if(data.length > 2) {
                    String player = data[1]; 
                    if(!HANDLERS.containsKey(player)) {
                        return UNKNOWN_PLAYER;
                    }
                    String message = "";
                    for(int i = 2; i < data.length; i++) {
                        message += data[i] + " ";
                    }
                    Handler toSend = HANDLERS.get(player);
                    toSend.out.println("NLM[ADMIN] whispered to you: " + message);
                    return "You whispered to " + player + ": " + message;
                } else return BAD_METHOD_CALL_1;
            case "pardon":
                if(data.length == 2) {
                    String player = data[1];
                    if(BANNED_NAMES.containsKey(player)) {
                        BANNED_NAMES.remove(player);
                        return "<span style=\"color:green;\">Successfully removed "
                            + player + " from the name/player blacklist.</span>";
                    } else return NOT_BANNED_NU;
                } else return BAD_METHOD_CALL_1;
            case "pardon-ip":
                if(data.length == 2) {
                    try {
                        String ip = data[1];
                        InetAddress current = InetAddress.getByName(ip);
                        if (BANNED_IPS.containsKey(current)) {
                            BANNED_IPS.remove(current);
                            return "<span style=\"color:green;\">Successfully removed "
                                    + current.getHostAddress() + " from the IP blacklist.</span>";
                        } else {
                            return NOT_BANNED_IP;
                        }
                    } catch (UnknownHostException uhe) {
                        return INVALID_IP;
                    }
                } else return BAD_METHOD_CALL_1;
            case "stop":
                for(Handler h : HANDLERS.values()) {
                    try {
                        h.close();
                    } catch (IOException ex) {
                        serverUI.printStackTrace(ex);
                    }
                }
                System.exit(0);
                break;
            case "list":
                if(HANDLERS.isEmpty())
                    return "<span style=\"color:green;\">There are no players connected.</span>";
                String output = "<span style=\"color:green;\">All connected players:</span><br>";
                for(String name : HANDLERS.keySet()) {
                    output += name + "<br>";
                }
                return output;
            case "setmaxplayers":
                if(data.length == 2) {
                    String num = data[1];
                    try {
                        int max = Integer.parseInt(num);
                        int current = HANDLERS.size();
                        if(max < current) 
                            max = current;
                        if(max < 1) 
                            max = 1;
                        
                        max_players = max;
                        
                        return "<span style=\"color:green;\">Successfully set maximum number of players to "
                                + max + ".</span>";
                    } catch (NumberFormatException nfe) {
                        if(num.equals("clear")) {
                            max_players = -1;
                            return "<span style=\"color:green;\">Successfully cleared room size limit.</span>";
                        } else return BAD_METHOD_CALL_3;
                    }
                } else return BAD_METHOD_CALL_1;
            default:
                return UNKNOWN_COMMAND;
        }
        try {
            throw new Exception("How did we get here?");
        } catch (Exception e) {
            serverUI.printStackTrace(e);
        }
        return null;
    }
    
    /**
     * Returns the help for a command
     * @param command the command to look up help for
     * @return the help documentation
     */
    private static String getHelp(String command) {
        String output = "<span style=\"color:blue;\">" + 
                COMMAND_TXT.get(command) + "</span><br>";
        switch(command) {
            case "help":
            case "?":
                output += "Provides help/list of commands.";
                break;
            case "kick":
                output += "Kicks a player off a server.";
                break;
            case "ban":
                output += "Adds a player to the blacklist.";
                break;
            case "ban-ip":
                output += "Adds an IP to the blacklist.";
                break;
            case "banlist":
                output += "Displays the server\'s blacklist";
                break;
            case "w":
            case "tell":
            case "msg":
                output += "Sends a private message to one or more players.";
                break;
            case "pardon":
                output += "Removes a player from the blacklist.";
                break;
            case "pardon-ip":
                output += "Removes an IP from the blacklist.";
                break;
            case "stop":
                output += "Stops the server.";
                break;
            case "list":
                output += "Lists all players on the server.";
                break;
            case "setmaxplayers":
                output += "Sets the maximum number of players allowed to join.";
                break;
            default:
                return UNKNOWN_COMMAND;
        }
        return output;
    }
    
    /**
     * Returns the help page of that number
     * @param page the page number
     * @return the help page
     */
    private static String getHelpPage(int page) {
        int start = page*5-5, size = COMMAND_TXT.size();
        if(start >= size || start < 0)
            return "<span style=\"color:red;\">There is no page " + page + 
                    ".</span>";
        String output = "<span style = \"color:green;\">--- Showing help page " 
                + page + " of " + ((int) Math.ceil(size/5.0)) + 
                " (/help &lt;page>) ---</span><br>";
        for(int i = start; i < start+5; i++) {
            if(i >= size) break;
            String key = (String) COMMAND_TXT.keySet().toArray()[i];
            output += COMMAND_TXT.get(key);
            if(i != start + 4 && i != size - 1) output += "<br>";
        }
        return output;
    }
    
    /**
     * Sends a message to all clients.
     * The "user" which "sends" the message is [ADMIN].
     * @param message the message to send
     */
    public static void distributeMessage(String message) {
        for(Handler h : HANDLERS.values()) {
            h.out.println("NLM[ADMIN]: " + message);
        }
    }
}