package com.superninja.effects;

import java.awt.*;

/**
 * Represents a single particle in the effects system.
 */
public class Particle {
    
    protected double x, y;
    protected double velocityX, velocityY;
    protected double size;
    protected double initialSize;
    protected Color color;
    protected double life;
    protected double maxLife;
    protected double gravity;
    protected double drag;
    protected double rotation;
    protected double rotationSpeed;
    protected ParticleShape shape;
    
    public enum ParticleShape {
        CIRCLE, SQUARE, STAR, DROPLET, SPARK
    }
    
    public Particle(double x, double y, double vx, double vy, double size, 
                   Color color, double life, ParticleShape shape) {
        this.x = x;
        this.y = y;
        this.velocityX = vx;
        this.velocityY = vy;
        this.size = size;
        this.initialSize = size;
        this.color = color;
        this.life = life;
        this.maxLife = life;
        this.gravity = 300;
        this.drag = 0.98;
        this.rotation = Math.random() * 360;
        this.rotationSpeed = (Math.random() - 0.5) * 360;
        this.shape = shape;
    }
    
    /**
     * Update particle physics
     */
    public void update(double deltaTime) {
        // Apply gravity
        velocityY += gravity * deltaTime;
        
        // Apply drag
        velocityX *= Math.pow(drag, deltaTime * 60);
        velocityY *= Math.pow(drag, deltaTime * 60);
        
        // Update position
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        
        // Update rotation
        rotation += rotationSpeed * deltaTime;
        
        // Update life
        life -= deltaTime;
        
        // Shrink over time
        double lifeRatio = life / maxLife;
        size = initialSize * lifeRatio;
    }
    
    /**
     * Render the particle
     */
    public void render(Graphics2D g2d) {
        if (life <= 0 || size <= 0) return;
        
        double lifeRatio = life / maxLife;
        int alpha = (int)(255 * lifeRatio);
        Color drawColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 
                                   Math.max(0, Math.min(255, alpha)));
        
        Graphics2D g = (Graphics2D) g2d.create();
        g.translate(x, y);
        g.rotate(Math.toRadians(rotation));
        
        switch (shape) {
            case CIRCLE -> {
                g.setColor(drawColor);
                int s = (int) size;
                g.fillOval(-s/2, -s/2, s, s);
            }
            case SQUARE -> {
                g.setColor(drawColor);
                int s = (int) size;
                g.fillRect(-s/2, -s/2, s, s);
            }
            case DROPLET -> {
                renderDroplet(g, drawColor);
            }
            case SPARK -> {
                renderSpark(g, drawColor);
            }
            case STAR -> {
                renderStar(g, drawColor);
            }
        }
        
        g.dispose();
    }
    
    private void renderDroplet(Graphics2D g, Color color) {
        g.setColor(color);
        int s = (int) size;
        
        // Elongated shape based on velocity
        double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        int stretch = (int) Math.min(size * 2, speed * 0.02);
        
        g.fillOval(-s/2, -s/2 - stretch, s, s + stretch);
    }
    
    private void renderSpark(Graphics2D g, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(2));
        int s = (int) size;
        g.drawLine(-s/2, 0, s/2, 0);
        g.drawLine(0, -s/2, 0, s/2);
    }
    
    private void renderStar(Graphics2D g, Color color) {
        g.setColor(color);
        int s = (int) size;
        
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        
        for (int i = 0; i < 10; i++) {
            double radius = (i % 2 == 0) ? s/2 : s/4;
            double angle = Math.PI/2 + i * Math.PI/5;
            xPoints[i] = (int)(radius * Math.cos(angle));
            yPoints[i] = (int)(-radius * Math.sin(angle));
        }
        
        g.fillPolygon(xPoints, yPoints, 10);
    }
    
    public boolean isAlive() {
        return life > 0;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    
    public void setGravity(double gravity) { this.gravity = gravity; }
    public void setDrag(double drag) { this.drag = drag; }
}
