package com.superninja.objects;

import java.util.EnumMap;
import java.util.Map;

/**
 * Tracks active power-up effects for a player.
 */
public class PowerUpState {
    
    private final int playerId;
    private final Map<PowerUpType, Double> activeEffects; // PowerUp -> remaining duration
    private boolean hasShield; // Shield is a one-use effect
    
    public PowerUpState(int playerId) {
        this.playerId = playerId;
        this.activeEffects = new EnumMap<>(PowerUpType.class);
        this.hasShield = false;
    }
    
    /**
     * Activate a power-up for this player
     */
    public void activatePowerUp(PowerUpType type) {
        if (type == PowerUpType.SHIELD) {
            hasShield = true;
        } else if (type.getDuration() > 0) {
            activeEffects.put(type, type.getDuration());
        }
    }
    
    /**
     * Update timers, removing expired effects
     */
    public void update(double deltaTime) {
        activeEffects.entrySet().removeIf(entry -> {
            entry.setValue(entry.getValue() - deltaTime);
            return entry.getValue() <= 0;
        });
    }
    
    /**
     * Check if a power-up effect is currently active
     */
    public boolean isActive(PowerUpType type) {
        if (type == PowerUpType.SHIELD) {
            return hasShield;
        }
        return activeEffects.containsKey(type) && activeEffects.get(type) > 0;
    }
    
    /**
     * Get remaining duration for a power-up (0 if not active)
     */
    public double getRemainingDuration(PowerUpType type) {
        return activeEffects.getOrDefault(type, 0.0);
    }
    
    /**
     * Use shield to block bomb penalty
     * @return true if shield was available and consumed
     */
    public boolean useShield() {
        if (hasShield) {
            hasShield = false;
            return true;
        }
        return false;
    }
    
    /**
     * Check if player has frenzy active (rapid spawning)
     */
    public boolean hasFrenzy() {
        return isActive(PowerUpType.FRENZY);
    }
    
    /**
     * Check if player has giant blade active (2x hitbox)
     */
    public boolean hasGiantBlade() {
        return isActive(PowerUpType.GIANT_BLADE);
    }
    
    /**
     * Check if opponent cursed this player with bomb rain
     */
    public boolean hasBombRain() {
        return isActive(PowerUpType.BOMB_RAIN);
    }
    
    /**
     * Check if opponent cursed this player with speed curse
     */
    public boolean hasSpeedCurse() {
        return isActive(PowerUpType.SPEED_CURSE);
    }
    
    /**
     * Check if opponent cursed this player with shrink
     */
    public boolean hasShrink() {
        return isActive(PowerUpType.SHRINK);
    }
    
    /**
     * Get all active effects for UI display
     */
    public Map<PowerUpType, Double> getActiveEffects() {
        return new EnumMap<>(activeEffects);
    }
    
    /**
     * Clear all effects (for round reset)
     */
    public void clearAll() {
        activeEffects.clear();
        hasShield = false;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public boolean hasShield() {
        return hasShield;
    }
}
