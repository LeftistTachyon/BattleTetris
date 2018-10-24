package simpletetris;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class randomly generates piece order
 * @author Grace Liu, Jed Wang
 */
public class TetrisBag {
    /**
     * The queue of Tetrominos
     */
    private LinkedList<Tetromino> queue;
    
    /**
     * Whether the regeneration of bags is suspended
     */
    private boolean suspended;

    /**
     * Creates a new TetrisBag.
     */
    public TetrisBag() {
        this(false);
    }

    /**
     * Creates a new TetrisBag.
     * @param suspended whether the regeneration of bags is suspended
     */
    public TetrisBag(boolean suspended) {
        this.suspended = suspended;
        queue = new LinkedList<>();
        regenerateBag();
    }
    
    /**
     * Adds 7 new tetrominos to the queue.
     */
    public void regenerateBag() {
        if(suspended) return;
        ArrayList<Tetromino> r = new ArrayList<>();
        r.add(new TetI());
        r.add(new TetJ());
        r.add(new TetL());
        r.add(new TetO());
        r.add(new TetS());
        r.add(new TetT());
        r.add(new TetZ());
        
        String bag = "";
        
        while(!r.isEmpty()) {
            int i = (int) (Math.random() * r.size());
            Tetromino t = r.remove(i);
            queue.add(t);
            bag += t.getShape();
        }
        
        notifyListener("NB" + bag);
    }
    
    /**
     * Adds a pre-specified bag to the mix.
     * @param bag the bag to add
     */
    public void addBag(String bag) {
        if(bag.length() != 7) throw new IllegalArgumentException(
                "Invalid bag: length");
        char[] cc = bag.toCharArray();
        for(char c : cc) {
            switch(c) {
                case 'I':
                    queue.add(new TetI());
                    break;
                case 'J':
                    queue.add(new TetJ());
                    break;
                case 'L':
                    queue.add(new TetL());
                    break;
                case 'O':
                    queue.add(new TetO());
                    break;
                case 'S':
                    queue.add(new TetS());
                    break;
                case 'T':
                    queue.add(new TetT());
                    break;
                case 'Z':
                    queue.add(new TetZ());
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Invalid bag: " + c);
            }
        }
    }
    
    /**
     * Returns the next tetromino in the queue.
     * Also refreshes the queue
     * @return the next tetromino
     */
    public Tetromino remove() {
        Tetromino output = queue.poll();
        if(queue.size() < 7)
            regenerateBag();
        return output;
    }
    
    /**
     * Determines what Tetromino is at that position is in the queue
     * @param which which piece to look for
     * @return what Tetromino is at that position
     */
    public Tetromino next(int which) {
        return (queue.size() > which)?queue.get(which):null;
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
}
