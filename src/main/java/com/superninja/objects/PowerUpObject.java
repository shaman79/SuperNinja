package com.superninja.objects;

import com.superninja.config.GameConfig;
import com.superninja.render.EmojiLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Represents a collectible power-up object.
 */
public class PowerUpObject {
    
    private final PowerUpType type;
    private double x, y;
    private double velocityX, velocityY;
    private double rotation;
    private double rotationSpeed;
    private int size;
    private boolean collected;
    private boolean active;
    private int targetPlayer; // Which player this power-up is heading towards
    private double pulsePhase; // For glowing effect
    
    public PowerUpObject(PowerUpType type, double x, double y, double velocityX, double velocityY, int targetPlayer) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.targetPlayer = targetPlayer;
        this.rotation = Math.random() * 360;
        this.rotationSpeed = (Math.random() - 0.5) * 100;
        this.size = GameConfig.SPECIAL_SIZE;
        this.collected = false;
        this.active = true;
        this.pulsePhase = Math.random() * Math.PI * 2;
    }
    
    /**
     * Update power-up physics
     */
    public void update(double deltaTime) {
        if (collected) return;
        
        // Apply horizontal gravity towards target player's edge
        if (targetPlayer == 1) {
            velocityX -= GameConfig.GRAVITY * deltaTime;
        } else {
            velocityX += GameConfig.GRAVITY * deltaTime;
        }
        
        // Update position
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        
        // Update rotation (slower than fruits for elegance)
        rotation += rotationSpeed * deltaTime;
        
        // Update pulse phase for glow effect
        pulsePhase += deltaTime * 5;
    }
    
    /**
     * Check if point is inside the power-up
     */
    public boolean contains(double px, double py) {
        if (collected) return false;
        double dx = px - x;
        double dy = py - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= size / 2.0;
    }
    
    /**
     * Check if line segment intersects with power-up
     */
    public boolean intersectsLine(double x1, double y1, double x2, double y2) {
        if (collected) return false;
        
        double dx = x2 - x1;
        double dy = y2 - y1;
        double fx = x1 - x;
        double fy = y1 - y;
        
        double a = dx * dx + dy * dy;
        double b = 2 * (fx * dx + fy * dy);
        double c = fx * fx + fy * fy - (size / 2.0) * (size / 2.0);
        
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return false;
        
        double sqrtDisc = Math.sqrt(discriminant);
        double t1 = (-b - sqrtDisc) / (2 * a);
        double t2 = (-b + sqrtDisc) / (2 * a);
        
        return (t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1);
    }
    
    /**
     * Collect this power-up
     */
    public void collect() {
        collected = true;
    }
    
    /**
     * Check if power-up is off screen
     */
    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return y > screenHeight + size || x < -size || x > screenWidth + size;
    }
    
    /**
     * Render the power-up
     */
    public void render(Graphics2D g2d) {
        if (collected) return;
        
        Graphics2D g = (Graphics2D) g2d.create();
        g.translate(x, y);
        
        // Draw glowing aura
        float pulse = (float)(0.5 + 0.5 * Math.sin(pulsePhase));
        int glowSize = (int)(size * 1.5);
        int alpha = (int)(80 + pulse * 80);
        Color glowColor = new Color(
            type.getColor().getRed(),
            type.getColor().getGreen(),
            type.getColor().getBlue(),
            Math.max(0, Math.min(255, alpha))
        );
        
        RadialGradientPaint glow = new RadialGradientPaint(
            0, 0, glowSize / 2f,
            new float[]{0f, 0.5f, 1f},
            new Color[]{glowColor, new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), alpha / 2), new Color(0, 0, 0, 0)}
        );
        g.setPaint(glow);
        g.fillOval(-glowSize / 2, -glowSize / 2, glowSize, glowSize);
        
        // Rotate for the icon
        g.rotate(Math.toRadians(rotation));
        
        // Draw the emoji
        BufferedImage emoji = EmojiLoader.getPowerUpEmoji(type, size);
        if (emoji != null) {
            g.drawImage(emoji, -size / 2, -size / 2, null);
        } else {
            // Fallback
            g.setColor(type.getColor());
            g.fillOval(-size / 2, -size / 2, size, size);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, size / 2));
            g.drawString("?", -size / 6, size / 6);
        }
        
        g.dispose();
    }
    
    // Getters
    public PowerUpType getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
    public boolean isCollected() { return collected; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getTargetPlayer() { return targetPlayer; }
}
