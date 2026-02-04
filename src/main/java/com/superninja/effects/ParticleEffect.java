package com.superninja.effects;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for particle effects.
 */
public abstract class ParticleEffect {
    
    protected List<Particle> particles;
    protected double x, y;
    protected boolean finished;
    
    public ParticleEffect(double x, double y) {
        this.x = x;
        this.y = y;
        this.particles = new ArrayList<>();
        this.finished = false;
    }
    
    /**
     * Update all particles
     */
    public void update(double deltaTime) {
        particles.removeIf(p -> !p.isAlive());
        
        for (Particle p : particles) {
            p.update(deltaTime);
        }
        
        if (particles.isEmpty()) {
            finished = true;
        }
    }
    
    /**
     * Render all particles
     */
    public void render(Graphics2D g2d) {
        for (Particle p : particles) {
            p.render(g2d);
        }
    }
    
    public boolean isFinished() {
        return finished;
    }
    
    protected void addParticle(Particle p) {
        particles.add(p);
    }
}
