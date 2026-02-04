package com.superninja.engine;

import com.superninja.config.GameConfig;
import com.superninja.effects.EffectManager;
import com.superninja.input.TouchListener;
import com.superninja.input.TouchPoint;
import com.superninja.objects.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Core game engine handling game logic, physics, and state management.
 */
public class GameEngine implements TouchListener {
    
    public enum GameState {
        WAITING,      // Waiting for players to touch to start
        COUNTDOWN,    // Countdown before round starts
        PLAYING,      // Active gameplay
        ROUND_END,    // Round just ended, showing results
        GAME_OVER     // All rounds complete
    }
    
    private GameState state;
    private int currentRound;
    private double roundTimer;
    private double countdownTimer;
    private double roundEndTimer;
    private double spawnTimer;
    private double currentSpawnInterval;
    private double currentObjectSpeed;
    private int spawnCounter; // For alternating between players
    
    private final Player player1;
    private final Player player2;
    
    private final List<GameObject> gameObjects;
    private final List<PowerUpObject> powerUpObjects;
    private final BladeTrail player1Blade;
    private final BladeTrail player2Blade;
    private final Map<Long, Integer> touchToPlayer; // Maps touch session to player
    
    private final PowerUpState player1PowerUps;
    private final PowerUpState player2PowerUps;
    private double powerUpSpawnTimer;
    private static final double POWER_UP_SPAWN_INTERVAL = 8.0; // Spawn power-up every ~8 seconds
    
    private final EffectManager effectManager;
    private final List<GameEventListener> listeners;
    
    private int screenWidth;
    private int screenHeight;
    
    public interface GameEventListener {
        void onStateChanged(GameState newState);
        void onRoundEnd(Player roundWinner, int round);
        void onGameOver(Player winner);
        void onSlice(GameObject obj, int playerId, int points, boolean critical);
        void onBombHit(int playerId);
    }
    
    public GameEngine(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        this.player1 = new Player(1, "Player 1");
        this.player2 = new Player(2, "Player 2");
        
        this.gameObjects = new CopyOnWriteArrayList<>();
        this.powerUpObjects = new CopyOnWriteArrayList<>();
        this.player1Blade = new BladeTrail(1);
        this.player2Blade = new BladeTrail(2);
        this.touchToPlayer = new HashMap<>();
        
        this.player1PowerUps = new PowerUpState(1);
        this.player2PowerUps = new PowerUpState(2);
        this.powerUpSpawnTimer = POWER_UP_SPAWN_INTERVAL;
        
        this.effectManager = new EffectManager();
        this.listeners = new ArrayList<>();
        
        this.state = GameState.WAITING;
        this.currentRound = 1;
    }
    
    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Update game state
     */
    public void update(double deltaTime) {
        switch (state) {
            case WAITING -> updateWaiting(deltaTime);
            case COUNTDOWN -> updateCountdown(deltaTime);
            case PLAYING -> updatePlaying(deltaTime);
            case ROUND_END -> updateRoundEnd(deltaTime);
            case GAME_OVER -> {} // Wait for restart
        }
        
        // Always update blades and effects
        player1Blade.update(deltaTime);
        player2Blade.update(deltaTime);
        effectManager.update(deltaTime);
    }
    
    private void updateWaiting(double deltaTime) {
        // Just wait for touch input to start
    }
    
    private void updateCountdown(double deltaTime) {
        countdownTimer -= deltaTime;
        
        if (countdownTimer <= 0) {
            setState(GameState.PLAYING);
            roundTimer = GameConfig.ROUND_DURATION_SECONDS;
            
            // Each round starts harder: reduce spawn interval and increase speed
            double roundDifficultyBonus = (currentRound - 1) * 0.15; // 0%, 15%, 30% harder
            currentSpawnInterval = GameConfig.INITIAL_SPAWN_INTERVAL * (1.0 - roundDifficultyBonus);
            currentObjectSpeed = GameConfig.INITIAL_OBJECT_SPEED * (1.0 + roundDifficultyBonus);
            
            spawnTimer = 0;
            spawnCounter = 0; // Reset spawn counter for fair distribution
        }
    }
    
