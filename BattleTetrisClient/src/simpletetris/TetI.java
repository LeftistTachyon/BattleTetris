package simpletetris;

import java.awt.Color;
import static java.awt.Color.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * A class that represents the I tetromino
 * @author Grace Liu, Jed Wang
 */
public class TetI extends Tetromino {
    /**
     * A mini TetI
     */
    private static final BufferedImage MINI;
    
    static {
        BufferedImage temp = null;
        try {
            temp = ImageIO.read(new File("images/miniI.png"));
        } catch (IOException ex) {
            System.err.println("miniI.png cannot be found");
        }
        MINI = temp;
    }
    
    @Override
    public Color[][] getUp() {
        return new Color[][]{{null, cyan, null, null}, 
                             {null, cyan, null, null}, 
                             {null, cyan, null, null}, 
                             {null, cyan, null, null}};
    }

    @Override
    public Color[][] getLeft() {
        return new Color[][]{{null, null, null, null}, 
                             {cyan, cyan, cyan, cyan},
                             {null, null, null, null}, 
                             {null, null, null, null}};
    }

    @Override
    public Color[][] getDown() {
        return new Color[][]{{null, null, cyan, null}, 
                             {null, null, cyan, null}, 
                             {null, null, cyan, null}, 
                             {null, null, cyan, null}};
    }

    @Override
    public Color[][] getRight() {
        return new Color[][]{{null, null, null, null}, 
                             {null, null, null, null}, 
                             {cyan, cyan, cyan, cyan},
                             {null, null, null, null}};
    }

    @Override
    public int getRotationBoxWidth() {
        return 4;
    }

