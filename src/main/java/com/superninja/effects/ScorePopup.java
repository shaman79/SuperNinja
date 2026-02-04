package com.superninja.effects;

import java.awt.*;

/**
 * Floating score popup effect.
 */
public class ScorePopup {
    
    private final double x;
    private double y;
    private final String text;
    private final Color color;
    private final int fontSize;
    private double life;
    private final double maxLife;
    private double velocityY;
    
    public ScorePopup(double x, double y, int score, boolean isCritical, boolean isCombo, int comboCount) {
        this.x = x;
        this.y = y;
        this.maxLife = 1.0;
        this.life = maxLife;
        this.velocityY = -80;
        
        // Build text
        StringBuilder sb = new StringBuilder();
        if (score > 0) {
            sb.append("+").append(score);
        } else {
            sb.append(score);
        }
        
        if (isCritical) {
            sb.append(" CRITICAL!");
        }
        if (isCombo && comboCount > 1) {
            sb.append(" x").append(comboCount);
        }
        
        this.text = sb.toString();
        
        // Determine color and size
        if (score < 0) {
            this.color = new Color(255, 50, 50);
            this.fontSize = 32;
        } else if (isCritical || (isCombo && comboCount > 2)) {
            this.color = new Color(255, 215, 0);
            this.fontSize = 36 + comboCount * 2;
        } else if (isCombo) {
            this.color = new Color(100, 255, 100);
            this.fontSize = 30;
        } else {
            this.color = Color.WHITE;
            this.fontSize = 28;
        }
    }
    
    /**
     * Constructor for custom text popups (power-ups, special messages)
     */
    public ScorePopup(double x, double y, String text, Color color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.fontSize = 28;
        this.maxLife = 1.2;
        this.life = maxLife;
        this.velocityY = -60;
    }
    
    public void update(double deltaTime) {
        life -= deltaTime;
        y += velocityY * deltaTime;
        velocityY *= 0.95; // Slow down
    }
    
    public void render(Graphics2D g2d) {
        if (life <= 0) return;
        
        float alpha = (float) Math.min(1.0, life / 0.3); // Fade out in last 0.3s
        
        Graphics2D g = (Graphics2D) g2d.create();
        g.setFont(new Font("Arial", Font.BOLD, fontSize));
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int drawX = (int)(x - textWidth / 2);
        int drawY = (int) y;
        
        // Shadow
        int shadowAlpha = Math.max(0, Math.min(255, (int)(alpha * 150)));
        g.setColor(new Color(0, 0, 0, shadowAlpha));
        g.drawString(text, drawX + 2, drawY + 2);
        
        // Main text
        int mainAlpha = Math.max(0, Math.min(255, (int)(alpha * 255)));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), mainAlpha));
        g.drawString(text, drawX, drawY);
        
        // Glow for big scores
        if (fontSize > 32) {
            int glowAlpha = Math.max(0, Math.min(255, (int)(alpha * 50)));
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), glowAlpha));
            g.drawString(text, drawX - 1, drawY - 1);
            g.drawString(text, drawX + 1, drawY + 1);
        }
        
        g.dispose();
    }
    
    public boolean isAlive() {
        return life > 0;
    }
}
