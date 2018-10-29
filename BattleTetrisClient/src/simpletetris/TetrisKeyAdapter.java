package simpletetris;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;
import java.util.HashMap;
import static simpletetris.TetrisKeyAdapter.GameAction.*;

/**
 * A MouseAdapter which listens in to key presses.
 * @author Jed Wang
 */
public class TetrisKeyAdapter extends KeyAdapter {
    /**
     * The matrix to report to.
     */
    private TetrisMatrix matrix;
    
    /**
     * Stores whether a key is pressed
     */
    private volatile HashMap<GameAction, Boolean> pressed;
    
    /**
     * Creates a new TetrisMouseAdapter.
     * @param matrix the TetrisMatrix to report to.
     */
    public TetrisKeyAdapter(TetrisMatrix matrix) {
        this.matrix = matrix;
        
        pressed = new HashMap<>();
        for(GameAction value : values()) {
            pressed.put(value, false);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case VK_LEFT:
            case VK_NUMPAD4:
                if(pressed.get(MOVE_LEFT))
                    break;
                pressed.put(MOVE_LEFT, true);
                DASaction(MOVE_LEFT);
                break;
            case VK_RIGHT:
            case VK_NUMPAD6:
                if(pressed.get(MOVE_RIGHT))
                    break;
                pressed.put(MOVE_RIGHT, true);
                DASaction(MOVE_RIGHT);
                break;
            case VK_SPACE:
            case VK_NUMPAD8:
                if(pressed.get(HARD_DROP))
                    break;
                pressed.put(HARD_DROP, true);
                executeAction(HARD_DROP);
                // notifyListener("M" + HARD_DROP.shorthand);
                break;
            case VK_DOWN:
            case VK_NUMPAD2:
                if(pressed.get(SOFT_DROP))
                    break;
                pressed.put(SOFT_DROP, true);
                DASaction(SOFT_DROP);
                break;
            case VK_UP:
            case VK_X:
            case VK_NUMPAD1:
            case VK_NUMPAD5:
            case VK_NUMPAD9:
                if(pressed.get(ROTATE_RIGHT))
                    break;
                executeAction(ROTATE_RIGHT);
                // notifyListener("M" + ROTATE_RIGHT.shorthand);
                break;
            case VK_CONTROL:
            case VK_Z:
            case VK_NUMPAD3:
            case VK_NUMPAD7:
                if(pressed.get(ROTATE_LEFT))
                    break;
                pressed.put(ROTATE_LEFT, true);
                executeAction(ROTATE_LEFT);
                // notifyListener("M" + ROTATE_LEFT.shorthand);
                break;
            case VK_SHIFT:
            case VK_C:
            case VK_NUMPAD0:
                if(pressed.get(HOLD))
                    break;
                pressed.put(HOLD, true);
                executeAction(HOLD);
                // notifyListener("M" + HOLD.shorthand);
                break;
            /*case VK_ESCAPE:
            case VK_F1:
            case VK_P:
                if(pressed.get(PAUSE))
                    break;
                pressed.put(PAUSE, true);
                matrix.executeAction(PAUSE);
                break;*/
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()) {
            case VK_LEFT:
            case VK_NUMPAD4:
                pressed.put(MOVE_LEFT, false);
                break;
            case VK_RIGHT:
            case VK_NUMPAD6:
                pressed.put(MOVE_RIGHT, false);
                break;
            case VK_SPACE:
            case VK_NUMPAD8:
                pressed.put(HARD_DROP, false);
            case VK_DOWN:
            case VK_NUMPAD2:
                pressed.put(SOFT_DROP, false);
                break;
            case VK_UP:
            case VK_X:
            case VK_NUMPAD1:
            case VK_NUMPAD5:
            case VK_NUMPAD9:
                pressed.put(ROTATE_RIGHT, false);
                break;
            case VK_CONTROL:
            case VK_Z:
            case VK_NUMPAD3:
            case VK_NUMPAD7:
                pressed.put(ROTATE_LEFT, false);
                break;
            case VK_SHIFT:
            case VK_C:
            case VK_NUMPAD0:
                pressed.put(HOLD, false);
                break;
            /*case VK_ESCAPE:
            case VK_F1:
            case VK_P:
                pressed.put(PAUSE, false);
                break;*/
        }
    }
    
