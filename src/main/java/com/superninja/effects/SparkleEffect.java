package com.superninja.effects;

import java.awt.*;

/**
 * Sparkle effect for special fruits.
 */
public class SparkleEffect extends ParticleEffect {
    
    public SparkleEffect(double x, double y) {
        super(x, y);
        createParticles();
    }
    
    private void createParticles() {
        // Golden sparkles
        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 100 + Math.random() * 300;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            
            double size = 6 + Math.random() * 12;
            
            // Gold/yellow colors
            Color color;
            if (Math.random() < 0.5) {
                color = new Color(255, 215, 0); // Gold
            } else {
                color = new Color(255, 255, 150); // Light yellow
            }
            
            double life = 0.5 + Math.random() * 0.5;
            
            Particle p = new Particle(x, y, vx, vy, size, color, life,
                                     Particle.ParticleShape.STAR);
            p.setGravity(50);
            p.setDrag(0.95);
            
            particles.add(p);
        }
        
        // White sparkle trails
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 150 + Math.random() * 200;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            
            double size = 3 + Math.random() * 6;
            Color color = new Color(255, 255, 255);
            double life = 0.3 + Math.random() * 0.4;
            
            Particle p = new Particle(x, y, vx, vy, size, color, life,
                                     Particle.ParticleShape.CIRCLE);
            p.setGravity(0);
            p.setDrag(0.92);
            
            particles.add(p);
        }
    }
}
