package com.superninja.objects;

import java.awt.Color;

/**
 * Types of power-ups that can be collected.
 */
public enum PowerUpType {
    // Self-beneficial power-ups
    FRENZY("Frenzy", "🔥", new Color(255, 100, 0), 4.0, true,
           "Rapid fruit spawning for easy scoring!"),
    SHIELD("Shield", "🛡️", new Color(100, 149, 237), 0, true,
           "Blocks next bomb penalty!"),
    GIANT_BLADE("Giant Blade", "⚔️", new Color(192, 192, 192), 6.0, true,
           "Your swipe hitbox is 2x larger!"),
    
    // Offensive power-ups (affect opponent)
    BOMB_RAIN("Bomb Rain", "☠️", new Color(50, 50, 50), 5.0, false,
           "Extra bombs spawn for your opponent!"),
    SPEED_CURSE("Speed Curse", "💨", new Color(135, 206, 250), 5.0, false,
           "Opponent's fruits move 30% faster!"),
    SHRINK("Shrink", "🔻", new Color(255, 0, 100), 5.0, false,
           "Opponent's fruits become smaller!");
    
    private final String name;
    private final String emoji;
    private final Color color;
    private final double duration; // 0 = instant/one-use
    private final boolean beneficial; // true = helps collector, false = hurts opponent
    private final String description;
    
    PowerUpType(String name, String emoji, Color color, double duration, 
                boolean beneficial, String description) {
        this.name = name;
        this.emoji = emoji;
        this.color = color;
        this.duration = duration;
        this.beneficial = beneficial;
        this.description = description;
    }
    
    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public Color getColor() { return color; }
    public double getDuration() { return duration; }
    public boolean isBeneficial() { return beneficial; }
    public String getDescription() { return description; }
    
    /**
     * Get the resource path for this power-up's emoji image
     */
    public String getResourcePath() {
        return switch (this) {
            case FRENZY -> "/emojis/frenzy.png";
            case SHIELD -> "/emojis/shield.png";
            case GIANT_BLADE -> "/emojis/blade.png";
            case BOMB_RAIN -> "/emojis/bombrain.png";
            case SPEED_CURSE -> "/emojis/speed.png";
            case SHRINK -> "/emojis/shrink.png";
        };
    }
    
    /**
     * Get a random power-up type
     */
    public static PowerUpType getRandomPowerUp() {
        PowerUpType[] types = values();
        return types[(int)(Math.random() * types.length)];
    }
}