    private void updatePlaying(double deltaTime) {
        // Update round timer
        roundTimer -= deltaTime;
        
        // Update power-up states
        player1PowerUps.update(deltaTime);
        player2PowerUps.update(deltaTime);
        
        // Increase difficulty over time
        double elapsedTime = GameConfig.ROUND_DURATION_SECONDS - roundTimer;
        currentSpawnInterval = Math.max(GameConfig.MIN_SPAWN_INTERVAL,
                GameConfig.INITIAL_SPAWN_INTERVAL - elapsedTime * GameConfig.SPAWN_INTERVAL_DECREASE_RATE);
        currentObjectSpeed = Math.min(GameConfig.MAX_OBJECT_SPEED,
                GameConfig.INITIAL_OBJECT_SPEED + elapsedTime * GameConfig.SPEED_INCREASE_RATE);
        
        // Frenzy effect: spawn faster when active
        double effectiveSpawnInterval = currentSpawnInterval;
        if (player1PowerUps.hasFrenzy() || player2PowerUps.hasFrenzy()) {
            effectiveSpawnInterval *= 0.4; // 2.5x faster spawning during frenzy
        }
        
        // Spawn objects
        spawnTimer -= deltaTime * 1000;
        if (spawnTimer <= 0) {
            spawnObject();
            spawnTimer = effectiveSpawnInterval * (0.8 + Math.random() * 0.4);
        }
        
        // Spawn power-ups periodically
        powerUpSpawnTimer -= deltaTime;
        if (powerUpSpawnTimer <= 0) {
            spawnPowerUp();
            powerUpSpawnTimer = POWER_UP_SPAWN_INTERVAL * (0.8 + Math.random() * 0.4);
        }
        
        // Update game objects
        updateGameObjects(deltaTime);
        
        // Update power-up objects
        updatePowerUpObjects(deltaTime);
        
        // Check slicing
        checkSlicing();
        
        // Check power-up collection
        checkPowerUpCollection();
        
        // Check round end
        if (roundTimer <= 0) {
            endRound();
        }
    }
    
    private void updateRoundEnd(double deltaTime) {
        roundEndTimer -= deltaTime;
        
        // Still update objects so they fall off screen
        updateGameObjects(deltaTime);
        
        if (roundEndTimer <= 0) {
            if (currentRound >= GameConfig.TOTAL_ROUNDS) {
                endGame();
            } else {
                currentRound++;
                startCountdown();
            }
        }
    }
    
    private void updateGameObjects(double deltaTime) {
        List<GameObject> toRemove = new ArrayList<>();
        for (GameObject obj : gameObjects) {
            // Apply Speed Curse effect: 30% faster for cursed player's fruits
            double effectiveDeltaTime = deltaTime;
            if (obj.getTargetPlayer() == 1 && player1PowerUps.hasSpeedCurse()) {
                effectiveDeltaTime *= 1.3;
            } else if (obj.getTargetPlayer() == 2 && player2PowerUps.hasSpeedCurse()) {
                effectiveDeltaTime *= 1.3;
            }
            obj.update(effectiveDeltaTime);
            
            // Mark for removal if off screen
            if (obj.isOffScreen(screenWidth, screenHeight)) {
                toRemove.add(obj);
            }
        }
        gameObjects.removeAll(toRemove);
    }
    
    private void spawnObject() {
        // Burst chance increases with each round: 15%, 25%, 35%
        double burstChance = 0.05 + currentRound * 0.10; // Round 1: 15%, Round 2: 25%, Round 3: 35%
        int spawnCount = 1;
        if (Math.random() < burstChance) {
            spawnCount = 2 + (int)(Math.random() * 2); // 2-3 pairs
        }
        
        for (int s = 0; s < spawnCount; s++) {
            // Same fruit type for both players (fair gameplay)
            ObjectType type = ObjectType.getRandomObject();
            
            // Spawn from center divider (x = screenWidth/2)
            double spawnX = screenWidth / 2.0;
            
            // Random Y position for this pair
            double spawnY = screenHeight * 0.15 + Math.random() * screenHeight * 0.7;
            // Mirrored Y position for the other fruit
            double mirroredY = screenHeight - spawnY;
            
            // Calculate velocity
            double baseSpeed = currentObjectSpeed * (0.85 + Math.random() * 0.3);
            double vy = (Math.random() - 0.5) * baseSpeed * 0.4;
            
            // Spawn fruit for Player 1 (going left)
            GameObject obj1 = new GameObject(type, spawnX, spawnY, -baseSpeed, vy, 1);
            gameObjects.add(obj1);
            
            // Spawn SAME fruit for Player 2 (going right) at mirrored Y position
            GameObject obj2 = new GameObject(type, spawnX, mirroredY, baseSpeed, -vy, 2);
            gameObjects.add(obj2);
            
            spawnCounter += 2;
        }
        
        // Apply Bomb Rain effect: extra bombs for cursed player
        if (player1PowerUps.hasBombRain() && Math.random() < 0.3) {
            spawnExtraBomb(1);
        }
        if (player2PowerUps.hasBombRain() && Math.random() < 0.3) {
            spawnExtraBomb(2);
        }
    }
    
