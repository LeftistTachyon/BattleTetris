package simpletetris;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * Creates a new Tetromino
 * @author Danny Tang, Jed Wang, Grace Liu
 */
public abstract class Tetromino {
    /**
     * The rotation of the piece
     */
    protected int rotation;
    
    /**
     * Keeps track of the number of rotations this tetromino has performed.
     * A max of 15. After that, rotation lock.
     */
    protected int rotations = 15;
    
    /**
     * The "up" rotation state
     */
    public static final int UP = 0;
    
    /**
     * The "left" rotation state
     */
    public static final int LEFT = 1;
    
    /**
     * The "down" rotation state
     */
    public static final int DOWN = 2;
    
    /**
     * The "right" rotation state
     */
    public static final int RIGHT = 3;
    
    /**
     * An int which represents clockwise rotation
     */
    public static final int CLOCKWISE = 10;
    
    /**
     * An int which represents counterclockwise rotation
     */
    public static final int COUNTERCLOCKWISE = 11;
    
    /**
     * Creates a new Tetromino.
     */
    public Tetromino(){
        rotation = 0;
    }
    
    /**
     * Creates a new Tetromino with a given rotation state
     * @param rotation the rotation state to create a new Tetromino with
     */
    public Tetromino(int rotation) {
        this.rotation = rotation;
    }
    
    /**
     * Returns the up state of the tetromino
     * @return the up state 
     */
    public abstract Color[][] getUp();
    
    /**
     * Returns the left state of the tetromino
     * @return the left state 
     */
    public abstract Color[][] getLeft();
    
    /**
     * Returns the down state of the tetromino
     * @return the down state 
     */
    public abstract Color[][] getDown();
    
    /**
     * Returns the right state of the tetromino
     * @return the right state 
     */
    public abstract Color[][] getRight();
    
    /**
     * Determines the width of the rotation box
     * @return the width of the rotation box
     */
    public abstract int getRotationBoxWidth();
    
    /**
     * Determines whether this tetromino overlaps any of the colors 
     * of the matrix.
     * @param area the area given: always rotationBoxWidth by rotationBoxWidth
     * @return whether this tetromino overlaps any of the colors of the matrix
     */
    public boolean overlaps(Color[][] area) {
        Color[][] drawBox = getDrawBox();
        for(int i = 0; i < area.length; i++) {
            for(int j = 0; j < area[i].length; j++) {
                if(area[i][j] != null && drawBox[i][j] != null)
                    return true;
            }
        }
        return false;
    }
    
    /**
     * Determines the wall kick for rotating this tetromino
     * @param tm the matrix this piece is in
     * @param direction the direction of rotation
     * @return the wall kick (the x coordinate is the x component of the kick 
     * and the y coordinate is the y component of the kick)
     */
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
        switch(rotation) {
            case UP:
                switch(direction) {
                    case CLOCKWISE:
                        // (-1, 0) (-1,+1) ( 0,-2) (-1,-2)
                        if(!copy.overlaps(tm.miniMatrix(-1, 0)))
                            return new Point(-1, 0);
                        if(!copy.overlaps(tm.miniMatrix(-1, 1)))
                            return new Point(-1, 1);
                        if(!copy.overlaps(tm.miniMatrix(0, -2)))
                            return new Point(0, -2);
                        if(!copy.overlaps(tm.miniMatrix(-1, -2)))
                            return new Point(-1, -2);
                        break;
                    case COUNTERCLOCKWISE:
                        // (+1, 0) (+1,+1) ( 0,-2) (+1,-2)
                        if(!copy.overlaps(tm.miniMatrix(1, 0)))
                            return new Point(1, 0);
                        if(!copy.overlaps(tm.miniMatrix(1, 1)))
                            return new Point(1, 1);
                        if(!copy.overlaps(tm.miniMatrix(0, -2)))
                            return new Point(0, -2);
                        if(!copy.overlaps(tm.miniMatrix(1, -2)))
                            return new Point(1, -2);
                        break;
                    default:
                        throw new IllegalStateException("Illegal rotation");
                }
                break;
            case RIGHT:
                switch(direction) {
                    case COUNTERCLOCKWISE:
                        // (+1, 0) (+1,-1) ( 0,+2) (+1,+2)
                        if(!copy.overlaps(tm.miniMatrix(1, 0)))
                            return new Point(1, 0);
                        if(!copy.overlaps(tm.miniMatrix(1, -1)))
                            return new Point(1, -1);
                        if(!copy.overlaps(tm.miniMatrix(0, 2)))
                            return new Point(0, 2);
                        if(!copy.overlaps(tm.miniMatrix(1, 2)))
                            return new Point(1, 2);
                        break;
                    case CLOCKWISE:
                        // (+1, 0) (+1,-1) ( 0,+2) (+1,+2)
                        if(!copy.overlaps(tm.miniMatrix(1, 0)))
                            return new Point(1, 0);
                        if(!copy.overlaps(tm.miniMatrix(1, -1)))
                            return new Point(1, -1);
                        if(!copy.overlaps(tm.miniMatrix(0, 2)))
                            return new Point(0, 2);
                        if(!copy.overlaps(tm.miniMatrix(1, 2)))
                            return new Point(1, 2);
                        break;
                    default:
                        throw new IllegalStateException("Illegal rotation");
                }
                break;
            case DOWN:
                switch(direction) {
                    case COUNTERCLOCKWISE:
                        // (-1, 0) (-1,+1) ( 0,-2) (-1,-2)
                        if(!copy.overlaps(tm.miniMatrix(-1, 0)))
                            return new Point(-1, 0);
                        if(!copy.overlaps(tm.miniMatrix(-1, 1)))
                            return new Point(-1, 1);
                        if(!copy.overlaps(tm.miniMatrix(0, -2)))
                            return new Point(0, -2);
                        if(!copy.overlaps(tm.miniMatrix(-1, -2)))
                            return new Point(-1, -2);
                        break;
                    case CLOCKWISE:
                        // (+1, 0) (+1,+1) ( 0,-2) (+1,-2)
                        if(!copy.overlaps(tm.miniMatrix(1, 0)))
                            return new Point(1, 0);
                        if(!copy.overlaps(tm.miniMatrix(1, 1)))
                            return new Point(1, 1);
                        if(!copy.overlaps(tm.miniMatrix(0, -2)))
                            return new Point(0, -2);
                        if(!copy.overlaps(tm.miniMatrix(1, -2)))
                            return new Point(1, -2);
                        break;
                    default:
                        throw new IllegalStateException("Illegal rotation");
                }
                break;
            case LEFT:
                switch(direction) {
                    case CLOCKWISE:
                        // (-1, 0) (-1,-1) ( 0,+2) (-1,+2)
                        if(!copy.overlaps(tm.miniMatrix(-1, 0)))
                            return new Point(-1, 0);
                        if(!copy.overlaps(tm.miniMatrix(-1, -1)))
                            return new Point(-1, -1);
                        if(!copy.overlaps(tm.miniMatrix(0, 2)))
                            return new Point(0, 2);
                        if(!copy.overlaps(tm.miniMatrix(-1, 2)))
                            return new Point(-1, 2);
                        break;
                    case COUNTERCLOCKWISE:
                        // (-1, 0) (-1,-1) ( 0,+2) (-1,+2)
                        if(!copy.overlaps(tm.miniMatrix(-1, 0)))
                            return new Point(-1, 0);
                        if(!copy.overlaps(tm.miniMatrix(-1, -1)))
                            return new Point(-1, -1);
                        if(!copy.overlaps(tm.miniMatrix(0, 2)))
                            return new Point(0, 2);
                        if(!copy.overlaps(tm.miniMatrix(-1, 2)))
                            return new Point(-1, 2);
                        break;
                    default:
                        throw new IllegalStateException("Illegal rotation");
                }
                break;
            default:
                throw new IllegalStateException("rotation should not be the "
                        + "value" + rotation);
        }
        
