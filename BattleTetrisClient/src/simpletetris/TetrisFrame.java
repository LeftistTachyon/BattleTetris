package simpletetris;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The Frame for this application
 * @author Jed Wang
 */
public class TetrisFrame extends JFrame {
    /**
     * The Panel for this application
     */
    public final TetrisPanel panel;
    
    /**
     * This player's matrix
     */
    public final TetrisMatrix player;
    
    /**
     * The opponent's matrix
     */
    public final TetrisMatrix opponent;

    /**
     * Creates a new TetrisFrame.
     */
    public TetrisFrame() {
        super("Simple Tetris");
        panel = new TetrisPanel();
        player = panel.playerMatrix;
        opponent = panel.opponentMatrix;
        
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.setSize(new Dimension(2*TetrisMatrix.WIDTH*Mino.MINO_WIDTH + 570, 
                (int) ((TetrisMatrix.VISIBLE_HEIGHT+1)*Mino.MINO_WIDTH) + 55));
        super.setResizable(true);
        super.getContentPane().add(panel);
        
        TetrisKeyAdapter tka = new TetrisKeyAdapter(panel.playerMatrix);
        tka.setActionListener((ActionEvent e) -> {
            notifyListener(e.getActionCommand());
        });
        super.addKeyListener(tka);
        
        panel.tka = tka;
        
        TetrisFrame _this = this;
        
        panel.addListener((ActionEvent e) -> {
            String message = e.getActionCommand();
            if(message.startsWith("MATCHOVER")) {
                if(Boolean.parseBoolean(message.substring(9))) {
                    // I won!
                    AudioPlayer.stopBackgroundMusic();
                    AudioPlayer.playWinMatchSFX();
                    JOptionPane.showMessageDialog(_this, "You Won!",
                            "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                } else {
                    // I lost....
                    AudioPlayer.stopBackgroundMusic();
                    AudioPlayer.playLoseMatchSFX();
                    JOptionPane.showMessageDialog(_this, "You lost.",
                            "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            } else notifyListener(message);
        });
        
        super.setVisible(true);
        new Thread(panel).start();
    }
    
    /**
     * The listener which islistening in
     */
    private ActionListener listener = null;
    
    /**
     * Sets the current ActionListener
     * @param al the ActionListener to set to
     */
    public void setActionListener(ActionListener al) {
        listener = al;
    }
    
    /**
     * Removes the listener which is listening to this TetrisBag.
     */
    public void removeActionListener() {
        listener = null;
    }
    
    /**
     * Notifies the listener that an event occured.
     * @param message the message to send
     */
    private void notifyListener(String message) {
        if(listener != null)
            listener.actionPerformed(new ActionEvent(this, 0, message));
    }
}