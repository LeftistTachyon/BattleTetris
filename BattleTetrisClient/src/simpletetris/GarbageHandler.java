package simpletetris;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * A class that deals with incoming and outgoing garbage
 * @author Jed Wang
 */
public class GarbageHandler {
    /**
     * The queue for garbage
     */
    private Deque<Integer> garbageQueue;
    
    /**
     * The number of lines pending in the queue
     */
    private int linesToRecieve;
    
    /**
     * The lines sent
     */
    private int linesSent;
    
    /**
     * Keeps track of combos
     */
    private int combo;
    
    /**
     * Whether the player is doing powerful moves back to back
     */
    private boolean b2b;
    
    /**
     * A collection of listeners which are listening to this ScoreKeeper.
     */
    private ArrayList<ActionListener> listeners = null;
    
    /**
     * A normal lines clear
     */
    public static final int NORMAL = 0;
    
    /**
     * A standard t-spin
     */
    public static final int T_SPIN = 1;
    
    /**
     * A t-spin mini
     */
    public static final int T_SPIN_MINI = 2;
    
    /**
     * Creates a new GarbageHandler.
     */
    public GarbageHandler() {
        garbageQueue = new LinkedList<>();
        linesToRecieve = 0;
        
        linesSent = 0;
        
        combo = 0;
        b2b = false;
    }
    
    /**
     * Adds garbage to the queue
     * @param lines a String representation of the lines sent
     */
    public void addGarbage(String lines) {
        Scanner adder = new Scanner(lines);
        while(adder.hasNextInt()) {
            int next = adder.nextInt();
            garbageQueue.add(next);
            linesToRecieve += next;
        }
    }
    
    /**
     * Counters the incoming garbage with this garbage
     * @param counterLines a String representation of the lines to send
     * @return a String representation of the lines to send to the opponent<br>
     * Returns null if no garbage is sent to the opponent
     */
    private String counterGarbage(String counterLines) {
        if(garbageQueue.isEmpty()) {
            return counterLines;
        }
        
        Scanner adder = new Scanner(counterLines);
        while(adder.hasNextInt()) {
            int counter = -adder.nextInt();
            linesToRecieve += counter;
            while(counter < 0 && !garbageQueue.isEmpty()) {
                counter += garbageQueue.removeFirst();
            }
            if(counter > 0) garbageQueue.addFirst(counter);
        }
        
        if(adder.hasNextInt()) {
            String output = "";
            while(adder.hasNextInt()) {
                output += adder.nextInt() + " ";
            }
            System.out.println(output.length());
            return output.trim();
        } else return null;
    }
    
    /**
     * Returns the next chunk of garbage to add to the bottom and removes it
     * @return the next chunk of garbage to add to the bottom<br>
     * Returns 0 if the queue is empty
     */
    public int getNextGarbage() {
        Integer next = garbageQueue.pollFirst();
        int output = (next == null)?0:next;
        linesToRecieve -= output;
        return output;
    }
    
    /**
     * Returns the next chunk of garbage to add to the bottom
     * @return the next chunk of garbage to add to the bottom<br>
     * Returns 0 if the queue is empty
     */
    public int peekNextGarbage() {
        Integer next = garbageQueue.peekFirst();
        return (next == null)?0:next;
    }
    
    /**
     * Returns whether there is garbage queued up
     * @return whether there is garbage queued up
     */
    public boolean hasGarbage() {
        return !garbageQueue.isEmpty();
    }

    @Override
    public String toString() {
        return garbageQueue.toString();
    }
    
    /**
     * Determines how the incoming garbage bar should be filled
     * @return how the incoming garbage bar should be filled
     */
    public int[] getInBarFill() {
        if(linesToRecieve == 0) return null;
        
        int[] output = new int[20];
        for(int i = 0; i < output.length; i++) {
            output[i] = linesToRecieve/20;
        }

        int leftovers = linesToRecieve%20;
        for(int i = output.length-1; i >= output.length - leftovers; i--) {
            output[i]++;
        }

        return output;
    }
    
