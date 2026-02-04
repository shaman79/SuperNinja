package com.superninja.effects;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages all visual effects in the game.
 */
public class EffectManager {
    
    private final List<ParticleEffect> particleEffects;
    private final List<ScorePopup> scorePopups;
    private ScreenShake activeShake;
    
    public EffectManager() {
        this.particleEffects = new ArrayList<>();
        this.scorePopups = new ArrayList<>();
    }
    
    /**
     * Update all effects
     */
    public void update(double deltaTime) {
        // Update particle effects
        Iterator<ParticleEffect> particleIt = particleEffects.iterator();
        while (particleIt.hasNext()) {
            ParticleEffect effect = particleIt.next();
            effect.update(deltaTime);
            if (effect.isFinished()) {
                particleIt.remove();
            }
        }
        
        // Update score popups
        Iterator<ScorePopup> popupIt = scorePopups.iterator();
        while (popupIt.hasNext()) {
            ScorePopup popup = popupIt.next();
            popup.update(deltaTime);
            if (!popup.isAlive()) {
                popupIt.remove();
            }
        }
        
        // Update screen shake
        if (activeShake != null) {
            activeShake.update(deltaTime);
            if (activeShake.isFinished()) {
                activeShake = null;
            }
        }
    }
    
    /**
     * Render all effects
     */
    public void render(Graphics2D g2d) {
        // Render particle effects
        for (ParticleEffect effect : particleEffects) {
            effect.render(g2d);
        }
        
        // Render score popups
        for (ScorePopup popup : scorePopups) {
            popup.render(g2d);
        }
    }
    
    /**
     * Add a juice splash effect
     */
    public void addJuiceSplash(double x, double y, Color color, double sliceAngle) {
        particleEffects.add(new JuiceSplashEffect(x, y, color, sliceAngle));
    }
    
    /**
     * Add an explosion effect
     */
    public void addExplosion(double x, double y) {
        ExplosionEffect explosion = new ExplosionEffect(x, y);
        particleEffects.add(explosion);
        
        // Trigger screen shake
        activeShake = new ScreenShake(explosion.getShakeIntensity(), explosion.getShakeDuration());
    }
    
    /**
     * Add a sparkle effect for special fruits
     */
    public void addSparkle(double x, double y) {
        particleEffects.add(new SparkleEffect(x, y));
    }
    
    /**
     * Add a score popup
     */
    public void addScorePopup(double x, double y, int score, boolean isCritical, 
                             boolean isCombo, int comboCount) {
        scorePopups.add(new ScorePopup(x, y, score, isCritical, isCombo, comboCount));
    }
    
    /**
     * Add a custom text popup (for power-ups, special messages)
     */
    public void addTextPopup(double x, double y, String text, java.awt.Color color) {
        scorePopups.add(new ScorePopup(x, y, text, color));
    }
    
    /**
     * Get current screen shake offset
     */
    public double getShakeOffsetX() {
        return activeShake != null ? activeShake.getOffsetX() : 0;
    }
    
    public double getShakeOffsetY() {
        return activeShake != null ? activeShake.getOffsetY() : 0;
    }
    
    /**
     * Clear all effects
     */
    public void clear() {
        particleEffects.clear();
        scorePopups.clear();
        activeShake = null;
    }
    
    /**
     * Screen shake effect helper class
     */
    public static class ScreenShake {
        private double intensity;
        private double duration;
        private double elapsed;
        private double offsetX, offsetY;
        
        public ScreenShake(double intensity, double duration) {
            this.intensity = intensity;
            this.duration = duration;
            this.elapsed = 0;
        }
        
        public void update(double deltaTime) {
            elapsed += deltaTime;
            
            double progress = elapsed / duration;
            double currentIntensity = intensity * (1 - progress);
            
            offsetX = (Math.random() - 0.5) * 2 * currentIntensity;
            offsetY = (Math.random() - 0.5) * 2 * currentIntensity;
        }
        
        public boolean isFinished() {
            return elapsed >= duration;
        }
        
        public double getOffsetX() { return offsetX; }
        public double getOffsetY() { return offsetY; }
    }
}
