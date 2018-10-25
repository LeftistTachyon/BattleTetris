package simpletetris;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * The JPanel where all of the images are drawn
 * @author Jed Wang
 */
public class TetrisPanel extends JPanel implements Runnable {
    /**
     * The matrix that the player manipulates.
     */
    public final TetrisMatrix playerMatrix;
    
    /**
     * The score of this player.
     */
    private int playerScore;
    
    /**
     * The matrix that the opponent manipulates.
     */
    public final TetrisMatrix opponentMatrix;
    
    /**
     * The score of the opponent.
     */
    private int opponentScore;
    
    /**
     * The TetrisKeyAdapter that is listening in to this Panel.
     */
    protected TetrisKeyAdapter tka;
    
    /**
     * The image to draw in the center
     */
    private BufferedImage centerImage;
    
    /**
     * The y for the transform.
     * Used to draw falling boards - stores the y-coordinate.
     */
    private double loseTransformY = 0;
    
    /**
     * The velocity for the transform.
     * Used to draw falling boards - stores the velocity.
     */
    private double loseTransformV = 0;
    
    /**
     * Best of {@code FIRST_TO*2-1}. In this case, best of 3.
     */
    public static final int FIRST_TO = 2;
    
    /**
     * The image for the score-keeper / win-tracker.
     */
    private static final BufferedImage WIN_TRACKER;
    
    /**
     * The image for the background of everything.
     */
    private static final BufferedImage BACKBACKGROUND;
    
    /**
     * The image for the READY text
     */
    private static final BufferedImage READY;
    
    /**
     * The image for the GO text
     */
    private static final BufferedImage GO;
    
    static {
        BufferedImage temp = null;
        try {
            temp = ImageIO.read(new File("images/winTracker" + FIRST_TO + ".png"));
        } catch (IOException ex) {
            System.err.println("Win tracker image file not found");
        }
        WIN_TRACKER = temp;
        
        temp = null;
        try {
            temp = ImageIO.read(new File("images/backbackground.jpg"));
        } catch (IOException ex) {
            System.err.println("Back background image file not found");
        }
        BACKBACKGROUND = temp;
        
        temp = null;
        try {
            temp = ImageIO.read(new File("images/ready.png"));
        } catch (IOException ex) {
            System.err.println("Ready image file not found");
        }
        READY = temp;
        
        temp = null;
        try {
            temp = ImageIO.read(new File("images/go.png"));
        } catch (IOException ex) {
            System.err.println("Go image file not found");
        }
        GO = temp;
    }
    
    /**
     * Whether to stop
     */
    private boolean stop = false;

    /**
     * Creates a new TetrisPanel.
     */
    public TetrisPanel() {
        playerScore = 0; 
        opponentScore = 0;
        playerMatrix = new TetrisMatrix(true);
        opponentMatrix = new TetrisMatrix(false);
        
        centerImage = null;
        
        playerMatrix.addActionListener((ActionEvent e) -> {
            String command = e.getActionCommand();
            if(command.equals("GAMEOVER")) {
                opponentScore++;
                playerMatrix.clearFalling();
                opponentMatrix.clearFalling();
                System.out.println("You lose. :(");
                AudioPlayer.stopBackgroundMusic();
                AudioPlayer.playLoseGameSFX();
                reset();
                loseTransformV = 1;
                Executors.newSingleThreadScheduledExecutor()
                        .schedule(() -> {
                    if(opponentScore == 2) {
                        notifyListeners("MATCHOVERfalse");
                    } else startGame();
                }, 5, TimeUnit.SECONDS);
            } else if(command.startsWith("SEND")) {
                opponentMatrix.addToGarbage(command.substring(4));
            } else notifyListeners(command);
        });
        
        opponentMatrix.addActionListener((ActionEvent e) -> {
            String command = e.getActionCommand();
            if(command.equals("GAMEOVER")) {
                playerScore++;
                playerMatrix.clearFalling();
                opponentMatrix.clearFalling();
                System.out.println("You win! :)");
                AudioPlayer.stopBackgroundMusic();
                AudioPlayer.playWinGameSFX();
                reset();
                loseTransformV = -1;
                Executors.newSingleThreadScheduledExecutor()
                        .schedule(() -> {
                    if(playerScore == 2) {
                        notifyListeners("MATCHOVERtrue");
                    } else startGame();
                }, 5, TimeUnit.SECONDS);
            } else if(command.startsWith("SEND")) {
                playerMatrix.addToGarbage(command.substring(4));
            }
        });
        
        startGame();
    }
    
    /**
     * Resets both matrixes.
     */
    private void reset() {
        tka.setListening(false);
        playerMatrix.reset();
        opponentMatrix.reset();
    }
    
