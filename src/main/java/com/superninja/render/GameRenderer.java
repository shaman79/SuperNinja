package com.superninja.render;

import com.superninja.config.GameConfig;
import com.superninja.effects.EffectManager;
import com.superninja.engine.GameEngine;
import com.superninja.objects.*;

import java.awt.*;

/**
 * Handles all game rendering.
 * Optimized for performance with object caching and pre-rendering.
 */
public class GameRenderer {
    
    private int screenWidth;
    private int screenHeight;
    
    // Fonts
    private Font scoreFont;
    private Font timerFont;
    private Font titleFont;
    private Font countdownFont;
    private Font infoFont;
    
    // Animation timers
    private double animTimer = 0;
    
    // TUIO status
    private boolean tuioConnected;
    private int tuioTouchCount;
    
    public GameRenderer(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        initFonts();
    }
    
    // Legacy constructor for backwards compatibility
    public GameRenderer(int screenWidth, int screenHeight, EffectManager effectManager) {
        this(screenWidth, screenHeight);
    }
    
    private void initFonts() {
        scoreFont = new Font("Arial", Font.BOLD, screenHeight / 15);
        timerFont = new Font("Arial", Font.BOLD, screenHeight / 25);
        titleFont = new Font("Arial", Font.BOLD, screenHeight / 12);
        countdownFont = new Font("Arial", Font.BOLD, screenHeight / 4);
        infoFont = new Font("Arial", Font.PLAIN, 18);
    }
    
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        initFonts();
    }
    
    public int getScreenWidth() {
        return screenWidth;
    }
    
    public int getScreenHeight() {
        return screenHeight;
    }
    
    public void setTuioStatus(boolean connected, int touchCount) {
        this.tuioConnected = connected;
        this.tuioTouchCount = touchCount;
    }
    
    public void update(double deltaTime) {
        animTimer += deltaTime;
    }
    
    /**
     * Main render method
     */
    public void render(Graphics2D g2d, GameEngine engine, int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        
        // Get effect manager from engine
        EffectManager effectManager = engine.getEffectManager();
        
        // Minimal rendering hints for performance (sprites are pre-rendered with AA)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // Apply screen shake
        double shakeX = effectManager.getShakeOffsetX();
        double shakeY = effectManager.getShakeOffsetY();
        if (shakeX != 0 || shakeY != 0) {
            g2d.translate(shakeX, shakeY);
        }
        
        // Draw background
        renderBackground(g2d);
        
        // Draw center divider
        renderDivider(g2d);
        
        // Draw game objects
        for (GameObject obj : engine.getGameObjects()) {
            obj.render(g2d);
        }
        
        // Draw blade trails
        engine.getPlayer1Blade().render(g2d);
        engine.getPlayer2Blade().render(g2d);
        
        // Draw effects
        effectManager.render(g2d);
        
        // Draw UI
        renderUI(g2d, engine);
        
        // Draw state-specific overlays
        switch (engine.getState()) {
            case WAITING -> renderWaitingOverlay(g2d);
            case COUNTDOWN -> renderCountdown(g2d, engine.getCountdownValue());
            case ROUND_END -> renderRoundEnd(g2d, engine);
            case GAME_OVER -> renderGameOver(g2d, engine);
            default -> {}
        }
        
        // Debug info
        if (GameConfig.TUIO_DEBUG) {
            renderDebugInfo(g2d, engine);
        }
        
        // Remove shake transform
        if (shakeX != 0 || shakeY != 0) {
            g2d.translate(-shakeX, -shakeY);
        }
    }
    
    private void renderBackground(Graphics2D g2d) {
        // Solid gradient background - optimized
        GradientPaint gradient = new GradientPaint(
                0, 0, GameConfig.BACKGROUND_TOP,
                0, screenHeight, GameConfig.BACKGROUND_BOTTOM);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, screenWidth, screenHeight);
    }
    
    private void renderDivider(Graphics2D g2d) {
        int x = screenWidth / 2;
        
        // Simple glowing divider line (vertical) - optimized
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.setStroke(new BasicStroke(6));
        g2d.drawLine(x, 0, x, screenHeight);
        
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(x, 0, x, screenHeight);
    }
    
    private void renderUI(Graphics2D g2d, GameEngine engine) {
        Player p1 = engine.getPlayer1();
        Player p2 = engine.getPlayer2();
        
        // Player 1 score (bottom, right-side up)
        renderPlayerScore(g2d, p1, false);
        
        // Player 2 score (top, upside down)
        renderPlayerScore(g2d, p2, true);
        
        // Timer (center)
        if (engine.getState() == GameEngine.GameState.PLAYING) {
            renderTimer(g2d, engine.getRoundTimer(), engine.getCurrentRound());
        }
        
        // Round indicators
        renderRoundIndicators(g2d, p1, p2, engine.getCurrentRound());
    }
    
    private void renderPlayerScore(Graphics2D g2d, Player player, boolean flipped) {
        Graphics2D g = (Graphics2D) g2d.create();
        
        Color playerColor = player.getId() == 1 ? GameConfig.PLAYER1_COLOR : GameConfig.PLAYER2_COLOR;
        
        // Player 1: top-left corner | Player 2: bottom-right corner (diagonal opposite)
        int edgeMargin = 55;
        
        if (!flipped) {
            // Player 1 - top-left corner, text readable from left side
            g.translate(edgeMargin, 120);
            g.rotate(Math.PI / 2); // Rotate so text goes down along left edge
        } else {
            // Player 2 - bottom-right corner, text readable from right side
            g.translate(screenWidth - edgeMargin, screenHeight - 120);
            g.rotate(-Math.PI / 2); // Rotate so text goes up along right edge
        }
        
        // Player label
        g.setColor(playerColor);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString(player.getName(), 0, 0);
        
        // Score (large, prominent)
        g.setFont(new Font("Arial", Font.BOLD, 44));
        g.setColor(Color.WHITE);
        String scoreText = String.valueOf(player.getScore());
        g.drawString(scoreText, 0, 45);
        
        // Combo indicator
        if (player.getComboCount() > 1) {
            g.setColor(new Color(255, 215, 0));
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("x" + player.getComboCount() + " COMBO", 0, 75);
        }
        
        g.dispose();
    }
    
    private void renderTimer(Graphics2D g2d, double time, int round) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        
        // Timer background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(centerX - 80, centerY - 40, 160, 80, 20, 20);
        
        // Timer text
        g2d.setFont(timerFont);
        g2d.setColor(time < 10 ? new Color(255, 100, 100) : Color.WHITE);
        String timeText = String.format("%.1f", Math.max(0, time));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(timeText, centerX - fm.stringWidth(timeText) / 2, centerY + 12);
        
        // Round indicator
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(Color.GRAY);
        String roundText = "Round " + round + "/" + GameConfig.TOTAL_ROUNDS;
        fm = g2d.getFontMetrics();
        g2d.drawString(roundText, centerX - fm.stringWidth(roundText) / 2, centerY + 32);
    }
    
    private void renderRoundIndicators(Graphics2D g2d, Player p1, Player p2, int currentRound) {
        int spacing = 25;
        int size = 15;
        
        // Player 1 indicators (bottom-left corner)
        for (int i = 0; i < GameConfig.TOTAL_ROUNDS; i++) {
            int x = 20 + i * spacing;
            int y = screenHeight - 30;
            
            if (i < p1.getRoundsWon()) {
                g2d.setColor(GameConfig.PLAYER1_COLOR);
                g2d.fillOval(x, y, size, size);
            } else {
                g2d.setColor(new Color(100, 100, 100));
                g2d.drawOval(x, y, size, size);
            }
        }
        
        // Player 2 indicators (bottom-right corner)
        for (int i = 0; i < GameConfig.TOTAL_ROUNDS; i++) {
            int x = screenWidth - 20 - (GameConfig.TOTAL_ROUNDS - i) * spacing;
            int y = screenHeight - 30;
            
            if (i < p2.getRoundsWon()) {
                g2d.setColor(GameConfig.PLAYER2_COLOR);
                g2d.fillOval(x, y, size, size);
            } else {
                g2d.setColor(new Color(100, 100, 100));
                g2d.drawOval(x, y, size, size);
            }
        }
    }
    
    private void renderWaitingOverlay(Graphics2D g2d) {
        // Semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, screenWidth, screenHeight);
        
        // Title
        g2d.setFont(titleFont);
        String title = "SUPER NINJA";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (screenWidth - fm.stringWidth(title)) / 2;
        
        // Glowing text effect
        for (int i = 5; i >= 0; i--) {
            float pulse = (float)(0.5 + 0.5 * Math.sin(animTimer * 3));
            g2d.setColor(new Color(255, 100, 50, (int)(30 * pulse * (5 - i) / 5)));
            g2d.drawString(title, titleX - i, screenHeight / 2 - 50 - i);
            g2d.drawString(title, titleX + i, screenHeight / 2 - 50 + i);
        }
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, titleX, screenHeight / 2 - 50);
        
        // Instruction
        g2d.setFont(new Font("Arial", Font.PLAIN, 28));
        String instruction = "Touch anywhere to start!";
        fm = g2d.getFontMetrics();
        int instructionAlpha = Math.max(0, Math.min(255, (int)(150 + 105 * Math.sin(animTimer * 4))));
        g2d.setColor(new Color(200, 200, 200, instructionAlpha));
        g2d.drawString(instruction, (screenWidth - fm.stringWidth(instruction)) / 2, screenHeight / 2 + 50);
        
        // Game rules
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(new Color(150, 150, 150));
        String[] rules = {
            "Slice fruits for points!",
            "Avoid the bombs!",
            "3 rounds of 60 seconds each",
            "Highest score wins!"
        };
        for (int i = 0; i < rules.length; i++) {
            fm = g2d.getFontMetrics();
            g2d.drawString(rules[i], (screenWidth - fm.stringWidth(rules[i])) / 2, 
                          screenHeight / 2 + 120 + i * 30);
        }
    }
    
    private void renderCountdown(Graphics2D g2d, int value) {
        g2d.setFont(countdownFont);
        // Value is already an int (ceiling of countdown timer), show as whole number
        String text = value <= 0 ? "GO!" : String.valueOf(value);
        FontMetrics fm = g2d.getFontMetrics();
        
        int x = (screenWidth - fm.stringWidth(text)) / 2;
        int y = screenHeight / 2 + fm.getAscent() / 3;
        
        // Pulsing effect
        double scale = 1.0 + 0.1 * Math.sin(animTimer * 10);
        Graphics2D g = (Graphics2D) g2d.create();
        g.translate(screenWidth / 2, screenHeight / 2);
        g.scale(scale, scale);
        g.translate(-screenWidth / 2, -screenHeight / 2);
        
        // Shadow
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(text, x + 4, y + 4);
        
        // Main text
        g.setColor(value <= 0 ? new Color(0, 255, 100) : Color.WHITE);
        g.drawString(text, x, y);
        
        g.dispose();
    }
    
    private void renderRoundEnd(Graphics2D g2d, GameEngine engine) {
        // Semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, screenWidth, screenHeight);
        
        Player p1 = engine.getPlayer1();
        Player p2 = engine.getPlayer2();
        
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        String roundText = "ROUND " + engine.getCurrentRound() + " COMPLETE";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);
        g2d.drawString(roundText, (screenWidth - fm.stringWidth(roundText)) / 2, screenHeight / 2 - 80);
        
        // Round scores
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        
        String p1Score = p1.getName() + ": " + p1.getRoundScore();
        String p2Score = p2.getName() + ": " + p2.getRoundScore();
        
        g2d.setColor(GameConfig.PLAYER1_COLOR);
        fm = g2d.getFontMetrics();
        g2d.drawString(p1Score, (screenWidth - fm.stringWidth(p1Score)) / 2, screenHeight / 2);
        
        g2d.setColor(GameConfig.PLAYER2_COLOR);
        g2d.drawString(p2Score, (screenWidth - fm.stringWidth(p2Score)) / 2, screenHeight / 2 + 50);
        
        // Winner announcement
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        String winner;
        if (p1.getRoundScore() > p2.getRoundScore()) {
            winner = p1.getName() + " wins the round!";
            g2d.setColor(GameConfig.PLAYER1_COLOR);
        } else if (p2.getRoundScore() > p1.getRoundScore()) {
            winner = p2.getName() + " wins the round!";
            g2d.setColor(GameConfig.PLAYER2_COLOR);
        } else {
            winner = "It's a tie!";
            g2d.setColor(Color.YELLOW);
        }
        fm = g2d.getFontMetrics();
        g2d.drawString(winner, (screenWidth - fm.stringWidth(winner)) / 2, screenHeight / 2 + 120);
    }
    
    private void renderGameOver(Graphics2D g2d, GameEngine engine) {
        // Dark overlay
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, screenWidth, screenHeight);
        
        Player p1 = engine.getPlayer1();
        Player p2 = engine.getPlayer2();
        Player winner = p1.isWinner() ? p1 : p2;
        Color winnerColor = winner.getId() == 1 ? GameConfig.PLAYER1_COLOR : GameConfig.PLAYER2_COLOR;
        
        // Game Over title
        g2d.setFont(titleFont);
        String title = "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, (screenWidth - fm.stringWidth(title)) / 2, screenHeight / 2 - 150);
        
        // Winner announcement with animation
        g2d.setFont(new Font("Arial", Font.BOLD, 56));
        String winText = winner.getName() + " WINS!";
        fm = g2d.getFontMetrics();
        
        // Glowing effect
        for (int i = 3; i >= 0; i--) {
            g2d.setColor(new Color(winnerColor.getRed(), winnerColor.getGreen(), 
                                   winnerColor.getBlue(), 50 * (3 - i)));
            g2d.drawString(winText, (screenWidth - fm.stringWidth(winText)) / 2 - i, 
                          screenHeight / 2 - 60 - i);
        }
        g2d.setColor(winnerColor);
        g2d.drawString(winText, (screenWidth - fm.stringWidth(winText)) / 2, screenHeight / 2 - 60);
        
        // Final scores
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        fm = g2d.getFontMetrics();
        
        String score1 = p1.getName() + ": " + p1.getScore() + " pts | " + p1.getRoundsWon() + " rounds";
        String score2 = p2.getName() + ": " + p2.getScore() + " pts | " + p2.getRoundsWon() + " rounds";
        
        g2d.setColor(GameConfig.PLAYER1_COLOR);
        g2d.drawString(score1, (screenWidth - fm.stringWidth(score1)) / 2, screenHeight / 2 + 30);
        
        g2d.setColor(GameConfig.PLAYER2_COLOR);
        g2d.drawString(score2, (screenWidth - fm.stringWidth(score2)) / 2, screenHeight / 2 + 80);
        
        // Statistics
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(new Color(180, 180, 180));
        String stats1 = "Fruits: " + p1.getFruitsSliced() + " | Best Combo: " + p1.getMaxCombo() + 
                       " | Bombs Hit: " + p1.getBombsHit();
        String stats2 = "Fruits: " + p2.getFruitsSliced() + " | Best Combo: " + p2.getMaxCombo() + 
                       " | Bombs Hit: " + p2.getBombsHit();
        fm = g2d.getFontMetrics();
        g2d.drawString(stats1, (screenWidth - fm.stringWidth(stats1)) / 2, screenHeight / 2 + 130);
        g2d.drawString(stats2, (screenWidth - fm.stringWidth(stats2)) / 2, screenHeight / 2 + 160);
        
        // Restart instruction
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        String restart = "Touch to play again!";
        fm = g2d.getFontMetrics();
        int restartAlpha = Math.max(0, Math.min(255, (int)(150 + 105 * Math.sin(animTimer * 4))));
        g2d.setColor(new Color(200, 200, 200, restartAlpha));
        g2d.drawString(restart, (screenWidth - fm.stringWidth(restart)) / 2, screenHeight / 2 + 220);
    }
    
    private void renderDebugInfo(Graphics2D g2d, GameEngine engine) {
        g2d.setFont(infoFont);
        g2d.setColor(Color.YELLOW);
        
        int y = 20;
        int x = screenWidth - 200;
        
        g2d.drawString("State: " + engine.getState(), x, y);
        y += 20;
        g2d.drawString("Objects: " + engine.getGameObjects().size(), x, y);
        y += 20;
        g2d.drawString("TUIO: " + (tuioConnected ? "Connected (" + tuioTouchCount + ")" : "Disconnected"), x, y);
    }
}
