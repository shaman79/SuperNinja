package com.superninja.input;

/**
 * Represents a single touch point from the TUIO input system.
 * Immutable representation of touch state at a moment in time.
 */
public class TouchPoint {
    
    private final long sessionId;
    private final float x;
    private final float y;
    private final float velocityX;
    private final float velocityY;
    private final float acceleration;
    private final TouchState state;
    private final long timestamp;
    
    /**
     * Touch point states
     */
    public enum TouchState {
        DOWN,    // Touch just started
        MOVE,    // Touch is moving
        UP       // Touch ended
    }
    
    /**
     * Create a new touch point
     */
    public TouchPoint(long sessionId, float x, float y, 
                      float velocityX, float velocityY, float acceleration,
                      TouchState state) {
        this.sessionId = sessionId;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.acceleration = acceleration;
        this.state = state;
        this.timestamp = System.nanoTime();
    }
    
    /**
     * Create a simple touch point with position only
     */
    public TouchPoint(long sessionId, float x, float y, TouchState state) {
        this(sessionId, x, y, 0, 0, 0, state);
    }
    
    public long getSessionId() { return sessionId; }
    public float getX() { return x; }
    public float getY() { return y; }
    
    public int getScreenX(int screenWidth) {
        return (int)(x * screenWidth);
    }
    
    public int getScreenY(int screenHeight) {
        return (int)(y * screenHeight);
    }
    
    public float getVelocityX() { return velocityX; }
    public float getVelocityY() { return velocityY; }
    public float getAcceleration() { return acceleration; }
    public TouchState getState() { return state; }
    public long getTimestamp() { return timestamp; }
    
    /** Check if touch is on player 1's side (left half for table mode) */
    public boolean isPlayer1Side() {
        return x < 0.5f;
    }
    
    /** Check if touch is on player 2's side (right half for table mode) */
    public boolean isPlayer2Side() {
        return x >= 0.5f;
    }
    
    public boolean isDown() {
        return state == TouchState.DOWN;
    }
    
    public boolean isMove() {
        return state == TouchState.MOVE;
    }
    
    public boolean isUp() {
        return state == TouchState.UP;
    }
    
    /**
     * Get the velocity magnitude
     */
    public float getVelocityMagnitude() {
        return (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
    }
    
    @Override
    public String toString() {
        return String.format("TouchPoint[id=%d, pos=(%.3f, %.3f), vel=(%.3f, %.3f), state=%s]",
                sessionId, x, y, velocityX, velocityY, state);
    }
}