        /*throw new IllegalStateException("No transformations worked. "
                + "Rotation: " + rotation 
                + " in direction " + direction + " with " + rotations 
                + " remaining rotations");*/
        
        return null; // no rotation
    }
    
    /**
     * Returns the minos to draw
     * @return the minos to draw
     */
    public Color[][] getDrawBox() {
        switch(rotation) {
            case UP:
                return getUp();
            case RIGHT:
                return getRight();
            case LEFT:
                return getLeft();
            case DOWN:
                return getDown();
        }
        throw new IllegalStateException("rotation has an illegal value: " 
                + rotation);
    }
    
    /**
     * Rotates this tetromino clockwise.
     */
    public void rotateRight() {
        if(rotations == 0) 
            return;
        
        rotation--;
        if(rotation == -1) 
            rotation = 3;
        
        rotations--;
    }
    
    /**
     * Rotates this tetromino counterclockwise.
     */
    public void rotateLeft() {
        if(rotations == 0)
            return;
        
        rotation++;
        if(rotation == 4)
            rotation = 0;
        
        rotations--;
    }
    
    /**
     * Rotates this tetromino to the given rotation state<br>
     * NOTE: this does NOT decrement the rotations counter
     * @param rotationState the state to rotate to
     */
    public void rotateTo(int rotationState) {
        if(rotationState < 0 || rotationState > 3) 
            throw new IllegalArgumentException("Invalid rotation state");
        rotation = rotationState;
    }
    
    /**
     * Resets the rotation counter to rotate 15 more times.
     */
    public void resetRotationCount() {
        rotations = 15;
    }
    
    /**
     * Returns a copy of this tetromino.
     * @return a copy of this tetromino
     */
    public abstract Tetromino copy();
    
    /**
     * Determines whether the given tetromino is the same as this one.
     * @param t the tetromino to compare to
     * @return whether the given tetromino is the same
     */
    public abstract boolean sameTetromino(Tetromino t);
    
    /**
     * Returns the color of this tetromino
     * @return the color of this tetromino
     */
    public abstract Color getColor();
    
    /**
     * Returns a mini-image or preview of the piece<br>
     * The aspect ratio of the piece is 5 x 3.
     * @return an image preview of this piece
     */
    public abstract BufferedImage getMiniImage();
    
    /**
     * Returns a String representation of the shape of this tetromino.
     * @return the String representation of this tetromino's shape.
     */
    public abstract String getShape();

    @Override
    public String toString() {
        String rotationString;
        switch(rotation) {
            case DOWN:
                rotationString = "DOWN";
                break;
            case UP:
                rotationString = "UP";
                break;
            case LEFT:
                rotationString = "LEFT";
                break;
            case RIGHT:
                rotationString = "RIGHT";
                break;
            default:
                rotationString = "NA";
                break;
        }
        return "[Tet" + getShape() + ", rotation: " + rotationString + "]";
    }
}