    /**
     * Notifies this ScoreKeeper that new Lines have been cleared.
     * @param linesCleared how many new lines have been cleared
     * @param clearType what type of line clear (e.g. standard, t-spin, 
     * t-spin mini)
     * @param perfectClear whether the move resulted in a perfect clear
     */
    public void newLinesCleared(int linesCleared, int clearType, 
            boolean perfectClear) {
        if(linesCleared == 0) {
            combo = 0;
            
            return;
        }
        
        if(linesCleared > 4 || linesCleared < 0) 
            throw new IllegalArgumentException("You cleared more than "
                    + "4 lines at once or less than 0 lines.");
        boolean bb = false;
        int newLinesToSend = 0;
        switch(clearType) {
            case NORMAL:
                switch(linesCleared) {
                    case 1:
                        // single
                        // 0 extra
                        bb = false;
                        if(!perfectClear) AudioPlayer.playComboSFX(combo);
                        break;
                    case 2:
                        // double
                        newLinesToSend = 1;
                        bb = false;
                        if(!perfectClear) AudioPlayer.playComboSFX(combo);
                        break;
                    case 3:
                        // triple
                        newLinesToSend = 2;
                        bb = false;
                        if(!perfectClear) AudioPlayer.playComboSFX(combo);
                        break;
                    case 4:
                        // tetris
                        newLinesToSend = 4;
                        bb = true;
                        if(!perfectClear) AudioPlayer.playTSpinSFX(2);
                        break;
                }
                break;
            case T_SPIN:
                switch(linesCleared) {
                    case 1:
                        // T-spin single
                        newLinesToSend = 2;
                        break;
                    case 2:
                        // T-spin double
                        newLinesToSend = 4;
                        break;
                    case 3:
                        // T-spin triple
                        newLinesToSend = 6;
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "You cleared 4 lines with a t-piece?");
                }
                bb = true;
                if(!perfectClear) AudioPlayer.playTSpinSFX(linesCleared);
                break;
            case T_SPIN_MINI:
                // T-spin mini
                newLinesToSend = 1;
                bb = true;
                if(!perfectClear) AudioPlayer.playTSpinSFX(linesCleared);
                break;
        }
        combo++;
        newLinesToSend += comboBonus();
        if(perfectClear) {
            newLinesToSend += 10;
            AudioPlayer.playAllClearSFX();
        }
        if(b2b && bb) newLinesToSend++;
        
        b2b = bb;
        
        if(newLinesToSend != 0) {
            String command = newLinesToSend + "";
            if(command.length() > 0)
                notifyListeners(counterGarbage(command));
            linesSent += newLinesToSend;
        }
    }
    
    /**
     * Determines the bonus for the combo
     * @return the combo bonus
     */
    private int comboBonus() {
        if(combo > 10) {
            return 5;
        } else if(combo > 7) {
            return 4;
        } else if(combo > 5) {
            return 3;
        } else if(combo > 3) {
            return 2;
        } else if(combo > 1) {
            return 1;
        } else return 0;
    }

    /**
     * Returns the number of lines sent
     * @return the number of lines sent
     */
    public int getLinesSent() {
        return linesSent;
    }

    /**
     * Returns the combo number.<br>
     * i.e. if the player performs a 2-combo, 2 will be returned.
     * @return combo number
     */
    public int getCombo() {
        return combo;
    }
    
    /**
     * Adds an <code>ActionListener</code> to this ScoreKeeper
     * @param listener an ActionListener to add
     */
    public void addListener(ActionListener listener) {
        if(listeners == null) listeners = new ArrayList<>();
        listeners.add(listener);
    }
    
    /**
     * Removes all listeners.
     */
    public void removeAllListeners() {
        listeners = new ArrayList<>();
    }
    
    /**
     * Notifies all listeners about an event.
     * @param message the message to convey
     */
    private void notifyListeners(String message) {
        if(listeners == null) return;
        ActionEvent event = new ActionEvent(this, 0, message);
        for(ActionListener listener : listeners) {
            listener.actionPerformed(event);
        }
    }
    
    /**
     * Resets everything.
     */
    public void reset() {
        garbageQueue = new LinkedList<>();
        linesToRecieve = 0;
        
        linesSent = 0;
        
        combo = 0;
        b2b = false;
    }

    /**
     * Whether B2B status is active
     * @return whether B2B status is active
     */
    public boolean isB2B() {
        return b2b;
    }
}
