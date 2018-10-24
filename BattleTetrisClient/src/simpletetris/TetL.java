package simpletetris;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * A class that represents the L tetromino
 * @author Jed Wang
 */
public class TetL extends Tetromino {
    /**
     * A mini TetL
     */
    private static final BufferedImage MINI;
    
    static {
        BufferedImage temp = null;
        try {
            temp = ImageIO.read(new File("images/miniL.png"));
        } catch (IOException ex) {
            System.err.println("miniL.png cannot be found");
        }
        MINI = temp;
    }
    
    /**
     * Shorthand
     */
    private static final Color O = Color.ORANGE;

    @Override
    public Color[][] getUp() {
        return new Color[][]{{null, O,    null}, 
                             {null, O,    null}, 
                             {O,    O,    null}};
    }

    @Override
    public Color[][] getLeft() {
        return new Color[][]{{O,    null, null}, 
                             {O,    O,    O}, 
                             {null, null, null}};
    }

    @Override
    public Color[][] getDown() {
        return new Color[][]{{null, O,    O}, 
                             {null, O,    null}, 
                             {null, O,    null}};
    }

    @Override
    public Color[][] getRight() {
        return new Color[][]{{null, null, null}, 
                             {O,    O,    O}, 
                             {null, null, O}};
    }

    @Override
    public int getRotationBoxWidth() {
        return 3;
    }

    @Override
    public Tetromino copy() {
        TetL l = new TetL();
        l.rotation = rotation;
        return l;
    }

    @Override
    public boolean sameTetromino(Tetromino t) {
        return t instanceof TetL;
    }

    @Override
    public Color getColor() {
        return new Color(234, 101, 22);
    }

    @Override
    public BufferedImage getMiniImage() {
        return MINI;
    }

    @Override
    public String getShape() {
        return "L";
    }
}
