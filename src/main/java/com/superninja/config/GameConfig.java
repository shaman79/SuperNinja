package com.superninja.config;

import java.awt.Color;

/**
 * Configuration class for SuperNinja game.
 * Contains all settings for the MultiTaction display and game parameters.
 */
public class GameConfig {
    
    // ============================================
    // Display Settings (MultiTaction MT553)
    // ============================================
    
    public static final int DISPLAY_WIDTH = 1920;
    public static final int DISPLAY_HEIGHT = 1080;
    public static final double ASPECT_RATIO = (double) DISPLAY_WIDTH / DISPLAY_HEIGHT;
    
    public static boolean FULLSCREEN = true;
    public static boolean VSYNC_ENABLED = true;
    
    // ============================================
    // TUIO Settings
    // ============================================
    
    public static int TUIO_PORT = 3333;
    public static final int MAX_TOUCH_POINTS = 32;
    public static boolean TUIO_DEBUG = false;
    
    // ============================================
    // Game Settings
    // ============================================
    
    public static final int TARGET_FPS = 60;
    public static final long FRAME_TIME_MS = 1000 / TARGET_FPS;
    
    /** Round duration in seconds */
    public static final int ROUND_DURATION_SECONDS = 60;
    
    /** Number of rounds per game */
    public static final int TOTAL_ROUNDS = 3;
    
    /** Countdown before round starts */
    public static final int COUNTDOWN_SECONDS = 3;
    
    // ============================================
    // Object Spawn Settings
    // ============================================
    
    /** Initial spawn interval in milliseconds */
    public static final double INITIAL_SPAWN_INTERVAL = 1500;
    
    /** Minimum spawn interval (fastest) */
    public static final double MIN_SPAWN_INTERVAL = 300;
    
    /** How much spawn interval decreases per second (ms) */
    public static final double SPAWN_INTERVAL_DECREASE_RATE = 25;
    
    /** Initial object speed */
    public static final double INITIAL_OBJECT_SPEED = 400;
    
    /** Maximum object speed */
    public static final double MAX_OBJECT_SPEED = 900;
    
    /** Speed increase per second of gameplay */
    public static final double SPEED_INCREASE_RATE = 8;
    
    /** Chance of spawning a bomb (0.0 - 1.0) */
    public static final double BOMB_SPAWN_CHANCE = 0.12;
    
    /** Chance of spawning a special fruit (0.0 - 1.0) */
    public static final double SPECIAL_FRUIT_CHANCE = 0.08;
    
    // ============================================
    // Scoring
    // ============================================
    
    /** Points per fruit sliced */
    public static final int POINTS_PER_FRUIT = 10;
    
    /** Bonus points for combo (multiplied by combo count) */
    public static final int COMBO_BONUS = 5;
    
    /** Points for special fruit */
    public static final int SPECIAL_FRUIT_POINTS = 50;
    
    /** Penalty for slicing a bomb */
    public static final int BOMB_PENALTY = 30;
    
    /** Points for critical slice (hitting center) */
    public static final int CRITICAL_BONUS = 15;
    
    // ============================================
    // Object Sizes
    // ============================================
    
    public static int FRUIT_SIZE = 80;
    public static int BOMB_SIZE = 90;
    public static int SPECIAL_SIZE = 70;
    
    // ============================================
    // Visual Settings
    // ============================================
    
    /** Trail length for blade effect */
    public static final int BLADE_TRAIL_LENGTH = 20;
    
    /** Particle count for slice effect */
    public static final int SLICE_PARTICLE_COUNT = 15;
    
    /** Particle count for bomb explosion */
    public static final int EXPLOSION_PARTICLE_COUNT = 40;
    
    /** Juice splash particle count */
    public static final int JUICE_PARTICLE_COUNT = 25;
    
    // ============================================
    // Colors
    // ============================================
    
    public static final Color PLAYER1_COLOR = new Color(0, 150, 255);
    public static final Color PLAYER2_COLOR = new Color(255, 100, 50);
    public static final Color PLAYER1_BLADE = new Color(100, 200, 255, 200);
    public static final Color PLAYER2_BLADE = new Color(255, 150, 100, 200);
    public static final Color BACKGROUND_TOP = new Color(10, 10, 30);
    public static final Color BACKGROUND_BOTTOM = new Color(30, 20, 50);
    public static final Color DIVIDER_COLOR = new Color(255, 255, 255, 100);
    
    // Fruit colors
    public static final Color APPLE_COLOR = new Color(220, 50, 50);
    public static final Color ORANGE_COLOR = new Color(255, 165, 0);
    public static final Color WATERMELON_COLOR = new Color(50, 180, 50);
    public static final Color BANANA_COLOR = new Color(255, 230, 80);
    public static final Color GRAPE_COLOR = new Color(150, 50, 200);
    public static final Color PINEAPPLE_COLOR = new Color(255, 200, 50);
    public static final Color BOMB_COLOR = new Color(40, 40, 40);
    public static final Color STAR_FRUIT_COLOR = new Color(255, 215, 0);
    
    // ============================================
    // Physics
    // ============================================
    
    public static final double GRAVITY = 400;
    public static final double ROTATION_SPEED = 180; // degrees per second
    public static final double MIN_LAUNCH_ANGLE = 60; // degrees from horizontal
    public static final double MAX_LAUNCH_ANGLE = 120;
    
    // ============================================
    // Slice Detection
    // ============================================
    
    /** Minimum swipe velocity to register as slice */
    public static final double MIN_SWIPE_VELOCITY = 0.1;
    
    /** Maximum distance from center for critical hit */
    public static final double CRITICAL_HIT_RADIUS = 0.3;
    
    // ============================================
    // Runtime Values (recalculated for screen size)
    // ============================================
    
    private static double scaleX = 1.0;
    private static double scaleY = 1.0;
    
    public static void recalculatePixelValues(int screenWidth, int screenHeight) {
        scaleX = (double) screenWidth / DISPLAY_WIDTH;
        scaleY = (double) screenHeight / DISPLAY_HEIGHT;
        
        FRUIT_SIZE = (int)(80 * Math.min(scaleX, scaleY));
        BOMB_SIZE = (int)(90 * Math.min(scaleX, scaleY));
        SPECIAL_SIZE = (int)(70 * Math.min(scaleX, scaleY));
    }
    
    public static double getScaleX() { return scaleX; }
    public static double getScaleY() { return scaleY; }
}
