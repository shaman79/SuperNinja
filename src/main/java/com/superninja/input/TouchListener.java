package com.superninja.input;

import java.util.List;

/**
 * Interface for receiving touch events from the TouchManager.
 */
public interface TouchListener {
    
    /**
     * Called when a new touch begins
     */
    void onTouchDown(TouchPoint touch);
    
    /**
     * Called when an existing touch moves
     */
    void onTouchMove(TouchPoint touch);
    
    /**
     * Called when a touch ends
     */
    void onTouchUp(TouchPoint touch);
    
    /**
     * Called each frame with all active touches (optional)
     */
    default void onTouchFrame(List<TouchPoint> activeTouches) {
        // Default empty implementation
    }
}
