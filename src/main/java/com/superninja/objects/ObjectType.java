package com.superninja.objects;

import com.superninja.config.GameConfig;
import java.awt.*;

/**
 * Enumeration of all game object types with their properties.
 */
public enum ObjectType {
    // Regular fruits - different points based on size/difficulty
    APPLE("Apple", "🍎", GameConfig.APPLE_COLOR, 10, false, false),
    ORANGE("Orange", "🍊", GameConfig.ORANGE_COLOR, 10, false, false),
    WATERMELON("Watermelon", "🍉", GameConfig.WATERMELON_COLOR, 15, false, false),
    BANANA("Banana", "🍌", GameConfig.BANANA_COLOR, 12, false, false),
    GRAPE("Grape", "🍇", GameConfig.GRAPE_COLOR, 8, false, false),
    PINEAPPLE("Pineapple", "🍍", GameConfig.PINEAPPLE_COLOR, 20, false, false),
    
    // Special fruits
    STAR_FRUIT("Star Fruit", "⭐", GameConfig.STAR_FRUIT_COLOR, 50, false, true),
    
    // Dangerous objects
    BOMB("Bomb", "💣", GameConfig.BOMB_COLOR, -GameConfig.BOMB_PENALTY, true, false);
    
    private final String name;
    private final String emoji;
    private final Color color;
    private final int points;
    private final boolean dangerous;
    private final boolean special;
    
    ObjectType(String name, String emoji, Color color, int points, boolean dangerous, boolean special) {
        this.name = name;
        this.emoji = emoji;
        this.color = color;
        this.points = points;
        this.dangerous = dangerous;
        this.special = special;
    }
    
    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public Color getColor() { return color; }
    public int getPoints() { return points; }
    public boolean isDangerous() { return dangerous; }
    public boolean isSpecial() { return special; }
    
    /**
     * Get a secondary color for gradients/effects
     */
    public Color getSecondaryColor() {
        if (dangerous) {
            return new Color(80, 0, 0);
        }
        return color.darker();
    }
    
    /**
     * Get juice/splash color
     */
    public Color getJuiceColor() {
        if (dangerous) {
            return new Color(255, 100, 0); // Explosion orange
        }
        if (special) {
            return new Color(255, 255, 200); // Golden sparkle
        }
        // Lighter, more saturated version
        int r = Math.min(255, color.getRed() + 50);
        int g = Math.min(255, color.getGreen() + 50);
        int b = Math.min(255, color.getBlue() + 50);
        return new Color(r, g, b, 200);
    }
    
    /**
     * Get a random fruit type (not bomb or special)
     */
    public static ObjectType getRandomFruit() {
        ObjectType[] fruits = {APPLE, ORANGE, WATERMELON, BANANA, GRAPE, PINEAPPLE};
        return fruits[(int)(Math.random() * fruits.length)];
    }
    
    /**
     * Get object type based on spawn chances
     */
    public static ObjectType getRandomObject() {
        double rand = Math.random();
        
        if (rand < GameConfig.BOMB_SPAWN_CHANCE) {
            return BOMB;
        } else if (rand < GameConfig.BOMB_SPAWN_CHANCE + GameConfig.SPECIAL_FRUIT_CHANCE) {
            return STAR_FRUIT;
        } else {
            return getRandomFruit();
        }
    }
}
