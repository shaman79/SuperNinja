package com.superninja.effects;

import com.superninja.config.GameConfig;
import java.awt.*;

/**
 * Juice splash effect when slicing a fruit.
 */
public class JuiceSplashEffect extends ParticleEffect {
    
    public JuiceSplashEffect(double x, double y, Color juiceColor, double sliceAngle) {
        super(x, y);
        createParticles(juiceColor, sliceAngle);
    }
    
    private void createParticles(Color color, double sliceAngle) {
        int count = GameConfig.JUICE_PARTICLE_COUNT;
        
        for (int i = 0; i < count; i++) {
            // Spread particles perpendicular to slice
            double spread = (Math.random() - 0.5) * Math.PI;
            double angle = sliceAngle + Math.PI/2 + spread;
            
            // Vary the speed
            double speed = 150 + Math.random() * 300;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            
            // Vary size
            double size = 5 + Math.random() * 15;
            
            // Vary color slightly
            Color particleColor = varyColor(color, 30);
            
            // Life varies
            double life = 0.5 + Math.random() * 0.5;
            
            Particle p = new Particle(x, y, vx, vy, size, particleColor, life, 
                                     Particle.ParticleShape.DROPLET);
            p.setGravity(400);
            p.setDrag(0.97);
            
            particles.add(p);
        }
        
        // Add some smaller spray particles
        for (int i = 0; i < count / 2; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 50 + Math.random() * 150;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            
            double size = 2 + Math.random() * 5;
            Color particleColor = varyColor(color, 50);
            double life = 0.3 + Math.random() * 0.4;
            
            Particle p = new Particle(x, y, vx, vy, size, particleColor, life,
                                     Particle.ParticleShape.CIRCLE);
            p.setGravity(200);
            p.setDrag(0.95);
            
            particles.add(p);
        }
    }
    
    private Color varyColor(Color base, int variance) {
        int r = clamp(base.getRed() + (int)((Math.random() - 0.5) * variance));
        int g = clamp(base.getGreen() + (int)((Math.random() - 0.5) * variance));
        int b = clamp(base.getBlue() + (int)((Math.random() - 0.5) * variance));
        return new Color(r, g, b);
    }
    
    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
