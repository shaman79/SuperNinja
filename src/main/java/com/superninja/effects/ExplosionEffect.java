package com.superninja.effects;

import com.superninja.config.GameConfig;
import java.awt.*;

/**
 * Explosion effect when hitting a bomb.
 */
public class ExplosionEffect extends ParticleEffect {
    
    private double shakeIntensity;
    private double shakeDuration;
    
    public ExplosionEffect(double x, double y) {
        super(x, y);
        this.shakeIntensity = 20;
        this.shakeDuration = 0.3;
        createParticles();
    }
    
    private void createParticles() {
        int count = GameConfig.EXPLOSION_PARTICLE_COUNT;
        
        // Core explosion particles (orange/yellow)
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 200 + Math.random() * 400;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            
            double size = 10 + Math.random() * 25;
            
            // Fire colors
            Color color;
            double rand = Math.random();
            if (rand < 0.33) {
                color = new Color(255, 200, 50); // Yellow
            } else if (rand < 0.66) {
                color = new Color(255, 100, 0);  // Orange
            } else {
                color = new Color(255, 50, 0);   // Red-orange
            }
            
            double life = 0.4 + Math.random() * 0.4;
            
            Particle p = new Particle(x, y, vx, vy, size, color, life,
                                     Particle.ParticleShape.CIRCLE);
            p.setGravity(100);
            p.setDrag(0.96);
            
            particles.add(p);
        }
        
        // Smoke particles (gray)
        for (int i = 0; i < count / 2; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 50 + Math.random() * 100;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed - 50; // Rise up
            
            double size = 20 + Math.random() * 40;
            
            int gray = 50 + (int)(Math.random() * 50);
            Color color = new Color(gray, gray, gray, 150);
            
            double life = 0.6 + Math.random() * 0.6;
            
            Particle p = new Particle(x, y, vx, vy, size, color, life,
                                     Particle.ParticleShape.CIRCLE);
            p.setGravity(-50); // Float up
            p.setDrag(0.94);
            
            particles.add(p);
        }
        
        // Sparks
        for (int i = 0; i < count / 2; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 300 + Math.random() * 400;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            
            double size = 4 + Math.random() * 8;
            Color color = new Color(255, 255, 200);
            double life = 0.2 + Math.random() * 0.3;
            
            Particle p = new Particle(x, y, vx, vy, size, color, life,
                                     Particle.ParticleShape.SPARK);
            p.setGravity(200);
            p.setDrag(0.92);
            
            particles.add(p);
        }
        
        // Debris
        for (int i = 0; i < count / 4; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 150 + Math.random() * 250;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            
            double size = 8 + Math.random() * 12;
            Color color = new Color(40, 40, 40);
            double life = 0.5 + Math.random() * 0.5;
            
            Particle p = new Particle(x, y, vx, vy, size, color, life,
                                     Particle.ParticleShape.SQUARE);
            p.setGravity(500);
            p.setDrag(0.98);
            
            particles.add(p);
        }
    }
    
    @Override
    public void render(Graphics2D g2d) {
        // Draw flash effect at the beginning
        if (!particles.isEmpty()) {
            double maxLife = particles.stream().mapToDouble(p -> p.life).max().orElse(0);
            if (maxLife > 0.6) {
                float flashAlpha = (float)((maxLife - 0.6) / 0.2);
                int alphaValue = Math.max(0, Math.min(255, (int)(flashAlpha * 150)));
                g2d.setColor(new Color(255, 255, 200, alphaValue));
                int flashSize = (int)(150 * flashAlpha);
                g2d.fillOval((int)(x - flashSize), (int)(y - flashSize), 
                            flashSize * 2, flashSize * 2);
            }
        }
        
        super.render(g2d);
    }
    
    public double getShakeIntensity() { return shakeIntensity; }
    public double getShakeDuration() { return shakeDuration; }
}
