package simpletetris;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import simpletetris.TetrisKeyAdapter.GameAction;
import static simpletetris.TetrisKeyAdapter.GameAction.*;
import static simpletetris.Mino.*;
import static java.awt.Color.*;
import java.awt.RenderingHints;
import java.util.regex.Pattern;

/**
 * A class that represents the Tetris matrix
 * @author Jed Wang
 */
public class TetrisMatrix {
    /**
     * The currently falling tetromino
     */
    private Tetromino falling;
    
    /**
     * The RNG for which piece is coming next
     */
    private TetrisBag bag;
    
    /**
     * The "x" coordinate of the top left corner of the Tetromino's box
     */
    private int x;
    
    /**
     * The "y" coordinate of the top left corner of the Tetromino's box
     */
    private int y;
    
    /**
     * A {@code Color[][]} that stores all of the minos in the matrix.
     */
    private Color[][] matrix;
    
    /**
     * Stores whether a piece can swap to hold.
     */
    private boolean holdSwappable;
    
    /**
     * The tetromino that is currently being held.
     */
    private Tetromino hold;
    
    /**
     * The ScoreKeeper for this TetrisMatrix.
     */
    //private final ScoreKeeper sk;
    
    /**
     * The GarbageDealer for this TetrisMatrix.
     */
    //private final GarbageDealer gd;
    
    /**
     * The GarbageHandler for this TetrisMatrix.
     */
    private final GarbageHandler gh;
    
    /**
     * Determines whether the piece was just kicked
     */
    private boolean kicked;
    
    /**
     * The last action performed by the player
     */
    private GameAction lastAction;
    
    /**
     * Adding gravity
     */
    private final Gravity gravity;
    
    /**
     * Adding lock delay for tetrominos
     */
    private final LockDelay lockDelay;
    
    /**
     * All of the listeners
     */
    private ArrayList<ActionListener> listeners = null;
    
    /**
     * Controls the line clear animation
     */
    private LinkedList<Integer> rowsCleared;
    
    /*
     * Controls the line clear animation
     */
    private double clearAnimation;
    
    /**
     * Whether this TetrisMatrix is on the left
     */
    private final boolean onLeft;
    
    /**
     * The service that maintains gravity and locking.
     */
    private ScheduledExecutorService service;
    
    /**
     * Text that denotes a t-spin or tetris
     */
    private String specialText = null;
    
    /**
     * The width of the matrix
     */
    public static final int WIDTH = 10;
    
    /**
     * The height of the matrix
     */
    public static final int HEIGHT = 40;
    
    /**
     * The visible height of the matrix; playing field
     */
    public static final double VISIBLE_HEIGHT = 20.5;
    
    /**
     * The height of the bars for garbage (incoming and outgoing)
     */
    public static final int BAR_HEIGHT = 400;
    
    /**
     * The width of the bars for garbage (incoming and outgoing)
     */
    public static final int BAR_WIDTH = 20;
    
    /**
     * The height of the inside of the bar
     */
    public static final int INNER_BAR_HEIGHT = 380;
    
    /**
     * The width of the inside of the bar
     */
    public static final int INNER_BAR_WIDTH = 10;
    
    /**
     * The height of a step on the bar
     */
    public static final int BAR_STEP_HEIGHT = INNER_BAR_HEIGHT / 20;
    
    /**
     * The gap between the inner bar and the outer bar for the width
     */
    public static final int BAR_WIDTH_GAP = (BAR_WIDTH - INNER_BAR_WIDTH) / 2;
    
    /**
     * The gap between the inner bar and the outer bar for the height
     */
    public static final int BAR_HEIGHT_GAP = (BAR_HEIGHT - INNER_BAR_HEIGHT) / 2;
    
    /**
     * A block of the background
     */
    private static final BufferedImage BACKGROUND_BLOCK;
    
    /**
     * The background image
     */
    private static final BufferedImage BACKGROUND_IMAGE;
    
    /**
     * The background texture for the piece preview
     */
    private static final BufferedImage PIECE_BACKGROUND;
    
    /**
     * The outline/border of the bar
     */
    private static final BufferedImage BAR_OUTLINE;
    
    /**
     * The icon for incoming garbage
     */
    private static final BufferedImage IN_GARBAGE_ICON;
    
    /**
     * The text/image that appears when an all-clear is executed.
     */
    private static final BufferedImage ALL_CLEAR;
    
    static {
        BufferedImage temp = null;
        try {
            temp = ImageIO.read(new File("images/background_block.png"));
        } catch (IOException ex) {
            System.err.println("Background block image file not found");
        }
        BACKGROUND_BLOCK = temp;
        
        temp = null;
        try {
            temp = ImageIO.read(new File("images/background.jpg"));
        } catch (IOException ex) {
            System.err.println("Background image file not found");
        }
        BACKGROUND_IMAGE = temp;
        
        temp = null;
        try {
            temp = ImageIO.read(new File("images/texture.png"));
        } catch (IOException ex) {
            System.err.println("Piece background image file not found");
        }
        PIECE_BACKGROUND = temp;
        
        temp = null;
        try {
            temp = ImageIO.read(new File("images/bar.png"));
        } catch (IOException ex) {
            System.err.println("Bar background image file not found");
        }
        BAR_OUTLINE = temp;
        
        temp = null;
        try {
            temp = ImageIO.read(new File("images/inGarbage.png"));
        } catch (IOException ex) {
            System.err.println("Incoming garbage icon image file not found");
        }
        IN_GARBAGE_ICON = temp;
        
        temp = null;
        try {
            temp = ImageIO.read(new File("images/allClear.png"));
        } catch (IOException ex) {
            System.err.println("All clear text image file not found");
        }
        ALL_CLEAR = temp;
    }
    