    private void spawnExtraBomb(int targetPlayer) {
        double spawnX = screenWidth / 2.0;
        double spawnY = screenHeight * 0.2 + Math.random() * screenHeight * 0.6;
        double baseSpeed = currentObjectSpeed * 0.9;
        double vy = (Math.random() - 0.5) * baseSpeed * 0.3;
        double vx = targetPlayer == 1 ? -baseSpeed : baseSpeed;
        
        GameObject bomb = new GameObject(ObjectType.BOMB, spawnX, spawnY, vx, vy, targetPlayer);
        gameObjects.add(bomb);
    }
    
    private void spawnPowerUp() {
        PowerUpType type = PowerUpType.getRandomPowerUp();
        
        // Spawn from center for both players
        double spawnX = screenWidth / 2.0;
        double spawnY = screenHeight * 0.3 + Math.random() * screenHeight * 0.4;
        double mirroredY = screenHeight - spawnY;
        
        double baseSpeed = currentObjectSpeed * 0.7; // Slower than fruits
        double vy = (Math.random() - 0.5) * baseSpeed * 0.3;
        
        // Power-up for Player 1
        PowerUpObject p1 = new PowerUpObject(type, spawnX, spawnY, -baseSpeed, vy, 1);
        powerUpObjects.add(p1);
        
        // Same power-up for Player 2
        PowerUpObject p2 = new PowerUpObject(type, spawnX, mirroredY, baseSpeed, -vy, 2);
        powerUpObjects.add(p2);
    }
    
    private void updatePowerUpObjects(double deltaTime) {
        List<PowerUpObject> toRemove = new ArrayList<>();
        for (PowerUpObject obj : powerUpObjects) {
            obj.update(deltaTime);
            
            if (obj.isOffScreen(screenWidth, screenHeight) || obj.isCollected()) {
                toRemove.add(obj);
            }
        }
        powerUpObjects.removeAll(toRemove);
    }
    
    private void checkPowerUpCollection() {
        for (PowerUpObject obj : powerUpObjects) {
            if (obj.isCollected()) continue;
            
            // Check player 1's blade
            if (player1Blade.isActive() && obj.intersectsLine(
                    player1Blade.getLastX(), player1Blade.getLastY(),
                    player1Blade.getCurrentX(), player1Blade.getCurrentY())) {
                collectPowerUp(obj, 1);
            }
            
            // Check player 2's blade
            if (!obj.isCollected() && player2Blade.isActive() && obj.intersectsLine(
                    player2Blade.getLastX(), player2Blade.getLastY(),
                    player2Blade.getCurrentX(), player2Blade.getCurrentY())) {
                collectPowerUp(obj, 2);
            }
        }
    }
    
    private void collectPowerUp(PowerUpObject obj, int playerId) {
        obj.collect();
        
        PowerUpType type = obj.getType();
        PowerUpState collector = playerId == 1 ? player1PowerUps : player2PowerUps;
        PowerUpState opponent = playerId == 1 ? player2PowerUps : player1PowerUps;
        
        if (type.isBeneficial()) {
            // Self-beneficial power-up
            collector.activatePowerUp(type);
        } else {
            // Offensive power-up: apply to opponent
            opponent.activatePowerUp(type);
        }
        
        // Show effect
        effectManager.addTextPopup(obj.getX(), obj.getY(), type.getEmoji() + " " + type.getName(), type.getColor());
    }
    