    /**
     * DAS-es an action
     * @param action the action to DAS
     */
    private void DASaction(GameAction action) {
        new Thread(new DASer(action)).start();
    }
    
    /**
     * counter
    */
    private static int cnt = 0;
    
    /**
     * A class that handles DAS-ing: the delay as well as the repetition.
     * @see http://tetris.wikia.com/wiki/DAS
     */
    private class DASer implements Runnable {
        /**
         * The action to execute
         */
        private GameAction toExecute;

        /**
         * Creates a DASer and gives it an action to DAS
         * @param toExecute the action to DAS
         */
        public DASer(GameAction toExecute) {
            this.toExecute = toExecute;
        }
        
        @Override
        public void run() {
            cnt++;
            executeAction(toExecute);
            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                System.err.println("DAS thread interrupted.");
            }
            
            matrix.pauseGravity();
            while(pressed.get(toExecute)) {
                if(cnt > 1) {
                    cnt--;
                    return;
                }
                executeAction(toExecute);
                try {
                    Thread.sleep(35);
                } catch (InterruptedException ex) {
                    System.err.println("DAS thread interrupted.");
                }
            }
            matrix.resumeGravity();
            cnt--;
        }
    }
    
    /**
     * Whether this TKA is listening to the keyboard or not
     */
    private boolean listening = false;

    /**
     * Returns whether this TKA is listening to the keyboard or not
     * @return whether this TKA is listening to the keyboard or not
     */
    public boolean isListening() {
        return listening;
    }

    /**
     * Sets whether this TKA is listening to the keyboard or not
     * @param listening whether you want this TKA is listening 
     * to the keyboard or not
     */
    public void setListening(boolean listening) {
        this.listening = listening;
    }
    
    /**
     * Executes an action and notifies the listener that the action 
     * has been executed.
     * @param ga the action to execute
     */
    private void executeAction(GameAction ga) {
        if(listening) {
            notifyListener("M" + ga.shorthand);
            matrix.executeAction(ga);
        }
    }
    
    /**
     * The listener which islistening in
     */
    private ActionListener listener = null;
    
    /**
     * Sets the current ActionListener
     * @param al the ActionListener to set to
     */
    public void setActionListener(ActionListener al) {
        listener = al;
    }
    
    /**
     * Removes the listener which is listening to this TetrisBag.
     */
    public void removeActionListener() {
        listener = null;
        }
    
    /**
     * Notifies the listener that an event occured.
     * @param message the message to send
     */
    private void notifyListener(String message) {
        if(listener != null)
            listener.actionPerformed(new ActionEvent(this, 0, message));
    }
    
    /**
     * All possible game actions
     */
    public static enum GameAction {
        /**
         * Moves the active tetromino leftwards
         */
        MOVE_LEFT("L"), 

        /**
         * Moves the active tetromino rightwards
         */
        MOVE_RIGHT("R"), 

        /**
         * Hard drops the active tetromino
         */
        HARD_DROP("HD"), 

        /**
         * Drops the active tetromino down by 1
         */
        SOFT_DROP("SD"), 

        /**
         * Rotates the active tetromino clockwise
         */
        ROTATE_RIGHT("RR"),

        /**
         * Rotates the active tetromino counterclockwise
         */
        ROTATE_LEFT("RL"),

        /**
         * Switches the held tetromino with the currently active one
         */
        HOLD("H"),

        /**
         * Refers to when the active tetromino falls down due to gravity
         */
        GRAVITY("G");
        
        /**
         * Shorthand notation for this move
         */
        private final String shorthand;

        /**.
         * Instantiates a GameAction.
         * @param shorthand the shorthand notation for this action.
         */
        private GameAction(String shorthand) {
            this.shorthand = shorthand;
        }

        /**
         * Returns the shorthand notation for this move
         * @return the shorthand notation for this move
         */
        public String getShorthand() {
            return shorthand;
        }
        
        /**
         * Determines the GameAction referred to by the String and returns it.
         * If the given String doesn't match a GameAction, null is returned.
         * @param sh the String that represents the wanted GameAction
         * @return the GameAction that is represented by the given String
         */
        public static GameAction fromShorthand(String sh) {
            for(GameAction ga : values()) {
                if(ga.getShorthand().equals(sh)) {
                    return ga;
                }
            }
            return null;
        }
    }
}