    /**
     * Whether the all-clear text should be shown
     */
    private boolean showAllClear = false;
    
    /**
     * Creates a new, default TetrisMatrix.
     */
    public TetrisMatrix() {
        this(true);
    }

    /**
     * Creates a new TetrisMatrix.
     * @param onLeft whether this TetrisMatrix is on the left side of the pair
     */
    public TetrisMatrix(boolean onLeft) {
        this.onLeft = onLeft;
        
        gh = new GarbageHandler();
        gh.addListener((ActionEvent e) -> {
            if(e.getActionCommand() != null) {
                notifyListeners("SEND" + e.getActionCommand());
            }
        });
        // gd.addGarbage("2 2 2 2 2 2 2");
        
        rowsCleared = null;
        
        gravity = new Gravity();
        if(onLeft) lockDelay = new LockDelay();
        else lockDelay = null;
        
        kicked = false;
        hold = null;
        matrix = new Color[WIDTH][HEIGHT];
        bag = new TetrisBag(!onLeft);
        bag.setActionListener((ActionEvent e) -> {
            notifyListeners(e.getActionCommand());
        });
        if(!onLeft) {
            bag.addBag("OOOOOOO");
            bag.addBag("OOOOOOO");
            bag.addBag("OOOOOOO");
            bag.addBag("OOOOOOO");
            bag.addBag("OOOOOOO");
        }
        
        falling = null;
    }
    
    /**
     * Resets everything so everything is anew.
     */
    public void reset() {
        if(service != null) service.shutdown();
        service = null;
        falling = null;
        
        gh.reset();
        
        rowsCleared = null;
        
        kicked = false;
        hold = null;
        matrix = new Color[WIDTH][HEIGHT];
        bag = new TetrisBag(!onLeft);
        bag.setActionListener((ActionEvent e) -> {
            notifyListeners(e.getActionCommand());
        });
        if(!onLeft) {
            bag.addBag("OOOOOOO");
            bag.addBag("OOOOOOO");
            bag.addBag("OOOOOOO");
            bag.addBag("OOOOOOO");
            bag.addBag("OOOOOOO");
        }
    }
    