    /**
     * Starts a game.
     */
    private void startGame() {
        new Thread(() -> {
            try {
                loseTransformV = 0;
                loseTransformY = 0;
                AudioPlayer.playInGameBackground();
                centerImage = READY;
                Thread.sleep(1000);
                centerImage = GO;
                Thread.sleep(500);
                centerImage = null;
                playerMatrix.start();
                opponentMatrix.start();
                tka.setListening(true);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
    
    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        drawBackground(g2D, BACKBACKGROUND);
        
        g2D.translate(20, 20);
        
        if(loseTransformY > 0) {
            if(loseTransformY < getHeight()) {
                g2D.translate(0, loseTransformY);
                playerMatrix.draw(g2D);
                g2D.translate(0, -loseTransformY);
            } else g2D.translate(235 + Mino.MINO_WIDTH*TetrisMatrix.WIDTH - 
                    TetrisMatrix.BAR_WIDTH_GAP, 0);
        } else {
            playerMatrix.draw(g2D);
        }
        
        g2D.translate(7.5, 0);
        
        int bottom = (int) (Mino.MINO_WIDTH*TetrisMatrix.VISIBLE_HEIGHT - 50);
        g2D.setColor(Color.WHITE);
        g2D.fillRect(-100, bottom - 50, 200, 50);
        
        int startXL = -19 - 24 * FIRST_TO, startXR = -5 + 24 * FIRST_TO;
        g2D.setColor(Color.YELLOW);
        for(int i = 0; i < playerScore; i++) {
            g2D.fillRect(startXL, bottom - 37, 21, 24);
            startXL += 24;
        }
        for(int i = 0; i < opponentScore; i++) {
            g2D.fillRect(startXR, bottom - 37, 21, 24);
            startXR -= 24;
        }
        
        g2D.drawImage(WIN_TRACKER, null, -100, bottom - 50); 
        g2D.translate(-7.5, 0);
        
        if(loseTransformY > 0) {
            if(-loseTransformY < getHeight()) {
                if(loseTransformY < 0) g2D.translate(0, -loseTransformY);
                opponentMatrix.draw(g2D);
                if(loseTransformY < 0) g2D.translate(0, loseTransformY);
            } else g2D.translate(235 + Mino.MINO_WIDTH*TetrisMatrix.WIDTH - 
                    TetrisMatrix.BAR_WIDTH_GAP, 0);
        } else opponentMatrix.draw(g2D);
        
        if(centerImage != null) {
            try {
                g2D.transform(g2D.getTransform().createInverse());
            } catch (NoninvertibleTransformException ex) {
                System.err.println("This transform can't be inverted.");
            }
            int iX = (getWidth() - centerImage.getWidth()) / 2, 
                    iY = (getHeight() - centerImage.getHeight()) / 2;
            g2D.drawImage(centerImage, null, iX, iY);
        }
    }
    
    /**
     * Fills the background with a solid color
     * @param g2D the Graphics2D to draw with
     * @param c the color to fill with
     */
    public void drawBackground(Graphics2D g2D, Color c) {
        g2D.setColor(c);
        g2D.fillRect(0, 0, getWidth(), getHeight());
    }
    
    /**
     * Draws the background with an image
     * @param g2D the Graphics2D to draw with
     * @param bi the image to fill in the background with
     */
    public void drawBackground(Graphics2D g2D, BufferedImage bi) {
        g2D.drawImage(bi, 0, 0, getWidth(), getHeight(), null);
    }

    @Override
    public void run() {
        while(!stop) {
            repaint();
            updateVariables();
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                System.err.println("Interrupted.");
            }
        }
    }
    
    /**
     * Updates variables by one step.
     */
    private void updateVariables() {
        if(loseTransformV != 0) {
            if(loseTransformV < 0) {
                loseTransformV -= 0.5;
            } else {
                loseTransformV += 0.5;
            }
            loseTransformY += loseTransformV;
            loseTransformY %= Integer.MAX_VALUE;
        }
    }
    
    /**
     * Stops drawing.
     */
    public void stop() {
        stop = true;
    }
    
    /**
     * All ActionListeners which are listening in.
     */
    private ArrayList<ActionListener> listeners = null;
    
    /**
     * Adds a listener which is now listening in to this TetrisPanel.
     * @param al the ActionListener to add
     */
    public void addListener(ActionListener al) {
        if(listeners == null) listeners = new ArrayList<>();
        listeners.add(al);
    }
    
    /**
     * Removes all listeners which are listening in.
     */
    public void removeAllListeners() {
        listeners = null;
    }
    
    /**
     * Notifies all listeners that an event has occurred
     * @param message the message to relay
     */
    public void notifyListeners(String message) {
        if(listeners == null) return;
        ActionEvent ae = new ActionEvent(this, 0, message);
        for(ActionListener al : listeners) {
            al.actionPerformed(ae);
        }
    }
}
