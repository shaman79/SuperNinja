package com.superninja.objects;

/**
 * Represents a player in the game.
 */
public class Player {
    
    private final int id;
    private final String name;
    private int score;
    private int roundScore;
    private int roundsWon;
    private int comboCount;
    private int maxCombo;
    private int fruitsSliced;
    private int bombsHit;
    private boolean isWinner;
    
    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        reset();
    }
    
    /**
     * Reset all stats for a new game
     */
    public void reset() {
        score = 0;
        roundScore = 0;
        roundsWon = 0;
        comboCount = 0;
        maxCombo = 0;
        fruitsSliced = 0;
        bombsHit = 0;
        isWinner = false;
    }
    
    /**
     * Reset for a new round
     */
    public void resetRound() {
        roundScore = 0;
        comboCount = 0;
    }
    
    /**
     * Add points to the player
     */
    public void addPoints(int points) {
        score += points;
        roundScore += points;
        if (score < 0) score = 0;
        if (roundScore < 0) roundScore = 0;
    }
    
    /**
     * Increment combo counter
     */
    public void incrementCombo() {
        comboCount++;
        if (comboCount > maxCombo) {
            maxCombo = comboCount;
        }
    }
    
    /**
     * Reset combo (missed or bomb)
     */
    public void resetCombo() {
        comboCount = 0;
    }
    
    /**
     * Record a fruit slice
     */
    public void recordFruitSlice() {
        fruitsSliced++;
    }
    
    /**
     * Record hitting a bomb
     */
    public void recordBombHit() {
        bombsHit++;
    }
    
    /**
     * Win a round
     */
    public void winRound() {
        roundsWon++;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getScore() { return score; }
    public int getRoundScore() { return roundScore; }
    public int getRoundsWon() { return roundsWon; }
    public int getComboCount() { return comboCount; }
    public int getMaxCombo() { return maxCombo; }
    public int getFruitsSliced() { return fruitsSliced; }
    public int getBombsHit() { return bombsHit; }
    public boolean isWinner() { return isWinner; }
    public void setWinner(boolean winner) { this.isWinner = winner; }
}