    /**
     * Starts play on this matrix.
     */
    public void start() {
        newPiece();
        
        if(onLeft) {
            service = Executors.newScheduledThreadPool(2);
            service.scheduleAtFixedRate(gravity, 0, 10, TimeUnit.MILLISECONDS);
            service.scheduleAtFixedRate(lockDelay, 0, 10, TimeUnit.MILLISECONDS);
        } else {
            service = Executors.newScheduledThreadPool(1);
            service.scheduleAtFixedRate(gravity, 0, 10, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Draws this TetrisMatrix
     * @param g2D the Graphics2D to draw with
     */
    public void draw(Graphics2D g2D) {
        g2D.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, 
                BasicStroke.JOIN_MITER));
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2D.translate(15, 0);
        g2D.setColor(BLACK);
        g2D.setFont(new Font("Consolas", 0, 36));
        if(onLeft) {
            g2D.drawImage(PIECE_BACKGROUND, null, 0, 157);
            g2D.setFont(new Font("Consolas", 0, 17));
            g2D.drawString("Lines Sent:", 5, 182);
            g2D.setFont(new Font("Consolas", 0, 36));
            g2D.drawString("" + gh.getLinesSent(), 5, 218);
            g2D.drawRect(0, 157, 110, 70);
            
            g2D.drawString("HOLD", 15, 45);
            
            g2D.drawImage(PIECE_BACKGROUND, null, 0, 50);
            if (hold != null) {
                BufferedImage miniImage = hold.getMiniImage();
                g2D.drawImage(miniImage, 5, 55, 100, 60, null);
            }
            
            g2D.drawRect(0, 50, 110, 70);
            
            g2D.translate(90 - BAR_WIDTH - BAR_WIDTH_GAP,
                    MINO_WIDTH * VISIBLE_HEIGHT - 5 - BAR_HEIGHT + BAR_HEIGHT_GAP);
            g2D.setColor(DARK_GRAY);
            g2D.fillRect(0, 0, INNER_BAR_WIDTH, INNER_BAR_HEIGHT);
            
            int[] temp = gh.getInBarFill();
            if (temp != null) {
                for (int i = 0, yy = 0; i < temp.length; i++, yy += BAR_STEP_HEIGHT) {
                    switch (temp[i]) {
                        case 0:
                            continue;
                        case 1:
                            g2D.setColor(yellow);
                            break;
                        case 2:
                            g2D.setColor(orange);
                            break;
                        case 3:
                            g2D.setColor(new Color(255, 150, 0));
                            break;
                        case 4:
                            g2D.setColor(new Color(255, 100, 0));
                            break;
                        case 5:
                            g2D.setColor(red);
                            break;
                        case 6:
                        default:
                            g2D.setColor(new Color(128, 0, 0));
                            break;
                    }
                    g2D.fillRect(0, yy, INNER_BAR_WIDTH, BAR_STEP_HEIGHT);
                }
            }

            //g2D.setColor(BLACK);
            g2D.translate(-BAR_WIDTH_GAP, -BAR_HEIGHT_GAP);
            g2D.drawImage(BAR_OUTLINE, null, 0, 0);
            
            g2D.drawImage(IN_GARBAGE_ICON, BAR_WIDTH / 2 - 26, -58, null);
            
            g2D.translate(-90 + BAR_WIDTH + BAR_WIDTH_GAP,
                    -MINO_WIDTH * VISIBLE_HEIGHT + BAR_HEIGHT + 5);
        } else {
            g2D.drawString("NEXT", 15, 45);
            
            g2D.drawImage(PIECE_BACKGROUND, null, 0, 50);
            
            if (bag.next(0) != null) {
                BufferedImage miniImage = bag.next(0).getMiniImage();
                g2D.drawImage(miniImage, 5, 55, 100, 60, null);
            }
            
            g2D.setColor(BLACK);
            g2D.drawRect(0, 50, 110, 70);
            
            for (int i = 1; i < 3; i++) {
                g2D.setClip(-3, 67 + 85 * i, 95, 63);
                g2D.drawImage(PIECE_BACKGROUND, null, 0, 70 + 85 * i);
                
                if (bag.next(i) != null) {
                    BufferedImage miniImage = bag.next(i).getMiniImage();
                    g2D.drawImage(miniImage, 5, 75 + 85 * i, 80, 48, null);
                }
                g2D.drawRect(0, 70 + 85 * i, 90, 58);
            }
            
            g2D.setClip(null);
            
            boolean a = gh.getCombo() > 1, b = specialText != null;
            if(a || b) {
                g2D.translate(0, 500);
                g2D.drawImage(PIECE_BACKGROUND, null, 0, 0);
                if(a) {
                    g2D.setFont(new Font("Consolas", 0, 20));
                    g2D.drawString(gh.getCombo() + " Combo", 5, 25);
                }
                if(b) {
                    g2D.setFont(new Font("Consolas", 0, 15));
                    String[] data = specialText.split(Pattern.quote("|"));
                    int y_ = 50;
                    for(String line : data) {
                        g2D.drawString(line, 5, y_);
                        y_ += 15;
                    }
                }
                g2D.drawRect(0, 0, 110, 70);
                g2D.translate(0, -500);
                g2D.setFont(new Font("Consolas", 0, 36));
            }
            
            /*g2D.translate(-90 + BAR_WIDTH,
                    -MINO_WIDTH * VISIBLE_HEIGHT + BAR_HEIGHT + 5);*/
        }
        
        g2D.translate(110, 0);
        g2D.setClip(0, 0, MINO_WIDTH*WIDTH, 
                (int) (MINO_WIDTH*VISIBLE_HEIGHT));
        g2D.drawImage(BACKGROUND_IMAGE, 0, 0, 
                MINO_WIDTH*WIDTH, (int) (MINO_WIDTH*VISIBLE_HEIGHT), null);
        
        g2D.translate(0, -MINO_WIDTH*(HEIGHT - VISIBLE_HEIGHT));
        
        for(int i = 0; i < matrix.length; i++) {
            for(int j = 0; j < matrix[i].length; j++) {
                
                if(matrix[i][j] == null) {
                    g2D.drawImage(BACKGROUND_BLOCK, null, 
                            i*MINO_WIDTH, j*MINO_WIDTH);
                } else {
                    drawMino(i*MINO_WIDTH, j*MINO_WIDTH, 
                            matrix[i][j], g2D);
                }
            }
        }
        
        if(showAllClear) {
            int acX = (MINO_WIDTH*WIDTH - ALL_CLEAR.getWidth())/2, 
                    acY = 23*MINO_WIDTH;
            g2D.drawImage(ALL_CLEAR, null, acX, acY);
        }
        
        if(falling != null) {
            int tlx = x * MINO_WIDTH, tly = y * MINO_WIDTH,
                    tlGy = getGhostY() * MINO_WIDTH;
            g2D.setColor(falling.getColor());
            Color[][] tetro = falling.getDrawBox();
            for (int i = 0; i < tetro.length; i++) {
                for (int j = 0; j < tetro[i].length; j++) {
                    if (tetro[i][j] == null) {
                        continue;
                    }
                    g2D.drawRect(tlx + i * MINO_WIDTH + 7, tlGy + j * MINO_WIDTH + 7,
                            MINO_WIDTH - 14, MINO_WIDTH - 14);
                    drawMino(tlx + i * MINO_WIDTH, tly + j * MINO_WIDTH,
                            tetro[i][j], g2D);
                }
            }
        }
        
        if(rowsCleared != null) {
            if(clearAnimation <= -255) {
                for(int i:rowsCleared) {
                    for (int j = i; j >= 1; j--) {
                        clearLine(j);
                    }
                    emptyLine(0);
                }
                
                rowsCleared = null;
                
                // gravity.enable();
                
                addGarbage();
                
                newPiece();
            } else {
                Color whitish = new Color(255, 255, 255, (clearAnimation >= 0) 
                        ? ((clearAnimation <= 255) ? (int) clearAnimation : 255) 
                        : 0);
                g2D.setStroke(new BasicStroke());
                g2D.setColor(whitish);
                for(int row:rowsCleared) {
                    int yPos = MINO_WIDTH * row;
                    g2D.fillRect(0, yPos, MINO_WIDTH * WIDTH, MINO_WIDTH);
                }

                clearAnimation -= 25.5;
                
                g2D.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, 
                        BasicStroke.JOIN_MITER));
            }
        }
        
