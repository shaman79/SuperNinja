package com.superninja.objects;

import com.superninja.config.GameConfig;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a player's blade trail for slicing.
 */
public class BladeTrail {
    
    private final int playerId;
    private final Color bladeColor;
    private final List<TrailPoint> points;
    private static final int MAX_POINTS = GameConfig.BLADE_TRAIL_LENGTH;
    private boolean active;
    
    public BladeTrail(int playerId) {
        this.playerId = playerId;
        this.bladeColor = playerId == 1 ? GameConfig.PLAYER1_BLADE : GameConfig.PLAYER2_BLADE;
        this.points = new CopyOnWriteArrayList<>();
        this.active = false;
    }
    
    /**
     * Add a new point to the trail
     */
    public void addPoint(double x, double y) {
        points.add(new TrailPoint(x, y, System.nanoTime()));
        active = true;
        
        // Remove old points
        while (points.size() > MAX_POINTS) {
            points.remove(0);
        }
    }
    
    /**
     * Update the trail (fade old points)
     */
    public void update(double deltaTime) {
        long currentTime = System.nanoTime();
        long maxAge = 150_000_000L; // 150ms
        
        // Collect points to remove first, then remove them
        List<TrailPoint> toRemove = new ArrayList<>();
        for (TrailPoint p : points) {
            if ((currentTime - p.timestamp) > maxAge) {
                toRemove.add(p);
            }
        }
        points.removeAll(toRemove);
        
        if (points.isEmpty()) {
            active = false;
        }
    }
    
    /**
     * Clear the trail
     */
    public void clear() {
        points.clear();
        active = false;
    }
    
    /**
     * Check if the trail intersects with a game object
     */
    public boolean intersects(GameObject obj) {
        if (points.size() < 2) return false;
        
        // Check last few segments
        int checkCount = Math.min(5, points.size() - 1);
        for (int i = points.size() - 1; i >= points.size() - checkCount && i > 0; i--) {
            TrailPoint p1 = points.get(i - 1);
            TrailPoint p2 = points.get(i);
            
            if (obj.intersectsLine(p1.x, p1.y, p2.x, p2.y)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get the current slice angle (direction of movement)
     */
    public double getSliceAngle() {
        if (points.size() < 2) return 0;
        
        TrailPoint p1 = points.get(points.size() - 2);
        TrailPoint p2 = points.get(points.size() - 1);
        
        return Math.atan2(p2.y - p1.y, p2.x - p1.x);
    }
    
    /**
     * Get the current velocity of the blade
     */
    public double getVelocity() {
        if (points.size() < 2) return 0;
        
        TrailPoint p1 = points.get(points.size() - 2);
        TrailPoint p2 = points.get(points.size() - 1);
        
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double dt = (p2.timestamp - p1.timestamp) / 1_000_000_000.0;
        
        if (dt <= 0) return 0;
        
        return Math.sqrt(dx * dx + dy * dy) / dt;
    }
    
    /**
     * Render the blade trail
     */
    public void render(Graphics2D g2d) {
        if (points.size() < 2) return;
        
        Graphics2D g = (Graphics2D) g2d.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        long currentTime = System.nanoTime();
        long maxAge = 150_000_000L;
        
        // Draw trail with varying thickness and opacity
        for (int i = 1; i < points.size(); i++) {
            TrailPoint p1 = points.get(i - 1);
            TrailPoint p2 = points.get(i);
            
            // Calculate age-based alpha
            float age1 = (currentTime - p1.timestamp) / (float) maxAge;
            float age2 = (currentTime - p2.timestamp) / (float) maxAge;
            float alpha = 1.0f - (age1 + age2) / 2.0f;
            alpha = Math.max(0, Math.min(1, alpha));
            
            // Position-based thickness (thicker at the end)
            float progress = (float) i / points.size();
            float thickness = 3 + progress * 15;
            
            // Create gradient color with clamped alpha
            int alpha1 = Math.max(0, Math.min(255, (int)(alpha * 255 * (1 - age1))));
            int alpha2 = Math.max(0, Math.min(255, (int)(alpha * 255 * (1 - age2))));
            Color color1 = new Color(
                    bladeColor.getRed(),
                    bladeColor.getGreen(),
                    bladeColor.getBlue(),
                    alpha1);
            Color color2 = new Color(
                    bladeColor.getRed(),
                    bladeColor.getGreen(),
                    bladeColor.getBlue(),
                    alpha2);
            
            // Draw line segment
            g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(color2);
            g.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
            
            // Glow effect
            g.setStroke(new BasicStroke(thickness + 8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int glowAlpha = Math.max(0, Math.min(255, (int)(alpha * 50)));
            g.setColor(new Color(
                    bladeColor.getRed(),
                    bladeColor.getGreen(),
                    bladeColor.getBlue(),
                    glowAlpha));
            g.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
        }
        
        // Draw tip sparkle
        if (!points.isEmpty()) {
            TrailPoint tip = points.get(points.size() - 1);
            float tipAlpha = 1.0f - (currentTime - tip.timestamp) / (float) maxAge;
            if (tipAlpha > 0) {
                int sparkleAlpha = Math.max(0, Math.min(255, (int)(tipAlpha * 200)));
                g.setColor(new Color(255, 255, 255, sparkleAlpha));
                int sparkleSize = 8;
                g.fillOval((int)(tip.x - sparkleSize/2), (int)(tip.y - sparkleSize/2), 
                          sparkleSize, sparkleSize);
            }
        }
        
        g.dispose();
    }
    
    public boolean isActive() { return active; }
    public int getPlayerId() { return playerId; }
    public List<TrailPoint> getPoints() { return points; }
    
    /**
     * Inner class for trail points
     */
    public static class TrailPoint {
        public final double x, y;
        public final long timestamp;
        
        public TrailPoint(double x, double y, long timestamp) {
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
        }
    }
}
