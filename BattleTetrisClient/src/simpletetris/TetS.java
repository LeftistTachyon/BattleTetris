package simpletetris;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * A class that represents the S tetromino
 * @author Jed Wang
 */
public class TetS extends Tetromino {
    /**
     * A mini TetS
     */
    private static final BufferedImage MINI;
    
    static {
        BufferedImage temp = null;
        try {
            temp = ImageIO.read(new File("images/miniS.png"));
        } catch (IOException ex) {
            System.err.println("miniS.png cannot be found");
        }
        MINI = temp;
    }
    
    /**
     * Shorthand
     */
    private static final Color G = Color.green;
    
    @Override
    public Color[][] getUp() {
        return new Color[][]{{null, G,    null}, 
                             {G,    G,    null}, 
                             {G,    null, null}};
    }

    @Override
    public Color[][] getLeft() {
        return new Color[][]{{G,    G,    null}, 
                             {null, G,    G}, 
                             {null, null, null}};
    }

    @Override
    public Color[][] getDown() {
        return new Color[][]{{null, null, G}, 
                             {null, G,    G}, 
                             {null, G,    null}};
    }

    @Override
    public Color[][] getRight() {
        return new Color[][]{{null, null, null}, 
                             {G,    G,    null}, 
                             {null, G,    G}};
    }

    @Override
    public int getRotationBoxWidth() {
        return 3;
    }

    @Override
    public Tetromino copy() {
        TetS s = new TetS();
        s.rotation = rotation;
        return s;
    }

    @Override
    public boolean sameTetromino(Tetromino t) {
        return t instanceof TetS;
    }

    @Override
    public Color getColor() {
        return new Color(106, 206, 46);
    }

    @Override
    public BufferedImage getMiniImage() {
        return MINI;
    }

    @Override
    public String getShape() {
        return "S";
    }
}