package com.superninja.render;

import com.superninja.objects.ObjectType;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Loads and caches emoji images for game objects.
 */
public class EmojiLoader {
    
    private static final Map<ObjectType, BufferedImage> emojiCache = new EnumMap<>(ObjectType.class);
    private static final Map<ObjectType, Map<Integer, BufferedImage>> scaledCache = new EnumMap<>(ObjectType.class);
    private static boolean loaded = false;
    
    /**
     * Load all emoji images from resources
     */
    public static void loadEmojis() {
        if (loaded) return;
        
        loadEmoji(ObjectType.APPLE, "/emojis/apple.png");
        loadEmoji(ObjectType.ORANGE, "/emojis/orange.png");
        loadEmoji(ObjectType.WATERMELON, "/emojis/watermelon.png");
        loadEmoji(ObjectType.BANANA, "/emojis/banana.png");
        loadEmoji(ObjectType.GRAPE, "/emojis/grape.png");
        loadEmoji(ObjectType.PINEAPPLE, "/emojis/pineapple.png");
        loadEmoji(ObjectType.STAR_FRUIT, "/emojis/star.png");
        loadEmoji(ObjectType.BOMB, "/emojis/bomb.png");
        
        loaded = true;
        System.out.println("Loaded " + emojiCache.size() + " emoji images");
    }
    
    private static void loadEmoji(ObjectType type, String resourcePath) {
        try (InputStream is = EmojiLoader.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                emojiCache.put(type, img);
                scaledCache.put(type, new java.util.HashMap<>());
            } else {
                System.err.println("Could not find emoji resource: " + resourcePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to load emoji: " + resourcePath + " - " + e.getMessage());
        }
    }
    
    /**
     * Get the emoji image for a given object type, scaled to the specified size
     */
    public static BufferedImage getEmoji(ObjectType type, int size) {
        if (!loaded) {
            loadEmojis();
        }
        
        BufferedImage original = emojiCache.get(type);
        if (original == null) {
            return null;
        }
        
        // Check scaled cache
        Map<Integer, BufferedImage> sizeCache = scaledCache.get(type);
        if (sizeCache != null && sizeCache.containsKey(size)) {
            return sizeCache.get(size);
        }
        
        // Scale the image
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(original, 0, 0, size, size, null);
        g.dispose();
        
        // Cache the scaled version
        if (sizeCache != null) {
            sizeCache.put(size, scaled);
        }
        
        return scaled;
    }
    
    /**
     * Get the original unscaled emoji image
     */
    public static BufferedImage getOriginalEmoji(ObjectType type) {
        if (!loaded) {
            loadEmojis();
        }
        return emojiCache.get(type);
    }
}