    private void checkSlicing() {
        for (GameObject obj : gameObjects) {
            if (obj.isSliced()) continue;
            
            // Calculate effective hitbox size (Shrink makes fruits smaller/harder to hit)
            double player1SizeMultiplier = player1PowerUps.hasShrink() ? 0.5 : 1.0;
            double player2SizeMultiplier = player2PowerUps.hasShrink() ? 0.5 : 1.0;
            
            // Giant Blade makes blade hitbox larger (effectively increases detection range)
            double player1BladeMultiplier = player1PowerUps.hasGiantBlade() ? 2.0 : 1.0;
            double player2BladeMultiplier = player2PowerUps.hasGiantBlade() ? 2.0 : 1.0;
            
            // Check player 1's blade (only for fruits heading to player 1)
            if (obj.getTargetPlayer() == 1 && player1Blade.isActive() && 
                player1Blade.intersectsWithMultiplier(obj, player1SizeMultiplier * player1BladeMultiplier)) {
                double velocity = player1Blade.getVelocity();
                if (velocity >= GameConfig.MIN_SWIPE_VELOCITY) {
                    sliceObject(obj, 1, player1Blade.getSliceAngle());
                }
            }
            
            // Check player 2's blade (only for fruits heading to player 2)
            if (obj.getTargetPlayer() == 2 && player2Blade.isActive() && 
                player2Blade.intersectsWithMultiplier(obj, player2SizeMultiplier * player2BladeMultiplier)) {
                double velocity = player2Blade.getVelocity();
                if (velocity >= GameConfig.MIN_SWIPE_VELOCITY) {
                    sliceObject(obj, 2, player2Blade.getSliceAngle());
                }
            }
        }
    }
    
    private void sliceObject(GameObject obj, int playerId, double sliceAngle) {
        Player player = playerId == 1 ? player1 : player2;
        PowerUpState powerUps = playerId == 1 ? player1PowerUps : player2PowerUps;
        ObjectType type = obj.getType();
        
        // Slice the object
        obj.slice(sliceAngle, playerId);
        
        if (type.isDangerous()) {
            // Hit a bomb! Check for shield protection
            if (powerUps.useShield()) {
                // Shield absorbed the bomb!
                effectManager.addTextPopup(obj.getX(), obj.getY(), "🛡️ BLOCKED!", new java.awt.Color(100, 149, 237));
            } else {
                // No shield, take the penalty
                player.addPoints(-GameConfig.BOMB_PENALTY);
                player.recordBombHit();
                player.resetCombo();
                
                effectManager.addExplosion(obj.getX(), obj.getY());
                effectManager.addScorePopup(obj.getX(), obj.getY(), -GameConfig.BOMB_PENALTY, 
                                           false, false, 0);
                
                for (GameEventListener l : listeners) {
                    l.onBombHit(playerId);
                }
            }
        } else {
            // Sliced a fruit
            int points = type.getPoints();
            boolean critical = false;
            
            // Check for critical hit (center slice)
            // This is simplified - in reality would check actual slice position
            if (Math.random() < 0.2) {
                points += GameConfig.CRITICAL_BONUS;
                critical = true;
            }
            
            // Combo bonus
            player.incrementCombo();
            if (player.getComboCount() > 1) {
                points += GameConfig.COMBO_BONUS * (player.getComboCount() - 1);
            }
            
            player.addPoints(points);
            player.recordFruitSlice();
            
            // Visual effects
            if (type.isSpecial()) {
                effectManager.addSparkle(obj.getX(), obj.getY());
            }
            effectManager.addJuiceSplash(obj.getX(), obj.getY(), type.getJuiceColor(), sliceAngle);
            effectManager.addScorePopup(obj.getX(), obj.getY(), points, critical,
                                       player.getComboCount() > 1, player.getComboCount());
            
            for (GameEventListener l : listeners) {
                l.onSlice(obj, playerId, points, critical);
            }
        }
    }
    
    /**
     * Start the game
     */
    public void startGame() {
        if (state != GameState.WAITING) return;
        
        player1.reset();
        player2.reset();
        currentRound = 1;
        gameObjects.clear();
        effectManager.clear();
        
        startCountdown();
    }
    
    private void startCountdown() {
        countdownTimer = GameConfig.COUNTDOWN_SECONDS;
        player1.resetRound();
        player2.resetRound();
        gameObjects.clear();
        powerUpObjects.clear();
        
        // Reset power-up states for new round
        player1PowerUps.clearAll();
        player2PowerUps.clearAll();
        powerUpSpawnTimer = 5.0; // First power-up after 5 seconds
        
        setState(GameState.COUNTDOWN);
    }
    
