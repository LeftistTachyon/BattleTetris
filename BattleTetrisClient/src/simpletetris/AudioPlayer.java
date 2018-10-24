package simpletetris;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.swing.SwingUtilities;

/**
 * A class that plays audio on command
 * @author Jed Wang
 */
public class AudioPlayer {
    /**
     * Standard music during gameplay.<br>
     * Taken from Super Mario World 2: Yoshi's Island.
     * Composed by Koji Kondo
     */
    private static final Media FLOWER_GARDEN;
    
    /**
     * Standard music during gameplay.<br>
     * Taken from Super Mario World 2: Yoshi's Island.
     * Composed by Koji Kondo
     */
    private static final Media ATHLETIC;
    
    /**
     * Standard music during gameplay.<br>
     * Taken from Super Mario World 2: Yoshi's Island.
     * Composed by Koji Kondo
     */
    private static final Media OVERWORLD;
    
    /**
     * A SFX that plays when garbage is recieved.<br>
     * Taken from Super Mario World.
     */
    private static final Media GARBAGE_ADDED;
    
    /**
     * SFX's that play that denote a combo.<br>
     * Taken from Super Mario World.
     */
    private static final Media[] COMBO;
    
    /**
     * SFX's that play that denote a t-spin.<br>
     * Taken from Super Mario World.
     */
    private static final Media[] T_SPIN;
    
    /**
     * SFX that plays when the player executes an all-clear.<br>
     * Taken from Super Mario World.
     */
    private static final Media ALL_CLEAR;
    
    /**
     * SFX that plays when the player loses a game.<br>
     * Taken from Super Mario World.
     */
    private static final Media LOSE_GAME;
    
    /**
     * SFX that plays when the player loses a matchup.<br>
     * Taken from Super Mario World.
     */
    private static final Media LOSE_MATCH;
    
    /**
     * SFX that plays when the player wins a game.<br>
     * Taken from Super Mario World.
     */
    private static final Media WIN_GAME;
    
    /**
     * SFX that plays when the player wins a matchup.<br>
     * Taken from Super Mario World.
     */
    private static final Media WIN_MATCH;
    
    /**
     * SFX that plays when the player moves the falling tetromino.<br>
     * Taken from Super Mario World.
     */
    private static final Media MOVE;
    
    /**
     * The MediaPlayer which controls playing background music.
     */
    private static MediaPlayer backgroundPlayer;
    
    /**
     * The MediaPlayer which controls playing SFX.
     */
    private static MediaPlayer sfxPlayer;
    
    /**
     * Stores whether JavaFX has been initialized.
     */
    private static boolean initialized = false;
    
    static {
        FLOWER_GARDEN = new Media(
                new File("music/flowergarden.m4a").toURI().toString());
        
        ATHLETIC = new Media(
                new File("music/athletic.m4a").toURI().toString());
        
        OVERWORLD = new Media(
                new File("music/overworld.m4a").toURI().toString());
        
        
        GARBAGE_ADDED = new Media(
                new File("sfx/addgarbage.m4a").toURI().toString());
        
        COMBO = new Media[8];
        for(int i = 0; i < COMBO.length; i++) {
            COMBO[i] = new Media(
                    new File("sfx/combo" + (i+1) + ".m4a").toURI().toString());
        }
        
        T_SPIN = new Media[4];
        for(int i = 0; i < T_SPIN.length; i++) {
            T_SPIN[i] = new Media(
                    new File("sfx/tspin" + i + ".m4a").toURI().toString());
        }
        
        ALL_CLEAR = new Media(
                new File("sfx/allClear.m4a").toURI().toString());
        
        LOSE_GAME = new Media(new File("sfx/lose.m4a").toURI().toString());
        
        LOSE_MATCH = new Media(new File("sfx/loseM.m4a").toURI().toString());
        
        WIN_GAME = new Media(new File("sfx/win.m4a").toURI().toString());
        
        WIN_MATCH = new Media(new File("sfx/winM.m4a").toURI().toString());
        
        MOVE = new Media(new File("sfx/move.wav").toURI().toString());
    }
    