    @Override
    public Point getWallKick(TetrisMatrix tm, int direction) {
        // Sanity check
        if(!sameTetromino(tm.getFallingPiece()))
            throw new IllegalStateException("Unable to determine wallkick");
        
        if(rotations == 0) return new Point(0, 0);
        
        // Check for wallkicks
        Tetromino copy = copy();
        switch(direction) {
            case CLOCKWISE:
                copy.rotateRight();
                break;
            case COUNTERCLOCKWISE:
                copy.rotateLeft();
                break;
            default:
                throw new IllegalStateException("Illegal rotation");
        }
        
        if(!copy.overlaps(tm.miniMatrix())) 
            return new Point(0, 0);
        
        // Manual checks: starting from test 2
        // I tetromino has its own set of kicks
        switch(rotation) {
            case UP:
                switch(direction) {
                    case CLOCKWISE:
                        // (-2, 0) (+1, 0) (-2,-1) (+1,+2)
                        if(!copy.overlaps(tm.miniMatrix(-2, 0)))
                            return new Point(-2, 0);
                        if(!copy.overlaps(tm.miniMatrix(1, 0)))
                            return new Point(1, 0);
                        if(!copy.overlaps(tm.miniMatrix(-2, -1)))
                            return new Point(-2, -1);
                        if(!copy.overlaps(tm.miniMatrix(1, 2)))
                            return new Point(1, 2);
                        break;
                    case COUNTERCLOCKWISE:
                        // (-1, 0) (+2, 0) (-1,+2) (+2,-1)
                        if(!copy.overlaps(tm.miniMatrix(-1, 0)))
                            return new Point(-1, 0);
                        if(!copy.overlaps(tm.miniMatrix(2, 0)))
                            return new Point(2, 0);
                        if(!copy.overlaps(tm.miniMatrix(-2, -1)))
                            return new Point(-2, -1);
                        if(!copy.overlaps(tm.miniMatrix(1, 2)))
                            return new Point(1, 2);
                        break;
                    default:
                        throw new IllegalStateException("Illegal rotation");
                }
                break;
            case RIGHT:
                switch(direction) {
                    case COUNTERCLOCKWISE:
                        // (+2, 0) (-1, 0) (+2,+1) (-1,-2)
                        if(!copy.overlaps(tm.miniMatrix(2, 0)))
                            return new Point(2, 0);
                        if(!copy.overlaps(tm.miniMatrix(-1, 0)))
                            return new Point(-1, 0);
                        if(!copy.overlaps(tm.miniMatrix(2, 1)))
                            return new Point(2, 1);
                        if(!copy.overlaps(tm.miniMatrix(-1, -2)))
                            return new Point(-1, -2);
                        break;
                    case CLOCKWISE:
                        // (-1, 0) (+2, 0) (-1,+2) (+2,-1)
                        if(!copy.overlaps(tm.miniMatrix(-1, 0)))
                            return new Point(-1, 0);
                        if(!copy.overlaps(tm.miniMatrix(2, 0)))
                            return new Point(2, 0);
                        if(!copy.overlaps(tm.miniMatrix(-1, 2)))
                            return new Point(-1, 2);
                        if(!copy.overlaps(tm.miniMatrix(2, -1)))
                            return new Point(2, -1);
                        break;
                    default:
                        throw new IllegalStateException("Illegal rotation");
                }
                break;
            case DOWN:
                switch(direction) {
                    case COUNTERCLOCKWISE:
                        // (+1, 0) (-2, 0) (+1,-2) (-2,+1)
                        if(!copy.overlaps(tm.miniMatrix(1, 0)))
                            return new Point(1, 0);
                        if(!copy.overlaps(tm.miniMatrix(-2, 0)))
                            return new Point(-2, 0);
                        if(!copy.overlaps(tm.miniMatrix(1, -2)))
                            return new Point(1, -2);
                        if(!copy.overlaps(tm.miniMatrix(-2, 1)))
                            return new Point(-2, 1);
                        break;
                    case CLOCKWISE:
                        // (+2, 0) (-1, 0) (+2,+1) (-1,-2)
                        if(!copy.overlaps(tm.miniMatrix(2, 0)))
                            return new Point(2, 0);
                        if(!copy.overlaps(tm.miniMatrix(-1, 0)))
                            return new Point(-1, 0);
                        if(!copy.overlaps(tm.miniMatrix(2, 1)))
                            return new Point(2, 1);
                        if(!copy.overlaps(tm.miniMatrix(-1, -2)))
                            return new Point(-1, -2);
                        break;
                    default:
                        throw new IllegalStateException("Illegal rotation");
                }
                break;
            case LEFT:
                switch(direction) {
                    case CLOCKWISE:
                        // (+1, 0) (-2, 0) (+1,-2) (-2,+1)
                        if(!copy.overlaps(tm.miniMatrix(1, 0)))
                            return new Point(1, 0);
                        if(!copy.overlaps(tm.miniMatrix(-2, 0)))
                            return new Point(-2, 0);
                        if(!copy.overlaps(tm.miniMatrix(1, -2)))
                            return new Point(1, -2);
                        if(!copy.overlaps(tm.miniMatrix(-2, 1)))
                            return new Point(-2, 1);
                        break;
                    case COUNTERCLOCKWISE:
                        // (-2, 0) (+1, 0) (-2,-1) (+1,+2)
                        if(!copy.overlaps(tm.miniMatrix(-2, 0)))
                            return new Point(-2, 0);
                        if(!copy.overlaps(tm.miniMatrix(1, 0)))
                            return new Point(1, 0);
                        if(!copy.overlaps(tm.miniMatrix(-2, -1)))
                            return new Point(-2, -1);
                        if(!copy.overlaps(tm.miniMatrix(1, 2)))
                            return new Point(1, 2);
                        break;
                    default:
                        throw new IllegalStateException("Illegal rotation");
                }
                break;
            default:
                throw new IllegalStateException("rotation should not be the "
                        + "value" + rotation);
        }
        
        return null; // no rotation
    }

    @Override
    public Tetromino copy() {
        TetI i = new TetI();
        i.rotation = rotation;
        return i;
    }

    @Override
    public boolean sameTetromino(Tetromino t) {
        return t instanceof TetI;
    }

    @Override
    public Color getColor() {
        return new Color(26, 172, 217);
    }

    @Override
    public BufferedImage getMiniImage() {
        return MINI;
    }

    @Override
    public String getShape() {
        return "I";
    }
}
