package simpletetris;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * A class that represents the Z tetromino
 * @author Jed Wang, Danny Tang
 */
public class TetZ extends Tetromino {
    /**
     * A mini TetZ
     */
    private static final BufferedImage MINI;
    
    static {
        BufferedImage temp = null;
        try {
            temp = ImageIO.read(new File("images/miniZ.png"));
        } catch (IOException ex) {
            System.err.println("miniZ.png cannot be found");
        }
        MINI = temp;
    }
    
    /**
     * Shorthand
     */
    private static final Color r = Color.RED;
    
    @Override
    public Color[][] getUp() {
        return new Color[][]{{r,    null, null}, 
                             {r,    r,    null}, 
                             {null, r,    null}};
    }

    @Override
    public Color[][] getLeft() {
        return new Color[][]{{null, r,    r}, 
                             {r,    r,    null}, 
                             {null, null, null}};
    }

    @Override
    public Color[][] getDown() {
        return new Color[][]{{null, r,    null}, 
                             {null, r,    r}, 
                             {null, null, r}};
    }

    @Override
    public Color[][] getRight() {
        return new Color[][]{{null, null, null}, 
                             {null, r,    r}, 
                             {r,    r,    null}};
    }

    @Override
    public int getRotationBoxWidth() {
        return 3;
    }

    @Override
    public Tetromino copy() {
        TetZ z = new TetZ();
        z.rotation = rotation;
        return z;
    }

    @Override
    public boolean sameTetromino(Tetromino t) {
        return t instanceof TetZ;
    }

    @Override
    public Color getColor() {
        return new Color(179, 20, 30);
    }

    @Override
    public BufferedImage getMiniImage() {
        return MINI;
    }

    @Override
    public String getShape() {
        return "Z";
    }
}