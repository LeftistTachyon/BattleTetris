package roomclient;

import java.util.Scanner;

/**
 * The main class for this client
 * @author Jed Wang
 */
public class RoomClientMain {
    /**
     * The main method.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ServerCommunication sc = new ServerCommunication();
        Scanner input = new Scanner(System.in);
        while(true) {
            String line = input.nextLine();
            switch(line) {
                case "EXIT":
                    sc.exitGame();
                    break;
                case "END":
                    System.exit(0);
                    break;
                default:
                    sc.out.println(line);
                    break;
            }
        }
    }
}