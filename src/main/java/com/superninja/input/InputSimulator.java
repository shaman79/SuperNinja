package com.superninja.input;

import java.awt.Component;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simulates touch input using mouse for testing without hardware.
 * Also handles keyboard input.
 */
public class InputSimulator implements MouseListener, MouseMotionListener, KeyListener {
    
    private int screenWidth;
    private int screenHeight;
    private final List<TouchListener> touchListeners;
    private final List<KeyboardListener> keyListeners;
    
    private final Map<Integer, TouchPoint> simulatedTouches;
    private long nextSessionId = 1000;
    
    private float lastX, lastY;
    private long lastMoveTime;
    
    public interface KeyboardListener {
        void onKeyPressed(int keyCode);
        void onKeyReleased(int keyCode);
    }
    
    public InputSimulator(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.touchListeners = new CopyOnWriteArrayList<>();
        this.keyListeners = new CopyOnWriteArrayList<>();
        this.simulatedTouches = new HashMap<>();
    }
    
    public void attachTo(Component component) {
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
        component.addKeyListener(this);
    }
    
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
    
    public void addTouchListener(TouchListener listener) {
        touchListeners.add(listener);
    }
    
    public void addKeyListener(KeyboardListener listener) {
        keyListeners.add(listener);
    }
    
    private float normalizeX(int x) {
        return (float) x / screenWidth;
    }
    
    private float normalizeY(int y) {
        return (float) y / screenHeight;
    }
    
    private TouchPoint createTouchPoint(MouseEvent e, TouchPoint.TouchState state) {
        float x = normalizeX(e.getX());
        float y = normalizeY(e.getY());
        
        // Calculate velocity
        float vx = 0, vy = 0;
        if (state == TouchPoint.TouchState.MOVE) {
            long currentTime = System.nanoTime();
            float timeDelta = (currentTime - lastMoveTime) / 1_000_000_000.0f;
            if (timeDelta > 0 && timeDelta < 0.1f) {
                vx = (x - lastX) / timeDelta;
                vy = (y - lastY) / timeDelta;
            }
        }
        
        lastX = x;
        lastY = y;
        lastMoveTime = System.nanoTime();
        
        return new TouchPoint(
                nextSessionId + e.getButton(),
                x, y, vx, vy, 0, state
        );
    }
    
    // MouseListener
    @Override
    public void mousePressed(MouseEvent e) {
        TouchPoint touch = createTouchPoint(e, TouchPoint.TouchState.DOWN);
        simulatedTouches.put(e.getButton(), touch);
        
        for (TouchListener listener : touchListeners) {
            listener.onTouchDown(touch);
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        TouchPoint touch = createTouchPoint(e, TouchPoint.TouchState.UP);
        simulatedTouches.remove(e.getButton());
        
        for (TouchListener listener : touchListeners) {
            listener.onTouchUp(touch);
        }
        
        nextSessionId++;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
    
    // MouseMotionListener
    @Override
    public void mouseDragged(MouseEvent e) {
        TouchPoint touch = createTouchPoint(e, TouchPoint.TouchState.MOVE);
        
        // Update for all pressed buttons
        for (Integer button : simulatedTouches.keySet()) {
            simulatedTouches.put(button, touch);
        }
        
        for (TouchListener listener : touchListeners) {
            listener.onTouchMove(touch);
        }
        
        // Also send frame update
        List<TouchPoint> activeTouches = new ArrayList<>(simulatedTouches.values());
        for (TouchListener listener : touchListeners) {
            listener.onTouchFrame(activeTouches);
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        // Track position for velocity calculation
        lastX = normalizeX(e.getX());
        lastY = normalizeY(e.getY());
        lastMoveTime = System.nanoTime();
    }
    
    // KeyListener
    @Override
    public void keyPressed(KeyEvent e) {
        for (KeyboardListener listener : keyListeners) {
            listener.onKeyPressed(e.getKeyCode());
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        for (KeyboardListener listener : keyListeners) {
            listener.onKeyReleased(e.getKeyCode());
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
}
