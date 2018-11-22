package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import javafx.scene.media.Media;
import javax.imageio.ImageIO;

/**
 * A class that deals with creating files with resources.
 * @author Jed Wang
 */
public class Resources {
    /**
     * Al methods are public and static; no need for this.
     */
    private Resources() {}
    
    /**
     * Finds and returns the file at the given path
     * @param path the path of the File to get
     * @return the File
     * @throws URISyntaxException if the URI syntax isn't right
     */
    public static File getFile(String path) throws URISyntaxException {
        URL resource = Resources.class.getResource(path);
        return new File(resource.toURI());
    }
    
    /**
     * Finds and returns the image at the given path. 
     * Shoutouts to <code>Hovercraft Full of Eels</code> for this code.
     * @param path the path of the Image to get
     * @return the Image
     * @throws IOException if something goes wrong
     */
    public static BufferedImage getImage(String path) throws IOException {
        InputStream imgStream = Resources.class.getResourceAsStream(path);
        return ImageIO.read(imgStream);
    }
    
    /**
     * Finds and returns the media (music file) at the given path.
     * @param path the path of the media to get
     * @return the Media
     * @throws URISyntaxException if the URI syntax isn't right
     */
    public static Media getMedia(String path) throws URISyntaxException {
        System.out.println(new File(path).exists());
        URL resource = Resources.
                class.
                getResource(path);
        System.out.println(resource);
        return new Media(resource.toURI().toString());
    }
}