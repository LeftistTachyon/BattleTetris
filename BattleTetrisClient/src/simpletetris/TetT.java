package simpletetris;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * A class that represents the T tetromino
 * @author Jed Wang, Danny Tang
 */
public class TetT extends Tetromino {
    /**
     * A mini TetT
     */
    private static final BufferedImage MINI;
    
    static {
        BufferedImage temp = null;
        try {
            temp = ImageIO.read(new File("images/miniT.png"));
        } catch (IOException ex) {
            System.err.println("miniT.png cannot be found");
        }
        MINI = temp;
    }
    
    /**
     * Shorthand
     */
    private static final Color P = Color.MAGENTA;
    @Override
    public Color[][] getUp() {
        return new Color[][]{{null, P,    null}, 
                             {P,    P,    null}, 
                             {null, P,    null}};
    }

    @Override
    public Color[][] getLeft() {
        return new Color[][]{{null, P,    null}, 
                             {P,    P,    P}, 
                             {null, null, null}};
    }

    @Override
    public Color[][] getDown() {
        return new Color[][]{{null, P,    null}, 
                             {null, P,    P}, 
                             {null, P,    null}};
    }

    @Override
    public Color[][] getRight() {
        return new Color[][]{{null, null, null}, 
                             {P,    P,    P}, 
                             {null, P,    null}};
    }

    @Override
    public int getRotationBoxWidth() {
        return 3;
    }

    @Override
    public Tetromino copy() {
        TetT t = new TetT();
        t.rotation = rotation;
        return t;
    }

    @Override
    public boolean sameTetromino(Tetromino t) {
        return t instanceof TetT;
    }

    @Override
    public Color getColor() {
        return new Color(146, 23, 156);
    }

    @Override
    public BufferedImage getMiniImage() {
        return MINI;
    }

    @Override
    public String getShape() {
        return "T";
    }
}