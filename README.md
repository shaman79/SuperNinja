# SuperNinja

A competitive **Fruit Ninja clone** for TUIO multitouch displays, designed for table mode gameplay. Two players stand on opposite **short edges** of the screen (left and right) and compete to slice the most fruits while avoiding bombs!

## Features

- **Competitive Multiplayer**: Two players compete simultaneously on opposite sides (left vs right)
- **TUIO Protocol Support**: Works with any TUIO-enabled multitouch device
- **Colorful Emoji Fruits**: Beautiful fruit emojis from Google Noto Emoji project
- **Mirrored Fruit Spawning**: Both players get the same fruits for fair competition
- **Beautiful Visual Effects**: Juice splashes, explosions, blade trails, and particle effects
- **Multiple Fruit Types**: 6 different fruits with unique point values
- **Bomb Hazards**: Slice bombs and lose points!
- **Special Star Fruits**: Give bonus points with sparkle effects
- **Combo System**: Chain slices for bonus points
- **Critical Hits**: Perfect slices earn extra points
- **3 Rounds**: 60 seconds each, with increasing difficulty
- **Score Popups**: Floating score indicators show points earned
- **Screen Shake**: Dramatic bomb explosion effects

## Requirements

- Java 17 or higher
- TUIO-enabled multitouch display (or TUIO Simulator for testing)

## Display Specifications

Optimized for **MultiTaction MT553** and similar displays:
- Resolution: 1920 x 1080
- Touch: Up to 32+ simultaneous touch points
- Protocol: TUIO 1.1 on UDP port 3333

## Building

### Using Maven Wrapper (Recommended)

```bash
# Windows
.\mvnw.cmd clean package

# Linux/Mac
./mvnw clean package
```

### Using installed Maven

```bash
mvn clean package
```

The built JAR will be in `target/superninja-1.0.0.jar`.

## Running

### Fullscreen Mode (Default)

```bash
java -jar target/superninja-1.0.0.jar
```

### Windowed Mode

```bash
java -jar target/superninja-1.0.0.jar -w
```

### With Debug Output

```bash
java -jar target/superninja-1.0.0.jar -d
```

## Controls

### Touch Controls (Primary)
- **Left half of screen**: Player 1's zone (stands at left edge)
- **Right half of screen**: Player 2's zone (stands at right edge)
- **Swipe** to slice fruits!
- **Avoid** swiping through bombs

### Player Positions
- Player 1: Top-left corner score display
- Player 2: Bottom-right corner score display
- Players face each other across the table

### Keyboard Controls
- **ESC**: Exit game
- **F2**: Toggle debug overlay
- **R**: Restart game

### Mouse Controls (Testing)
- Click and drag to simulate touch input
- Works for testing without touch hardware

## Gameplay

### Objective
Slice fruits for points while avoiding bombs. The player with the highest score after 3 rounds wins!

### Scoring
| Fruit | Emoji | Points |
|-------|-------|--------|
| Grape | 🍇 | 8 pts |
| Apple | 🍎 | 10 pts |
| Orange | 🍊 | 10 pts |
| Banana | 🍌 | 12 pts |
| Watermelon | 🍉 | 15 pts |
| Pineapple | 🍍 | 20 pts |
| Star Fruit | ⭐ | 50 pts |
| Bomb | 💣 | -50 pts |
| Critical Hit | | +15 pts |
| Combo Bonus | | +5 pts per chain |

### Game Flow
1. **Touch to Start**: Both players touch the screen to begin
2. **3-2-1 Countdown**: Get ready!
3. **60 Second Rounds**: Slice as many fruits as possible
4. **Round Results**: See who won the round
5. **3 Rounds Total**: Best of 3 rounds wins!

### Tips
- Chain slices quickly for combo bonuses
- Watch for bomb fuses - they're dangerous!
- Star fruits (⭐) are worth 50 points
- Pineapples (🍍) are the highest-scoring regular fruit at 20 pts
- Both players get mirrored fruits for fair competition
- Rounds 2 and 3 are progressively harder!

## Testing Without Touch Hardware

Use the **TUIO Simulator** for testing:

1. Download from: https://github.com/mkalten/TUIO11_Simulator
2. Run the simulator
3. Run SuperNinja
4. Create virtual touch points in the simulator

Alternatively, just use mouse input - the game includes mouse simulation.

## Project Structure

```
SuperNinja/
├── src/main/java/com/superninja/
│   ├── SuperNinjaGame.java        # Main game class
│   ├── config/
│   │   └── GameConfig.java        # All configuration settings
│   ├── engine/
│   │   └── GameEngine.java        # Core game logic
│   ├── render/
│   │   ├── GameRenderer.java      # All rendering code
│   │   └── EmojiLoader.java       # Loads and caches emoji images
│   ├── objects/
│   │   ├── GameObject.java        # Fruits, bombs, etc.
│   │   ├── ObjectType.java        # Object type definitions with emojis
│   │   ├── BladeTrail.java        # Blade swipe visualization
│   │   └── Player.java            # Player state
│   ├── effects/
│   │   ├── Particle.java          # Base particle
│   │   ├── ParticleEffect.java    # Effect base class
│   │   ├── JuiceSplashEffect.java # Fruit juice splashes
│   │   ├── ExplosionEffect.java   # Bomb explosions
│   │   ├── SparkleEffect.java     # Star fruit effects
│   │   ├── ScorePopup.java        # Floating score text
│   │   └── EffectManager.java     # Manages all effects
│   └── input/
│       ├── TouchManager.java      # TUIO integration
│       ├── TouchPoint.java        # Touch data
│       ├── TouchListener.java     # Touch event interface
│       └── InputSimulator.java    # Mouse/keyboard input
├── src/main/resources/
│   └── emojis/                    # Emoji PNG images (Google Noto Emoji)
│       ├── apple.png, orange.png, watermelon.png
│       ├── banana.png, grape.png, pineapple.png
│       ├── star.png, bomb.png
├── pom.xml                         # Maven build config
└── README.md                       # This file
```

## Configuration

Key settings in `GameConfig.java`:

| Setting | Default | Description |
|---------|---------|-------------|
| `ROUND_DURATION_SECONDS` | 60 | Length of each round |
| `TOTAL_ROUNDS` | 3 | Number of rounds |
| `BOMB_SPAWN_CHANCE` | 0.12 | Probability of bomb spawn |
| `SPECIAL_FRUIT_CHANCE` | 0.08 | Probability of star fruit |
| `INITIAL_OBJECT_SPEED` | 400 | Starting speed |
| `MAX_OBJECT_SPEED` | 900 | Maximum speed |
| `TUIO_PORT` | 3333 | TUIO UDP port |

## Dependencies

- **TUIO 1.1** (com.artistech:tuio-lib:1.1.6) - Touch input protocol library (from Maven Central)
- **Java Swing/AWT** - UI framework (built-in)
- **Java2D** - Graphics rendering (built-in)

## Credits

- Inspired by **Fruit Ninja** by Halfbrick Studios
- Emoji images from **Google Noto Emoji** (Apache 2.0 License): https://github.com/googlefonts/noto-emoji
- TUIO Protocol: http://www.tuio.org/
- Rendering architecture inspired by **SuperHockey**: https://github.com/nicokosi/SuperHockey

## License

MIT License - Feel free to use and modify!
