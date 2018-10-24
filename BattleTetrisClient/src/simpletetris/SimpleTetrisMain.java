package simpletetris;

import java.awt.event.ActionEvent;

/**
 * The main class for this application.
 * @author Jed Wang, Grace Liu, Danny Tang
 */
public class SimpleTetrisMain {
    /**
     * The "main" method.
     */
    public static void main() {
        TetrisFrame tf = new TetrisFrame();
        tf.setActionListener((ActionEvent e) -> {
            System.out.println(e.getActionCommand());
        });
    }
}