        g2D.setClip(null);
        g2D.setColor(BLACK);
        g2D.drawRect(0, (int) (MINO_WIDTH * (HEIGHT - VISIBLE_HEIGHT)),
                    WIDTH * MINO_WIDTH, (int) (VISIBLE_HEIGHT * MINO_WIDTH));
        
        if(onLeft) {
            g2D.translate(MINO_WIDTH * WIDTH, MINO_WIDTH * (HEIGHT - VISIBLE_HEIGHT));
            g2D.drawString("NEXT", 15, 45);
            
            g2D.drawImage(PIECE_BACKGROUND, null, 0, 50);
            
            if (bag.next(0) != null) {
                BufferedImage miniImage = bag.next(0).getMiniImage();
                g2D.drawImage(miniImage, 5, 55, 100, 60, null);
            }
            
            g2D.setColor(BLACK);
            g2D.drawRect(0, 50, 110, 70);
            
            for (int i = 1; i < 3; i++) {
                g2D.setClip(17, 67 + 85 * i, 95, 63);
                g2D.drawImage(PIECE_BACKGROUND, null, 20, 70 + 85 * i);
                
                if (bag.next(i) != null) {
                    BufferedImage miniImage = bag.next(i).getMiniImage();
                    g2D.drawImage(miniImage, 25, 75 + 85 * i, 80, 48, null);
                }
                g2D.drawRect(20, 70 + 85 * i, 90, 58);
            }
            
            g2D.setClip(null);
            
            boolean a = gh.getCombo() > 1, b = specialText != null;
            if(a || b) {
                g2D.translate(0, 500);
                g2D.drawImage(PIECE_BACKGROUND, null, 0, 0);
                if(a) {
                    g2D.setFont(new Font("Consolas", 0, 20));
                    g2D.drawString(gh.getCombo() + " Combo", 5, 25);
                }
                if(b) {
                    g2D.setFont(new Font("Consolas", 0, 15));
                    String[] data = specialText.split(Pattern.quote("|"));
                    int y_ = 50;
                    for(String line : data) {
                        g2D.drawString(line, 5, y_);
                        y_ += 15;
                    }
                }
                g2D.drawRect(0, 0, 110, 70);
                g2D.translate(0, -500);
                g2D.setFont(new Font("Consolas", 0, 36));
            }
        } else {
            g2D.translate(MINO_WIDTH * WIDTH, MINO_WIDTH * (HEIGHT - VISIBLE_HEIGHT));
            
            g2D.drawImage(PIECE_BACKGROUND, null, 0, 157);
            g2D.setFont(new Font("Consolas", 0, 17));
            g2D.drawString("Lines Sent:", 5, 182);
            g2D.setFont(new Font("Consolas", 0, 36));
            g2D.drawString("" + gh.getLinesSent(), 5, 218);
            g2D.drawRect(0, 157, 110, 70);
            
            g2D.drawString("HOLD", 15, 45);
            
            g2D.drawImage(PIECE_BACKGROUND, null, 0, 50);
            if (hold != null) {
                BufferedImage miniImage = hold.getMiniImage();
                g2D.drawImage(miniImage, 5, 55, 100, 60, null);
            }
            
            g2D.drawRect(0, 50, 110, 70);
            
            g2D.translate(15 + BAR_WIDTH - BAR_WIDTH_GAP,
                    MINO_WIDTH * VISIBLE_HEIGHT - 5 - BAR_HEIGHT + BAR_HEIGHT_GAP);
            g2D.setColor(DARK_GRAY);
            g2D.fillRect(0, 0, INNER_BAR_WIDTH, INNER_BAR_HEIGHT);
            
            int[] temp = gh.getInBarFill();
            if (temp != null) {
                for (int i = 0, yy = 0; i < temp.length; i++, yy += BAR_STEP_HEIGHT) {
                    switch (temp[i]) {
                        case 0:
                            continue;
                        case 1:
                            g2D.setColor(yellow);
                            break;
                        case 2:
                            g2D.setColor(orange);
                            break;
                        case 3:
                            g2D.setColor(new Color(255, 150, 0));
                            break;
                        case 4:
                            g2D.setColor(new Color(255, 100, 0));
                            break;
                        case 5:
                            g2D.setColor(red);
                            break;
                        case 6:
                        default:
                            g2D.setColor(new Color(128, 0, 0));
                            break;
                    }
                    g2D.fillRect(0, yy, INNER_BAR_WIDTH, BAR_STEP_HEIGHT);
                }
            }
            
            g2D.translate(-BAR_WIDTH_GAP, -BAR_HEIGHT_GAP);
            g2D.drawImage(BAR_OUTLINE, null, 0, 0);
            
            g2D.drawImage(IN_GARBAGE_ICON, BAR_WIDTH / 2 - 26, -58, null);
            
            g2D.translate(BAR_WIDTH*7 - BAR_WIDTH_GAP - 103, 
                    -MINO_WIDTH * VISIBLE_HEIGHT + BAR_HEIGHT + 5);
        }
        
