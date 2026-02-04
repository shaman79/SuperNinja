package com.superninja.input;

import com.superninja.config.GameConfig;
import TUIO.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages TUIO touch input from the multitouch display.
 */
public class TouchManager implements TuioListener {
    
    private TuioClient tuioClient;
    private final Map<Long, TouchPoint> activeTouches;
    private final List<TouchListener> listeners;
    private boolean connected;
    private boolean debugMode;
    
    private int totalTouchesReceived;
    private int activeTouchCount;
    
    public TouchManager() {
        this.activeTouches = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.connected = false;
        this.debugMode = GameConfig.TUIO_DEBUG;
    }
    
    public boolean connect(int port) {
        try {
            tuioClient = new TuioClient(port);
            tuioClient.addTuioListener(this);
            tuioClient.connect();
            connected = true;
            
            if (debugMode) {
                System.out.println("TouchManager: Connected to TUIO on port " + port);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("TouchManager: Failed to connect to TUIO: " + e.getMessage());
            connected = false;
            return false;
        }
    }
    
    public boolean connect() {
        return connect(GameConfig.TUIO_PORT);
    }
    
    public void disconnect() {
        if (tuioClient != null) {
            tuioClient.disconnect();
            tuioClient = null;
            connected = false;
            activeTouches.clear();
            
            if (debugMode) {
                System.out.println("TouchManager: Disconnected from TUIO");
            }
        }
    }
    
    public boolean isConnected() {
        return connected && tuioClient != null;
    }
    
    public void addListener(TouchListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(TouchListener listener) {
        listeners.remove(listener);
    }
    
    public List<TouchPoint> getActiveTouches() {
        return new ArrayList<>(activeTouches.values());
    }
    
    public TouchPoint getTouch(long sessionId) {
        return activeTouches.get(sessionId);
    }
    
    public int getTouchCount() {
        return activeTouches.size();
    }
    
    public List<TouchPoint> getPlayer1Touches() {
        return activeTouches.values().stream()
                .filter(TouchPoint::isPlayer1Side)
                .toList();
    }
    
    public List<TouchPoint> getPlayer2Touches() {
        return activeTouches.values().stream()
                .filter(TouchPoint::isPlayer2Side)
                .toList();
    }
    
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }
    
    public String getDebugInfo() {
        return String.format("TUIO: %s | Active: %d | Total: %d",
                connected ? "Connected" : "Disconnected",
                activeTouchCount,
                totalTouchesReceived);
    }
    
    // ========================================
    // TuioListener Implementation
    // ========================================
    
    @Override
    public void addTuioCursor(TuioCursor cursor) {
        TouchPoint touch = createTouchPoint(cursor, TouchPoint.TouchState.DOWN);
        activeTouches.put(cursor.getSessionID(), touch);
        activeTouchCount = activeTouches.size();
        totalTouchesReceived++;
        
        if (debugMode) {
            System.out.println("Touch DOWN: " + touch);
        }
        
        for (TouchListener listener : listeners) {
            listener.onTouchDown(touch);
        }
    }
    
    @Override
    public void updateTuioCursor(TuioCursor cursor) {
        TouchPoint touch = createTouchPoint(cursor, TouchPoint.TouchState.MOVE);
        activeTouches.put(cursor.getSessionID(), touch);
        
        if (debugMode) {
            System.out.println("Touch MOVE: " + touch);
        }
        
        for (TouchListener listener : listeners) {
            listener.onTouchMove(touch);
        }
    }
    
    @Override
    public void removeTuioCursor(TuioCursor cursor) {
        TouchPoint touch = createTouchPoint(cursor, TouchPoint.TouchState.UP);
        activeTouches.remove(cursor.getSessionID());
        activeTouchCount = activeTouches.size();
        
        if (debugMode) {
            System.out.println("Touch UP: " + touch);
        }
        
        for (TouchListener listener : listeners) {
            listener.onTouchUp(touch);
        }
    }
    
    @Override
    public void refresh(TuioTime frameTime) {
        List<TouchPoint> touches = getActiveTouches();
        for (TouchListener listener : listeners) {
            listener.onTouchFrame(touches);
        }
    }
    
    @Override
    public void addTuioObject(TuioObject obj) {}
    
    @Override
    public void updateTuioObject(TuioObject obj) {}
    
    @Override
    public void removeTuioObject(TuioObject obj) {}
    
    @Override
    public void addTuioBlob(TuioBlob blob) {}
    
    @Override
    public void updateTuioBlob(TuioBlob blob) {}
    
    @Override
    public void removeTuioBlob(TuioBlob blob) {}
    
    private TouchPoint createTouchPoint(TuioCursor cursor, TouchPoint.TouchState state) {
        return new TouchPoint(
                cursor.getSessionID(),
                cursor.getX(),
                cursor.getY(),
                cursor.getXSpeed(),
                cursor.getYSpeed(),
                cursor.getMotionAccel(),
                state
        );
    }
}
