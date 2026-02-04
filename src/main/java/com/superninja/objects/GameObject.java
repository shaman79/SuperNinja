package com.superninja.objects;

import com.superninja.config.GameConfig;
import com.superninja.render.EmojiLoader;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Represents a sliceable game object (fruit, bomb, etc.)
 */
public class GameObject {
    
    private final ObjectType type;
    private double x, y;
    private double velocityX, velocityY;
    private double rotation;
    private double rotationSpeed;
    private int size;
    private boolean sliced;
    private boolean active;
    private int targetPlayer; // 1 or 2
    
    // Sliced halves
    private SlicedHalf leftHalf;
    private SlicedHalf rightHalf;
    private double sliceAngle;
    
    public GameObject(ObjectType type, double x, double y, double velocityX, double velocityY, int targetPlayer) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.targetPlayer = targetPlayer;
        this.rotation = Math.random() * 360;
        this.rotationSpeed = (Math.random() - 0.5) * GameConfig.ROTATION_SPEED * 2;
        this.size = type.isDangerous() ? GameConfig.BOMB_SIZE : 
                   (type.isSpecial() ? GameConfig.SPECIAL_SIZE : GameConfig.FRUIT_SIZE);
        this.sliced = false;
        this.active = true;
    }
    
    /**
     * Update object physics
     */
    public void update(double deltaTime) {
        if (sliced) {
            // Update sliced halves
            if (leftHalf != null) leftHalf.update(deltaTime, targetPlayer);
            if (rightHalf != null) rightHalf.update(deltaTime, targetPlayer);
            return;
        }
        
        // Apply horizontal gravity towards target player's edge
        if (targetPlayer == 1) {
            velocityX -= GameConfig.GRAVITY * deltaTime; // Pull left
        } else {
            velocityX += GameConfig.GRAVITY * deltaTime; // Pull right
        }
        
        // Update position
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        
        // Update rotation
        rotation += rotationSpeed * deltaTime;
    }
    
    /**
     * Slice the object at a given angle
     */
    public void slice(double angle, int slicedByPlayer) {
        if (sliced) return;
        
        sliced = true;
        sliceAngle = angle;
        
        // Create two halves that fly apart
        double perpX = Math.cos(angle + Math.PI / 2);
        double perpY = Math.sin(angle + Math.PI / 2);
        double separationSpeed = 150;
        
        leftHalf = new SlicedHalf(x, y, 
                velocityX - perpX * separationSpeed, 
                velocityY - perpY * separationSpeed,
                rotation, rotationSpeed - 100, size, angle, true);
        
        rightHalf = new SlicedHalf(x, y,
                velocityX + perpX * separationSpeed,
                velocityY + perpY * separationSpeed,
                rotation, rotationSpeed + 100, size, angle, false);
    }
    
    /**
     * Check if point is inside the object
     */
    public boolean contains(double px, double py) {
        if (sliced) return false;
        double dx = px - x;
        double dy = py - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= size / 2.0;
    }
    
    /**
     * Check if line segment intersects with object
     */
    public boolean intersectsLine(double x1, double y1, double x2, double y2) {
        if (sliced) return false;
        
        // Simple circle-line intersection test
        double dx = x2 - x1;
        double dy = y2 - y1;
        double fx = x1 - x;
        double fy = y1 - y;
        
        double a = dx * dx + dy * dy;
        double b = 2 * (fx * dx + fy * dy);
        double c = fx * fx + fy * fy - (size / 2.0) * (size / 2.0);
        
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return false;
        
        double sqrtDisc = Math.sqrt(discriminant);
        double t1 = (-b - sqrtDisc) / (2 * a);
        double t2 = (-b + sqrtDisc) / (2 * a);
        
        return (t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1);
    }
    
    /**
     * Check if object is still on screen or should be removed
     */
    public boolean isOffScreen(int screenWidth, int screenHeight) {
        if (!sliced) {
            return y > screenHeight + size;
        } else {
            // Check if both halves are off screen
            boolean leftOff = leftHalf == null || leftHalf.getY() > screenHeight + size;
            boolean rightOff = rightHalf == null || rightHalf.getY() > screenHeight + size;
            return leftOff && rightOff;
        }
    }
    
    /**
     * Render the object
     */
    public void render(Graphics2D g2d) {
        if (sliced) {
            renderSlicedHalves(g2d);
        } else {
            renderWhole(g2d);
        }
    }
    
    private void renderWhole(Graphics2D g2d) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.translate(x, y);
        g.rotate(Math.toRadians(rotation));
        
        // Render emoji for all object types
        renderEmoji(g);
        
        g.dispose();
    }
    
    private void renderEmoji(Graphics2D g) {
        // Get the emoji image for this object type
        BufferedImage emoji = EmojiLoader.getEmoji(type, size);
        
        if (emoji != null) {
            // Draw the emoji image centered
            int halfSize = size / 2;
            g.drawImage(emoji, -halfSize, -halfSize, null);
        } else {
            // Fallback: draw a colored circle if emoji not loaded
            int halfSize = size / 2;
            Color baseColor = type.getColor();
            RadialGradientPaint gradient = new RadialGradientPaint(
                -halfSize / 3f, -halfSize / 3f, size * 0.8f,
                new float[]{0f, 0.5f, 1f},
                new Color[]{baseColor.brighter(), baseColor, baseColor.darker()}
            );
            g.setPaint(gradient);
            g.fillOval(-halfSize, -halfSize, size, size);
        }
    }
    
    private void drawStar(Graphics2D g, int cx, int cy, int outerR, int innerR, int points) {
        int[] xPoints = new int[points * 2];
        int[] yPoints = new int[points * 2];
        for (int i = 0; i < points * 2; i++) {
            double angle = Math.PI * i / points - Math.PI / 2;
            int r = (i % 2 == 0) ? outerR : innerR;
            xPoints[i] = cx + (int)(r * Math.cos(angle));
            yPoints[i] = cy + (int)(r * Math.sin(angle));
        }
        g.fillPolygon(xPoints, yPoints, points * 2);
    }
    
    private Color brighter(Color c, float factor) {
        int r = Math.min(255, (int)(c.getRed() * factor));
        int g = Math.min(255, (int)(c.getGreen() * factor));
        int b = Math.min(255, (int)(c.getBlue() * factor));
        return new Color(r, g, b);
    }
    
    private Color darker(Color c, float factor) {
        int r = (int)(c.getRed() * factor);
        int g = (int)(c.getGreen() * factor);
        int b = (int)(c.getBlue() * factor);
        return new Color(r, g, b);
    }
    
    // Keep this for sliced halves rendering
    private void renderFruit(Graphics2D g, int halfSize) {
        Color baseColor = type.getColor();
        
        switch (type) {
            case APPLE -> renderApple(g, halfSize, baseColor);
            case ORANGE -> renderOrangeSprite(g, halfSize, baseColor);
            case WATERMELON -> renderWatermelon(g, halfSize, baseColor);
            case BANANA -> renderBanana(g, halfSize, baseColor);
            case GRAPE -> renderGrape(g, halfSize, baseColor);
            case PINEAPPLE -> renderPineapple(g, halfSize, baseColor);
            default -> renderGenericFruit(g, halfSize, baseColor);
        }
    }
    
    private void renderApple(Graphics2D g, int halfSize, Color baseColor) {
        // Main body with realistic apple shape gradient
        GradientPaint bodyGradient = new GradientPaint(
                -halfSize, -halfSize, new Color(220, 40, 40),
                halfSize, halfSize, new Color(140, 20, 20));
        g.setPaint(bodyGradient);
        
        // Slightly indented top for apple shape
        g.fillOval(-halfSize, -halfSize + 4, size, size - 4);
        
        // Secondary color variation
        g.setColor(new Color(180, 30, 30, 100));
        g.fillArc(-halfSize, -halfSize + 4, size, size - 4, 45, 90);
        
        // Bright highlight (light reflection)
        RadialGradientPaint highlight = new RadialGradientPaint(
                -halfSize/2, -halfSize/2, halfSize,
                new float[]{0f, 1f},
                new Color[]{new Color(255, 255, 255, 150), new Color(255, 255, 255, 0)});
        g.setPaint(highlight);
        g.fillOval(-halfSize + 5, -halfSize + 8, size/2, size/3);
        
        // Brown stem
        g.setColor(new Color(90, 60, 30));
        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(0, -halfSize + 2, 3, -halfSize - 10);
        
        // Green leaf
        g.setColor(new Color(50, 160, 50));
        int[] leafX = {4, 18, 10};
        int[] leafY = {-halfSize - 6, -halfSize - 4, -halfSize + 2};
        g.fillPolygon(leafX, leafY, 3);
        
        // Leaf vein
        g.setColor(new Color(30, 120, 30));
        g.setStroke(new BasicStroke(1));
        g.drawLine(6, -halfSize - 4, 14, -halfSize - 2);
    }
    
    private void renderOrangeSprite(Graphics2D g, int halfSize, Color baseColor) {
        // Main body with realistic orange gradient
        RadialGradientPaint bodyGradient = new RadialGradientPaint(
                -halfSize/3, -halfSize/3, size,
                new float[]{0f, 0.7f, 1f},
                new Color[]{new Color(255, 180, 50), new Color(255, 140, 0), new Color(200, 100, 0)});
        g.setPaint(bodyGradient);
        g.fillOval(-halfSize, -halfSize, size, size);
        
        // Peel texture (small bumps)
        g.setColor(new Color(255, 160, 30, 60));
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * Math.PI * 2;
            double dist = halfSize * 0.3 + Math.random() * halfSize * 0.5;
            int dx = (int)(Math.cos(angle) * dist);
            int dy = (int)(Math.sin(angle) * dist);
            int bumpSize = 3 + (int)(Math.random() * 3);
            g.fillOval(dx - bumpSize/2, dy - bumpSize/2, bumpSize, bumpSize);
        }
        
        // Highlight
        RadialGradientPaint highlight = new RadialGradientPaint(
                -halfSize/2, -halfSize/2, halfSize,
                new float[]{0f, 1f},
                new Color[]{new Color(255, 255, 255, 120), new Color(255, 255, 255, 0)});
        g.setPaint(highlight);
        g.fillOval(-halfSize + 5, -halfSize + 5, size/2, size/3);
        
        // Stem dimple at top
        g.setColor(new Color(160, 80, 0));
        g.fillOval(-4, -halfSize + 2, 8, 6);
        
        // Small green stem remnant
        g.setColor(new Color(80, 120, 40));
        g.fillOval(-2, -halfSize, 4, 4);
    }
    
    private void renderWatermelon(Graphics2D g, int halfSize, Color baseColor) {
        // Dark green rind
        RadialGradientPaint rindGradient = new RadialGradientPaint(
                0, 0, halfSize,
                new float[]{0.7f, 0.85f, 1f},
                new Color[]{new Color(255, 80, 80), new Color(40, 120, 40), new Color(20, 80, 20)});
        g.setPaint(rindGradient);
        g.fillOval(-halfSize, -halfSize, size, size);
        
        // Light green stripes on rind
        g.setColor(new Color(80, 160, 60, 150));
        g.setStroke(new BasicStroke(4));
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3;
            g.drawLine(0, 0, (int)(Math.cos(angle) * halfSize), (int)(Math.sin(angle) * halfSize));
        }
        
        // Red flesh (inner circle)
        RadialGradientPaint fleshGradient = new RadialGradientPaint(
                0, 0, halfSize * 0.7f,
                new float[]{0f, 0.8f, 1f},
                new Color[]{new Color(255, 100, 100), new Color(255, 50, 60), new Color(200, 30, 40)});
        g.setPaint(fleshGradient);
        g.fillOval(-halfSize + 10, -halfSize + 10, size - 20, size - 20);
        
        // Black seeds
        g.setColor(new Color(30, 20, 20));
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4 + 0.2;
            int dist = halfSize / 2;
            int sx = (int)(Math.cos(angle) * dist);
            int sy = (int)(Math.sin(angle) * dist);
            g.fillOval(sx - 2, sy - 3, 4, 6);
        }
        
        // Highlight
        g.setColor(new Color(255, 255, 255, 60));
        g.fillOval(-halfSize + 8, -halfSize + 8, size / 3, size / 4);
    }
    
    private void renderBanana(Graphics2D g, int halfSize, Color baseColor) {
        // Banana curve shape
        g.setColor(new Color(255, 225, 50));
        g.rotate(Math.toRadians(30));
        g.fillArc(-halfSize - 10, -halfSize/2, size + 20, size, 200, 140);
        
        // Brown tips
        g.setColor(new Color(139, 90, 43));
        g.fillOval(-halfSize + 2, halfSize/2 - 2, 8, 8);
        g.fillOval(halfSize - 8, -halfSize/2 - 2, 8, 8);
        
        // Highlight
        g.setColor(new Color(255, 255, 200, 100));
        g.fillOval(-halfSize/2, -halfSize/3, size/2, size/5);
    }
    
    private void renderGrape(Graphics2D g, int halfSize, Color baseColor) {
        // Cluster of grapes
        g.setColor(new Color(128, 0, 128));
        int grapeSize = halfSize / 2;
        // Bottom row
        g.fillOval(-grapeSize, halfSize/3, grapeSize, grapeSize);
        g.fillOval(0, halfSize/3, grapeSize, grapeSize);
        // Middle row
        g.fillOval(-grapeSize - grapeSize/2, 0, grapeSize, grapeSize);
        g.fillOval(-grapeSize/2, 0, grapeSize, grapeSize);
        g.fillOval(grapeSize/2, 0, grapeSize, grapeSize);
        // Top row
        g.fillOval(-grapeSize, -grapeSize, grapeSize, grapeSize);
        g.fillOval(0, -grapeSize, grapeSize, grapeSize);
        
        // Highlights on each grape
        g.setColor(new Color(255, 255, 255, 80));
        g.fillOval(-grapeSize + 2, -grapeSize + 2, grapeSize/3, grapeSize/3);
        g.fillOval(2, -grapeSize + 2, grapeSize/3, grapeSize/3);
        
        // Stem
        g.setColor(new Color(101, 67, 33));
        g.setStroke(new BasicStroke(2));
        g.drawLine(0, -grapeSize, 0, -halfSize);
    }
    
    private void renderPineapple(Graphics2D g, int halfSize, Color baseColor) {
        // Body with diamond pattern
        g.setColor(new Color(218, 165, 32));
        g.fillOval(-halfSize + 5, -halfSize/2, size - 10, size);
        
        // Diamond pattern
        g.setColor(new Color(184, 134, 11));
        g.setStroke(new BasicStroke(1));
        for (int i = -3; i <= 3; i++) {
            g.drawLine(-halfSize + 10, i * 8, halfSize - 10, i * 8 + 10);
            g.drawLine(-halfSize + 10, i * 8, halfSize - 10, i * 8 - 10);
        }
        
        // Crown (leaves)
        g.setColor(new Color(34, 139, 34));
        for (int i = -2; i <= 2; i++) {
            g.fillPolygon(
                new int[]{i * 6 - 3, i * 6, i * 6 + 3},
                new int[]{-halfSize/2, -halfSize - 15, -halfSize/2},
                3);
        }
    }
    
    private void renderGenericFruit(Graphics2D g, int halfSize, Color baseColor) {
        // Main body with gradient
        GradientPaint gradient = new GradientPaint(
                -halfSize, -halfSize, baseColor.brighter(),
                halfSize, halfSize, baseColor.darker());
        g.setPaint(gradient);
        g.fillOval(-halfSize, -halfSize, size, size);
        
        // Highlight
        g.setColor(new Color(255, 255, 255, 80));
        g.fillOval(-halfSize + 5, -halfSize + 5, size / 3, size / 4);
        
        // Outline
        g.setColor(baseColor.darker().darker());
        g.setStroke(new BasicStroke(2));
        g.drawOval(-halfSize, -halfSize, size, size);
    }
    
    private void renderBomb(Graphics2D g, int halfSize) {
        // Main bomb body
        GradientPaint gradient = new GradientPaint(
                -halfSize, -halfSize, new Color(60, 60, 60),
                halfSize, halfSize, new Color(20, 20, 20));
        g.setPaint(gradient);
        g.fillOval(-halfSize, -halfSize, size, size);
        
        // Highlight
        g.setColor(new Color(100, 100, 100, 100));
        g.fillOval(-halfSize + 8, -halfSize + 8, size / 4, size / 5);
        
        // Fuse
        g.setColor(new Color(139, 90, 43));
        g.setStroke(new BasicStroke(4));
        g.drawLine(0, -halfSize, 8, -halfSize - 15);
        
        // Spark
        g.setColor(new Color(255, 200, 50));
        g.fillOval(5, -halfSize - 20, 10, 10);
        g.setColor(new Color(255, 100, 0));
        g.fillOval(7, -halfSize - 18, 6, 6);
        
        // Skull warning
        g.setColor(new Color(200, 200, 200));
        g.setFont(new Font("Arial", Font.BOLD, size / 3));
        FontMetrics fm = g.getFontMetrics();
        String skull = "☠";
        g.drawString(skull, -fm.stringWidth(skull) / 2, fm.getAscent() / 3);
        
        // Danger outline pulse
        float pulse = (float)(0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 100.0));
        g.setColor(new Color(255, 0, 0, (int)(80 * pulse)));
        g.setStroke(new BasicStroke(3));
        g.drawOval(-halfSize - 3, -halfSize - 3, size + 6, size + 6);
    }
    
    private void renderStarFruit(Graphics2D g, int halfSize) {
        // Draw star shape
        Path2D star = new Path2D.Double();
        int points = 5;
        double outerRadius = halfSize;
        double innerRadius = halfSize * 0.4;
        
        for (int i = 0; i < points * 2; i++) {
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double angle = Math.PI / 2 + i * Math.PI / points;
            double px = radius * Math.cos(angle);
            double py = -radius * Math.sin(angle);
            if (i == 0) star.moveTo(px, py);
            else star.lineTo(px, py);
        }
        star.closePath();
        
        // Gradient fill
        GradientPaint gradient = new GradientPaint(
                -halfSize, -halfSize, new Color(255, 255, 150),
                halfSize, halfSize, new Color(255, 200, 0));
        g.setPaint(gradient);
        g.fill(star);
        
        // Sparkle effect
        g.setColor(new Color(255, 255, 255, 150));
        g.fillOval(-5, -halfSize / 2, 10, 10);
        
        // Outline
        g.setColor(new Color(200, 150, 0));
        g.setStroke(new BasicStroke(2));
        g.draw(star);
    }
    
    private void renderSlicedHalves(Graphics2D g2d) {
        BufferedImage emoji = EmojiLoader.getEmoji(type, size);
        
        if (leftHalf != null) {
            leftHalf.render(g2d, emoji, size);
        }
        if (rightHalf != null) {
            rightHalf.render(g2d, emoji, size);
        }
    }
    
    // Getters
    public ObjectType getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
    public boolean isSliced() { return sliced; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getTargetPlayer() { return targetPlayer; }
    public SlicedHalf getLeftHalf() { return leftHalf; }
    public SlicedHalf getRightHalf() { return rightHalf; }
    public double getSliceAngle() { return sliceAngle; }
    
    /**
     * Inner class representing a sliced half
     */
    public static class SlicedHalf {
        private double x, y;
        private double velocityX, velocityY;
        private double rotation, rotationSpeed;
        private int size;
        private double sliceAngle;
        private boolean isLeft;
        private double alpha = 1.0;
        
        public SlicedHalf(double x, double y, double vx, double vy, 
                         double rotation, double rotationSpeed, int size,
                         double sliceAngle, boolean isLeft) {
            this.x = x;
            this.y = y;
            this.velocityX = vx;
            this.velocityY = vy;
            this.rotation = rotation;
            this.rotationSpeed = rotationSpeed;
            this.size = size;
            this.sliceAngle = sliceAngle;
            this.isLeft = isLeft;
        }
        
        public void update(double deltaTime, int targetPlayer) {
            // Horizontal gravity towards player's edge
            if (targetPlayer == 1) {
                velocityX -= GameConfig.GRAVITY * deltaTime * 0.5;
            } else {
                velocityX += GameConfig.GRAVITY * deltaTime * 0.5;
            }
            x += velocityX * deltaTime;
            y += velocityY * deltaTime;
            rotation += rotationSpeed * deltaTime;
            alpha = Math.max(0, alpha - deltaTime * 0.5);
        }
        
        public void render(Graphics2D g2d, BufferedImage emoji, int originalSize) {
            Graphics2D g = (Graphics2D) g2d.create();
            int clampedAlpha = Math.max(0, Math.min(255, (int)(alpha * 255)));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clampedAlpha / 255f));
            g.translate(x, y);
            g.rotate(Math.toRadians(rotation));
            
            int halfSize = originalSize / 2;
            
            // Create clipping shape for half
            Arc2D.Double halfShape = new Arc2D.Double(
                    -halfSize, -halfSize, originalSize, originalSize,
                    isLeft ? 90 : 270, 180, Arc2D.PIE);
            
            g.setClip(halfShape);
            
            // Draw the emoji image (clipped to half)
            if (emoji != null) {
                g.drawImage(emoji, -halfSize, -halfSize, originalSize, originalSize, null);
            } else {
                // Fallback gradient circle
                g.setColor(Color.ORANGE);
                g.fillOval(-halfSize, -halfSize, originalSize, originalSize);
            }
            
            g.dispose();
        }
        
        public double getY() { return y; }
    }
}
