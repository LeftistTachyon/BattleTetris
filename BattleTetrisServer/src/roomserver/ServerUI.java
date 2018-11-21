package roomserver;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

/**
 * The GUI for the server.
 * @author Jed Wang
 */
public class ServerUI extends JFrame {
    
    /** Creates new form ServerUI */
    public ServerUI() {
        initComponents();
        chatOut = new PrintStream(new ChatOutputStream());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        listSP = new JScrollPane();
        playerList = new JList<>();
        chatSP = new JScrollPane();
        chatPane = new JTextPane();
        textField = new JTextField();
        playerLModel = new DefaultListModel<>();
        chatHist = "";

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Server UI");

        chatPane.setEditable(false);
        chatPane.setFont(new Font("Consolas", Font.PLAIN, 15));
        chatPane.setContentType("text/html");
        chatSP.setViewportView(chatPane);

        textField.setFont(new Font("Segoe UI", 0, 11)); // NOI18N
        textField.addActionListener(this::sendMessage);
        
        playerList.setFont(new Font("Segoe UI", 0, 14)); // NOI18N
        playerList.setModel(playerLModel);
        playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                playerSelected(me);
            }
        });
        listSP.setViewportView(playerList);
        
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(listSP, GroupLayout.PREFERRED_SIZE, 146, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(chatSP)
                    .addComponent(textField, GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(listSP, GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chatSP)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>                        

    /**
     * Activated when the admin presses enter in the text field
     * @param evt the ActionEvent that is generated
     */
    private void sendMessage(ActionEvent evt) {                                            
        String message = textField.getText();
        if(message.equals("")) return;
        textField.setText("");
        if(message.startsWith("/") && !"/".equals(message)) {
            chatHist = chatHist.trim() +  ClientCommunication.processCommand(
                    message.substring(1));
        } else {
            chatHist = chatHist.trim() +  "[ADMIN]: " + message;
            ClientCommunication.distributeMessage(message);
        }
        chatHist = chatHist.trim() +  "<br>";
        updateChat();
    }
    
    /**
     * Activated when the admin double clicks a user on the list
     * @param me the MouseEvent that is generated
     */
    private void playerSelected(MouseEvent me) {
        if(me.getClickCount() == 2) {
            int idx = playerList.locationToIndex(me.getPoint());
            String selected = playerLModel.get(idx);
            if (selected != null) {
                textField.setText(textField.getText() + selected);
            }
        }
    }
    
    /**
     * Updates the chat according to the contents of {@code chatHist}.
     */
    private void updateChat() {
        chatPane.setText("<html><pre>" + chatHist + "</pre></html>");
    }
    
    /**
     * Adds a player to the lobby list
     * @param name the name of the player
     */
    public void addPlayer(String name) {
        int i;
        for(i = 0; i < playerLModel.getSize(); i++) {
            if(name.compareTo(playerLModel.getElementAt(i)) < 0) break;
        }
        playerLModel.add(i, name);
    }
    
    /**
     * Removes a player from the lobby list
     * @param name the name of the player
     */
    public void removePlayer(String name) {
        playerLModel.removeElement(name);
    }
    
    /**
     * Creates and runs a ServerUI.
     * @return the created and shown ServerUI
     */
    public static ServerUI run() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            UIManager.LookAndFeelInfo[] installedLookAndFeels=UIManager.getInstalledLookAndFeels();
            for (UIManager.LookAndFeelInfo installedLookAndFeel : installedLookAndFeels) {
                if ("Nimbus".equals(installedLookAndFeel.getName())) {
                    UIManager.setLookAndFeel(installedLookAndFeel.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | 
                IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        //</editor-fold>

        /* Create and display the form */
        ServerUI output = new ServerUI();
        EventQueue.invokeLater(() -> {
            output.setVisible(true);
        });
        return output;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Variables declaration - do not modify">
    /**
     * A JList that contains all players except for this player.
     */
    private JList<String> playerList;
    
    /**
     * The ListModel for {@code playerList}.
     */
    private DefaultListModel<String> playerLModel;
    
    /**
     * The {@code JScrollPane} that conatins {@code playerList}.
     */
    private JScrollPane listSP;
    
    /**
     * The {@code JScrollPane} that conatins {@code chatPane}.
     */
    private JScrollPane chatSP;
    /**
     * The {@code JTextPane} that conatins the chat history.
     */
    private JTextPane chatPane;
    /**
     * The chat history.
     */
    private String chatHist;
    /**
     * A PrintStream that prints directly into {@code chatPane}.<br>
     * NOTE: use {@code .println()} to print <code>&lt;span></code> tags.
     */
    public final PrintStream chatOut;
    
    /**
     * The {@code JTextField} used for user input.
     */
    private JTextField textField;
    //</editor-fold>
    
    /**
     * An implementation of an OutputStream that writes to the chat 
     * {@code JTextPane}.
     * Shoutouts to <pre>Hovercraft Full of Eels</pre> 
     * for this code!
     */
    public class ChatOutputStream extends OutputStream {
        /**
         * The String that builds the line of text.
         */
        private String s;

        /**
         * Creates a new ChatOutputStream.
         */
        public ChatOutputStream() {
            s = "";
        }

        @Override
        public void flush() throws IOException {
            super.flush();
        }

        @Override
        public void close() throws IOException {
            super.close();
        }
        
        @Override
        public void write(int b) throws IOException {
            if(b == '\r') return;
            
            if(b == '\n') {
                final String toAdd = s + "<br>";
                SwingUtilities.invokeLater(() -> {
                    chatHist = chatHist.trim() +  toAdd;
                    updateChat();
                });
                s = "";
                
                return;
            }
            
            s += ((char)b);
        }
    }
    
    /**
     * Prints out the entire log.
     */
    public void printLog() {
        System.err.println("log:");
        String[] data = chatHist.split(Pattern.quote("<br>"));
        for(String s : data) {
            System.out.println(s);
        }
    }
    
    /**
     * Prints the stack trace of an Exception that was thrown
     * @param e the Exception to print the stack trace of
     */
    public void printStackTrace(Exception e) {
        chatOut.print("<span style=\"color:rgb(128,0,0);\">");
        e.printStackTrace(chatOut);
        chatOut.println("</span>");
    }
}
