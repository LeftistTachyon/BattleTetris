package roomserver;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import roomserver.ClientCommunication.Handler;

/**
 * The main class for this server
 * @author Jed Wang
 */
public class RoomServerMain {
    /**
     * The main method
     * @param args the command line arguments
     * @throws java.io.IOException if something goes wrong
     */
    public static void main(String[] args) throws IOException {
        System.out.println("The Tetris server is running.");
        
        /*MainWindow mw = new MainWindow();
        Handler.setMainWindow(mw);*/
        
        try(ServerSocket listener = new ServerSocket(ClientCommunication.PORT)) {
            while(true) {
                Handler h = new Handler(listener.accept());
                h.start();
                // mw.addHandler(h);
            }
        } catch(BindException be) {
            System.err.println("Cannot start server: " + be.getMessage());
            /*JOptionPane.showMessageDialog(/*mw null, "Cannot start server", 
                    be.getMessage(), JOptionPane.ERROR_MESSAGE);*/
            System.exit(1);
        }
    }
}