    /**
     * Just execute static methods.
     */
    private AudioPlayer() {}
    
    /**
     * Plays a random background music allocated for in-game.
     */
    public static void playInGameBackground() {
        if(!initialized) initialize();
        
        if(backgroundPlayer != null) 
            backgroundPlayer.stop();
        
        int r = (int) (Math.random() * 3);
        switch(r) {
            case 0:
                backgroundPlayer = new MediaPlayer(ATHLETIC);
                break;
            case 1:
                backgroundPlayer = new MediaPlayer(FLOWER_GARDEN);
                break;
            case 2:
                backgroundPlayer = new MediaPlayer(OVERWORLD);
                break;
        }
        backgroundPlayer.setVolume(0.75);
        backgroundPlayer.play();
    }
    
    /**
     * Stops the background music.
     */
    public static void stopBackgroundMusic() {
        if(backgroundPlayer != null) 
            backgroundPlayer.stop();
    }
    
    /**
     * Plays the garbage added SFX.
     */
    public static void playGarbageSFX() {
        if(!initialized) initialize();
        
        sfxPlayer = new MediaPlayer(GARBAGE_ADDED);
        sfxPlayer.play();
    }
    
    /**
     * Plays the combo SFX.
     * @param combo which combo sound to play
     */
    public static void playComboSFX(int combo) {
        if(!initialized) initialize();
        
        sfxPlayer = new MediaPlayer(COMBO[Math.min(combo, COMBO.length - 1)]);
        sfxPlayer.play();
    }
    
    /**
     * Plays the t-spin SFX.
     * @param lines how many lines cleared with the t-spin
     */
    public static void playTSpinSFX(int lines) {
        if(!initialized) initialize();
        
        sfxPlayer = new MediaPlayer(T_SPIN[Math.min(lines, T_SPIN.length - 1)]);
        sfxPlayer.play();
    }
    
    /**
     * Plays the all clear SFX.
     */
    public static void playAllClearSFX() {
        if(!initialized) initialize();
        
        sfxPlayer = new MediaPlayer(ALL_CLEAR);
        sfxPlayer.play();
    }
    
    /**
     * Plays the lose game SFX.
     */
    public static void playLoseGameSFX() {
        if(!initialized) initialize();
        
        sfxPlayer = new MediaPlayer(LOSE_GAME);
        sfxPlayer.play();
    }
    
    /**
     * Plays the lose match SFX.
     */
    public static void playLoseMatchSFX() {
        if(!initialized) initialize();
        
        sfxPlayer = new MediaPlayer(LOSE_MATCH);
        sfxPlayer.play();
    }
    
    /**
     * Plays the win game SFX.
     */
    public static void playWinGameSFX() {
        if(!initialized) initialize();
        
        sfxPlayer = new MediaPlayer(WIN_GAME);
        sfxPlayer.play();
    }
    
    /**
     * Plays the win match SFX.
     */
    public static void playWinMatchSFX() {
        if(!initialized) initialize();
        
        sfxPlayer = new MediaPlayer(WIN_MATCH);
        sfxPlayer.play();
    }
    
    /**
     * Plays the move SFX.
     * @param volume the volume of this SFX
     */
    public static void playMoveSFX(double volume) {
        if(!initialized) initialize();
        
        sfxPlayer = new MediaPlayer(MOVE);
        sfxPlayer.setVolume(volume);
        sfxPlayer.play();
    }
    
    /**
     * Initializes JavaFX.
     */
    private static void initialize() {
        try {
            SwingUtilities.invokeAndWait(JFXPanel::new);
        } catch (InterruptedException ex) {
            System.err.println("Initialization interrupted: " + ex.toString());
        } catch (InvocationTargetException ex) {
            System.err.println("Unable to create a new JFXPanel");
        }
        
        initialized = true;
    }
}