    private void endRound() {
        setState(GameState.ROUND_END);
        roundEndTimer = 3.0;
        
        // Determine round winner
        Player roundWinner = null;
        if (player1.getRoundScore() > player2.getRoundScore()) {
            roundWinner = player1;
            player1.winRound();
        } else if (player2.getRoundScore() > player1.getRoundScore()) {
            roundWinner = player2;
            player2.winRound();
        }
        // Tie: no one wins the round
        
        for (GameEventListener l : listeners) {
            l.onRoundEnd(roundWinner, currentRound);
        }
    }
    
    private void endGame() {
        setState(GameState.GAME_OVER);
        
        // Determine overall winner
        Player winner;
        if (player1.getRoundsWon() > player2.getRoundsWon()) {
            winner = player1;
        } else if (player2.getRoundsWon() > player1.getRoundsWon()) {
            winner = player2;
        } else {
            // Tie in rounds - use total score
            winner = player1.getScore() >= player2.getScore() ? player1 : player2;
        }
        
        winner.setWinner(true);
        
        for (GameEventListener l : listeners) {
            l.onGameOver(winner);
        }
    }
    
    /**
     * Restart the game
     */
    public void restart() {
        player1.reset();
        player2.reset();
        currentRound = 1;
        gameObjects.clear();
        effectManager.clear();
        player1Blade.clear();
        player2Blade.clear();
        
        setState(GameState.WAITING);
    }
    
    private void setState(GameState newState) {
        state = newState;
        for (GameEventListener l : listeners) {
            l.onStateChanged(newState);
        }
    }
    
    // ========================================
    // TouchListener Implementation
    // ========================================
    
    @Override
    public void onTouchDown(TouchPoint touch) {
        if (state == GameState.WAITING) {
            startGame();
            return;
        }
        
        if (state == GameState.GAME_OVER) {
            restart();
            return;
        }
        
        // Determine which player this touch belongs to
        int playerId = touch.isPlayer1Side() ? 1 : 2;
        touchToPlayer.put(touch.getSessionId(), playerId);
        
        // Start blade trail
        BladeTrail blade = playerId == 1 ? player1Blade : player2Blade;
        blade.clear();
        blade.addPoint(touch.getScreenX(screenWidth), touch.getScreenY(screenHeight));
    }
    
    @Override
    public void onTouchMove(TouchPoint touch) {
        if (state != GameState.PLAYING) return;
        
        Integer playerId = touchToPlayer.get(touch.getSessionId());
        if (playerId == null) {
            playerId = touch.isPlayer1Side() ? 1 : 2;
            touchToPlayer.put(touch.getSessionId(), playerId);
        }
        
        BladeTrail blade = playerId == 1 ? player1Blade : player2Blade;
        blade.addPoint(touch.getScreenX(screenWidth), touch.getScreenY(screenHeight));
    }
    
    @Override
    public void onTouchUp(TouchPoint touch) {
        touchToPlayer.remove(touch.getSessionId());
    }
    
    @Override
    public void onTouchFrame(List<TouchPoint> activeTouches) {
        // Reset combos if no active touches for a player
        boolean player1Active = activeTouches.stream().anyMatch(TouchPoint::isPlayer1Side);
        boolean player2Active = activeTouches.stream().anyMatch(TouchPoint::isPlayer2Side);
        
        if (!player1Active && player1Blade.isActive()) {
            // Player 1 stopped swiping - could reset combo after delay
        }
        if (!player2Active && player2Blade.isActive()) {
            // Player 2 stopped swiping
        }
    }
    
    // ========================================
    // Getters
    // ========================================
    
    public GameState getState() { return state; }
    public int getCurrentRound() { return currentRound; }
    public double getRoundTimer() { return roundTimer; }
    public int getCountdownValue() { return (int) Math.ceil(countdownTimer); }
    
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    
    public List<GameObject> getGameObjects() { return gameObjects; }
    public List<PowerUpObject> getPowerUpObjects() { return powerUpObjects; }
    public BladeTrail getPlayer1Blade() { return player1Blade; }
    public BladeTrail getPlayer2Blade() { return player2Blade; }
    
    public PowerUpState getPlayer1PowerUps() { return player1PowerUps; }
    public PowerUpState getPlayer2PowerUps() { return player2PowerUps; }
    
    public EffectManager getEffectManager() { return effectManager; }
    
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
}