        g2D.translate(110, 0);
    }
    
    /**
     * Returns the section of the matrix that the falling piece occupies<br>
     * e.g.<br>
     * Falling piece (a TetO) is at (0, 0).<br>
     * The matrix looks as such: <br>
     * <code>....<br>
     * X.XX<br>
     * XXXX<br></code><br>
     * The output would be:<br>
     * <code>..<br>
     * X.</code>
     * @return the section of the matrix that the falling piece occupies
     */
    public Color[][] miniMatrix() {
        return miniMatrix(0, 0);
    }
    
    /**
     * Returns the section of the matrix that the falling piece occupies, 
     * considering the offset.<br>
     * e.g. <code>(1, -1)</code> would kick the piece right 1 and down 1
     * @see TetrisMatrix#miniMatrix()
     * @param offsetX the offset of the X coordinate
     * @param offsetY the offset of the Y coordinate
     * @return the section of the matrix that the falling piece occupies 
     * (not really) 
     */
    public Color[][] miniMatrix(int offsetX, int offsetY) {
        if(falling == null) return null;
        
        Color[][] output = new Color[falling.getRotationBoxWidth()]
                [falling.getRotationBoxWidth()];
        
        int tlx = x + offsetX, tly = y - offsetY;
        for(int i = 0; i < output.length; i++) {
            int trueX = tlx + i;
            for(int j = 0; j < output[i].length; j++) {
                int trueY = tly + j;
                if(trueX < 0 || trueX >= WIDTH || trueY < 0 || trueY >= HEIGHT) {
                    output[i][j] = BLACK;
                } else {
                    output[i][j] = matrix[trueX][trueY];
                }
            }
        }
        
        return output;
    }
    
    /**
     * Adds garbage to the queue for this matrix
     * @param garbage the garbage
     */
    public void addToGarbage(String garbage) {
        gh.addGarbage(garbage);
    }
    
    /**
     * Executes the given action.
     * @param ga the action to execute.
     */
    public void executeAction(GameAction ga) {
        if(falling == null) return;
        switch(ga) {
            case ROTATE_LEFT:
                Point kickL = falling.getWallKick(this, 
                        Tetromino.COUNTERCLOCKWISE);
                if(kickL == null) return;
                falling.rotateLeft();
                x += kickL.x;
                y -= kickL.y;
                kicked = kickL.x != 0 || kickL.y != 0;
                AudioPlayer.playMoveSFX(1.0);
                if(lockDelay != null) lockDelay.addTouch();
                lastAction = ga;
                break;
            case ROTATE_RIGHT:
                Point kickR = falling.getWallKick(this, 
                        Tetromino.CLOCKWISE);
                if(kickR == null) return;
                falling.rotateRight();
                x += kickR.x;
                y -= kickR.y;
                kicked = kickR.x != 0 || kickR.y != 0;
                AudioPlayer.playMoveSFX(1.0);
                if(lockDelay != null) lockDelay.addTouch();
                lastAction = ga;
                break;
            case MOVE_LEFT:
                if(!falling.overlaps(miniMatrix(-1, 0))) {
                    x--;
                    lastAction = ga;
                }
                AudioPlayer.playMoveSFX(0.1);
                break;
            case MOVE_RIGHT:
                if(!falling.overlaps(miniMatrix(1, 0))) {
                    x++;
                    lastAction = ga;
                }
                AudioPlayer.playMoveSFX(0.1);
                break;
            case SOFT_DROP:
                if(!falling.overlaps(miniMatrix(0, -1))) {
                    y++;
                    lastAction = ga;
                }
                AudioPlayer.playMoveSFX(0.1);
                break;
            case HARD_DROP:
                y = getGhostY();
                if(onLeft) lockPiece();
                AudioPlayer.playMoveSFX(1.0);
                break;
            case HOLD:
                if(holdSwappable) {
                    if(hold == null) {
                        hold = falling;
                        newPiece();
                    } else {
                        Tetromino temp = hold;
                        hold = falling;
                        falling = temp;
                        
                        falling.rotateTo(Tetromino.UP);
                        falling.resetRotationCount();
                        hold.rotateTo(Tetromino.UP);
                        
                        moveTetToStart();
                    }
                    
                    holdSwappable = false;
                }
                break;
        }
    }
    
    /**
     * Chooses the next piece and places it at the top of the playfield.
     */
    public void newPiece() {
        falling = bag.remove();
        
        moveTetToStart();
        
        gravity.resetGravity();
        
        holdSwappable = true;
    }
    
    /**
     * Moves the tetromino to the start
     */
    private void moveTetToStart() {
        y = 20;
        x = (WIDTH - falling.getRotationBoxWidth())/2;
        
        for(int i = 0; i < 3 /* The leeway is by 3 */; i++) {
            if(falling.overlaps(miniMatrix(0, -2)))
                y--;
        }
        if(falling.overlaps(miniMatrix())){
            // no falling piece
            falling = null;
            
            // Game over!
            notifyListeners("GAMEOVER");
        } else if(falling.overlaps(miniMatrix(0, -1))) {
            // ditch the piece first
            Color[][] copy = falling.getDrawBox();
            for(int r= 0; r < copy.length; r++) {
                for(int c = 0; c < copy[r].length; c++) {
                    if(copy[r][c] != null && matrix[r + x][c + y] == null) {
                        matrix[r + x][c + y] = copy[r][c];
                    }
                }
            }
            
            // no falling piece
            falling = null;
            
            // Game over!
            notifyListeners("GAMEOVER");
        }
    }
    
    /**
     * Locks all pieces and does some checks before sending in a new piece.
     */
    public void lockPiece() {
        // stop gravity
        gravity.disable();
        
        // reset lock piece checker
        if(lockDelay != null) lockDelay.reset();
        
        // determine immobility
        boolean immobile = immobile();
        
        // lock
        Color[][] copy = falling.getDrawBox();
        for(int r= 0; r < copy.length; r++) {
            for(int c = 0; c < copy[r].length; c++) {
                if(copy[r][c] != null && matrix[r + x][c + y] == null) {
                    matrix[r + x][c + y] = copy[r][c];
                }
            }
        }
        
        notifyListeners("LOCK" + x + " " + y);
        
        int linesCleared = 0;
        for(int i = 0; i < HEIGHT; i++) {
            if(lineFilled(i)) {
                linesCleared++;
            }
        }
        
        // check for t-spins
        String special = null; 
        boolean b2b = gh.isB2B(), allClear = allClear();
        if(falling instanceof TetT && (lastAction == ROTATE_LEFT || 
                lastAction == ROTATE_RIGHT) && threeCorner()) {
            System.out.println("T-spin " + linesCleared);
            if((!immobile || kicked) && linesCleared < 2) {
                gh.newLinesCleared(linesCleared, GarbageHandler.T_SPIN_MINI, 
                        allClear);
                switch(linesCleared) {
                    case 1:
                        special = "T-spin|mini single";
                        break;
                    case 0:
                        special = "T-spin|mini";
                        break;
                }
            } else {
                gh.newLinesCleared(linesCleared, GarbageHandler.T_SPIN, 
                        allClear);
                switch(linesCleared) {
                    case 0:
                        special = "T-spin";
                        break;
                    case 1:
                        special = "T-spin|single";
                        break;
                    case 2:
                        special = "T-spin|double";
                        break;
                    case 3:
                        special = "T-spin|triple";
                        break;
                }
            }
        } else {
            gh.newLinesCleared(linesCleared, GarbageHandler.NORMAL, 
                    allClear);
            if(linesCleared == 4) special = "Tetris";
        }
        
        if(allClear) {
            showAllClear = true;
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                showAllClear = false;
            }, 2000, TimeUnit.MILLISECONDS);
        }
        
        if(special != null) {
            if(b2b) special = "B2B " + special;
            specialText = special;
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                specialText = null;
            }, 1, TimeUnit.SECONDS);
        }
        
        // empty lines
        for(int i = 0; i < HEIGHT; i++) {
            if(lineFilled(i)) {
                if(rowsCleared == null) rowsCleared = new LinkedList<>();
                emptyLine(i);
                rowsCleared.add(i);
            }
        }
        
        // after locking, reset
        if(linesCleared == 0) {
            // add garbage
            addGarbage();
            newPiece();
        } else {
            falling = null;
            clearAnimation = 510;
        }
    }
    
    /**
     * Clears / Resets the falling tetromino to {@code null}.
     */
    public void clearFalling() {
        falling = null;
    }
    
    /**
     * Adds garbage needed for this drop
     */
    private void addGarbage() {
        int temp = 0;
        boolean addedGarbage = false;
        while(true) {
            int temptemp = gh.peekNextGarbage();
            if(temptemp == 0) break;
            temp += temptemp;
            addGarbageLines(gh.getNextGarbage());
            addedGarbage = true;
            System.out.println("Oof! " + temptemp + " lines of garbage");
            
            if(temp > 5) break;
        }
        if(addedGarbage) AudioPlayer.playGarbageSFX();
    }
    
    /**
     * Determines if the board is all clear
     * @return if the board is all clear
     */
    private boolean allClear() {
        for(int i = 0; i < HEIGHT; i++) {
            if(!lineHomogenous(i))
                return false;
        }
        return true;
    }
    
    /**
     * Determines whether the piece is immobile
     * @return whether the piece is immobile
     */
    private boolean immobile() {
        return falling.overlaps(miniMatrix(1, 0)) 
                && falling.overlaps(miniMatrix(-1, 0)) 
                && falling.overlaps(miniMatrix(0, 1)) 
                && falling.overlaps(miniMatrix(0, -1));
    }
    
    /**
     * Determines whether the t-piece has 3 of the 4 corners of the 
     * bounding box are filled in.
     * @return whether 3-corners are filled
     */
    private boolean threeCorner() {
        if(!(falling instanceof TetT)) 
            throw new IllegalStateException("Cannot perform a 3-corner check on a non-T tetromino");
        
        // box is 3x3
        Color[][] box = miniMatrix();
        int cnt = 0;
        if(box[0][0] != null) cnt++;
        if(box[0][2] != null) cnt++;
        if(box[2][0] != null) cnt++;
        if(box[2][2] != null) cnt++;
        
        return cnt >= 3;
    }
    
    /**
     * Determines the y-coordinate of the ghost-piece
     * @return the y-coordinate of the ghost-piece
     */
    public int getGhostY() {
        int placeHolderY = 0;
        while(!falling.overlaps(miniMatrix(0, -placeHolderY))){
            placeHolderY++;
        }
        return y + placeHolderY - 1;
    }
    
    /**
     * Empties a row of blocks
     * @param row which row to empty
     */
    public void emptyLine(int row) {
        for(int i = 0; i < WIDTH; i++) {
            matrix[i][row] = null;
        }
    }
    
    /**
     * Clears a line in the matrix
     * @param row which row to clear
     */
    public void clearLine(int row) {
        for(int i = 0; i < WIDTH; i++) {
            matrix[i][row] = matrix[i][row - 1];
        }
    }
    
    /**
     * Pushes a line upwards.
     * @param row which row to push upwards
     * @param rows how many lines to push up the given line
     */
    public void pushUpLine(int row, int rows) {
        for(int i = 0; i < WIDTH; i++) {
            matrix[i][row - rows] = matrix[i][row];
        }
    }
    
    /**
     * Determines whether a line is completely filled
     * @param row which row to check
     * @return whether a line is completely filled
     */
    public boolean lineFilled(int row) {
        for(int i = 0; i < WIDTH; i++) {
            if(matrix[i][row] == null) 
                return false;
        }
        return true;
    }
    
    /**
     * Determines whether a line is completely empty
     * @param row which row to check
     * @return whether a line is completely empty
     */
    public boolean lineEmpty(int row) {
        for(int i = 0; i < WIDTH; i++) {
            if(matrix[i][row] != null)
                return false;
        }
        return true;
    }
    
    /**
     * Determines whether a row is either all filled or all empty<br>
     * Used to determine whether an all-clear has occured
     * @param row which row to check
     * @return whether a row is either all filled or all empty
     */
    public boolean lineHomogenous(int row) {
        boolean empty = matrix[0][row] == null;
        return (empty)?lineEmpty(row):lineFilled(row);
    }
    
    /**
     * Adds a given amount of garbage lines to the bottom of the matrix.
     * @param lines how many garbage lines to add
     */
    public void addGarbageLines(int lines) {
        if(lines > HEIGHT) lines = HEIGHT;
        
       for(int i = lines; i < HEIGHT; i++) {
           pushUpLine(i, lines);
       }
       
        for(int i = 0; i < lines; i++) {
            emptyLine(HEIGHT - i - 1);
        }
       
       int hole = (int) (Math.random() * WIDTH);
       for(int i = 0; i < lines; i++) {
           if(Math.random() < 0.25) hole = (int) (Math.random() * WIDTH);
           setGarbageLine(HEIGHT - i - 1, hole);
       }
    }
    
    /**
     * Sets a row as a garbage line
     * @param row which row to set
     * @param hole the column the hole should be at
     */
    private void setGarbageLine(int row, int hole) {
        for(int i = 0; i < WIDTH; i++) {
            if(i != hole) matrix[i][row] = GRAY;
        }
    }
    
    /**
     * Outputs a line of the matrix
     * @param row which row to print
     */
    public void printLine(int row) {
        for(int i = 0; i < WIDTH; i++) {
            System.out.print((matrix[i][row] == null)?" ":"X");
        }
        System.out.println();
    }

    /**
     * Returns the currently falling tetromino
     * @return the currently falling tetromino
     */
    public Tetromino getFallingPiece() {
        return falling;
    }
    
    /**
     * Prints the matrix.
     */
    public void printMatrix() {
        for(Color[] colors : matrix) {
            for(Color color : colors) {
                if(color == null) {
                    System.out.print(" ");
                } else {
                    System.out.print("X");
                }
            }
            System.out.println();
        }
    }
    
    /**
     * Prints the mini-matrix offsetted from the falling tetromino
     * @param offsetX the offset for the x coordinate
     * @param offsetY the offset for the y coordinate
     */
    public void printDebugMatrix(int offsetX, int offsetY) {
        Color[][] mini = miniMatrix(offsetX, offsetY), tet = falling.getDrawBox();
        for(int i = 0;i<mini.length;i++) {
            for(int j = 0;j<mini[i].length;j++) {
                if(mini[j][i] == null && tet[j][i] == null) {
                    System.out.print(" ");
                } else if(mini[j][i] == null ^ tet[j][i] == null) {
                    System.out.print("@");
                } else {
                    System.out.print("X");
                }
            }
            System.out.println();
        }
    }
    
    /**
     * Adds an ActionListener to listen to this TetrisMatrix
     * @param al the ActionListener to add
     */
    public void addActionListener(ActionListener al) {
        if(listeners == null) listeners = new ArrayList<>();
        listeners.add(al);
    }
    
    /**
     * Clears all listeners.
     */
    public void clearListeners() {
        listeners = null;
    }
    
    /**
     * Notifies listeners of a message / occurance
     * @param message the message to send
     */
    private void notifyListeners(String message) {
        if(listeners == null) return;
        ActionEvent ae = new ActionEvent(this, 0, message);
        for(ActionListener listener : listeners) {
            listener.actionPerformed(ae);
        }
    }
    
    /**
     * Disables gravity.
     */
    public void disableGravity() {
        gravity.disable();
    }
    
    /**
     * Enables gravity.
     */
    public void enableGravity() {
        gravity.enable();
    }
    
    /**
     * Pauses gravity.
     */
    public void pauseGravity() {
        gravity.pause();
    }
    
    /**
     * Resumes gravity.
     */
    public void resumeGravity() {
        gravity.resume();
    }
    
    /**
     * Adds gravity to the pieces
     */
    private class Gravity implements Runnable {
        /**
         * Set to {@code false} to stop gravity
         */
        private boolean enabled = true;
        
        /**
         * Whether this Gravity is paused
         */
        private boolean paused = false;
        
        /**
         * The counter
         */
        private int i = 0;
        
        @Override
        public void run() {
            try {
                /*if(cnt > 1) {
                    System.out.println(cnt + "/60 G");
                }*/
                if(falling != null) {
                    if (paused) {
                        if(falling == null) return;
                        if (falling.overlaps(miniMatrix(0, -1)) && enabled) {
                            enabled = false;
                        }
                        if(falling == null) return;
                        if (!falling.overlaps(miniMatrix(0, -1)) && !enabled) {
                            enabled = true;
                        }
                        return;
                    }
                    if (!enabled) {
                        i = 0;
                    } else if (i == 99) {
                        y++;
                        lastAction = GRAVITY;
                    }
                    if (falling.overlaps(miniMatrix(0, -1)) && enabled) {
                        enabled = false;
                    }
                    if (!falling.overlaps(miniMatrix(0, -1)) && !enabled) {
                        enabled = true;
                    }
                    i++;
                    i %= 100;
                }
            } catch (Exception e) {
                // Just in case stuff happens
                e.printStackTrace();
            }
        }
        
        /**
         * Stops gravity.
         */
        public void disable() {
            enabled = false;
        }
        
        /**
         * Restarts gravity.
         */
        public void enable() {
            enabled = true;
        }
        
        /**
         * Pauses gravity.
         */
        public void pause() {
            paused = true;
        }
        
        /**
         * Resumes gravity.
         */
        public void resume() {
            paused = false;
        }
        
        /**
         * Resets gravity so the next piece is affected correctly
         */
        public void resetGravity() {
            i = 0;
        }
    }
    
    /**
     * A class that deals with lock delay.
     */
    private class LockDelay implements Runnable {
        /**
         * The number of times this tetromino touched a 
         * thing with its bottom surface.
         */
        private int touches;
        
        /**
         * Whether this tetromino is floating
         */
        private boolean floating;
        
        /**
         * The piece number dropped
         */
        private long pieceNo;

        /**
         * Creates a new LockDelay.
         */
        public LockDelay() {
            pieceNo = 0;
            touches = 0;
            floating = (falling == null) ? false 
                    : !falling.overlaps(miniMatrix(0, -1));
        }

        @Override
        public void run() {
            try {
                if(falling != null) {
                    if (falling.overlaps(miniMatrix(0, -1)) && floating) {
                        floating = false;
                        touches++;
                        Executors.newScheduledThreadPool(1).schedule(
                                new LockDelayChecker(touches, pieceNo),
                                500, TimeUnit.MILLISECONDS);
                    }
                    if (!falling.overlaps(miniMatrix(0, -1)) && !floating) {
                        floating = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        /**
         * Resets this LockDelay so that checks are not run for this piece anymore.
         */
        public void reset() {
            touches = 0;
            floating = !falling.overlaps(miniMatrix(0, -1));
            pieceNo++;
        }
        
        /**
         * Adds a touch.
         */
        public void addTouch() {
            touches++;
            Executors.newScheduledThreadPool(1).schedule(
                    new LockDelayChecker(touches, pieceNo), 
                    500, TimeUnit.MILLISECONDS);
        }
        
        /**
         * Checks if the falling tetromino should be locked
         */
        private class LockDelayChecker implements Runnable {
            /**
             * The number of touches the tetromino had at that moment
             */
            private final int touches_;
            
            /**
             * The piece id of the piece that this is detecting
             */
            private final long pieceNo_;

            /**
             * Creates a new LockDelayChecker
             * @param touches 
             */
            public LockDelayChecker(int touches, long pieceNo) {
                touches_ = touches;
                pieceNo_ = pieceNo;
            }
            
            @Override
            public void run() {
                if(!floating && touches_ == touches && pieceNo_ == pieceNo) {
                    lockPiece();
                }
            }
        }
    }
    
    /**
     * Locks the currently falling piece to the given coordinates
     * @param x the x-coordinate to lock to
     * @param y the y-coordinate to lock to
     */
    public void lockFalling(int x, int y) {
        this.x = x;
        this.y = y;
        lockPiece();
    }
    
    /**
     * Adds a bag of tetrominos
     * @param bag the bag of tetrominos to add
     */
    public void addBag(String bag) {
        this.bag.addBag(bag);
    }
}
