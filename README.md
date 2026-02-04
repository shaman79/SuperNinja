# SuperNinja

A competitive **Fruit Ninja clone** for TUIO multitouch displays, designed for table mode gameplay. Two players stand on opposite sides of the screen and compete to slice the most fruits while avoiding bombs!

![SuperNinja Game](docs/screenshot.png)

## Features

- **Competitive Multiplayer**: Two players compete simultaneously on opposite sides of the screen
- **TUIO Protocol Support**: Works with any TUIO-enabled multitouch device
- **Beautiful Visual Effects**: Juice splashes, explosions, blade trails, and particle effects
- **Multiple Fruit Types**: Various fruits with different colors and juice effects
- **Bomb Hazards**: Slice bombs and lose points!
- **Special Fruits**: Star fruits give bonus points with sparkle effects
- **Combo System**: Chain slices for bonus points
- **Critical Hits**: Perfect slices earn extra points
- **3 Rounds**: 60 seconds each, player with most rounds won wins
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
- **Bottom half of screen**: Player 1's zone
- **Top half of screen**: Player 2's zone
- **Swipe** to slice fruits!
- **Avoid** swiping through bombs

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
| Action | Points |
|--------|--------|
| Regular Fruit | 10 pts |
| Star Fruit (Special) | 50 pts |
| Critical Hit Bonus | +15 pts |
| Combo Bonus | +5 pts per chain |
| Hit Bomb | -30 pts |

### Game Flow
1. **Touch to Start**: Both players touch the screen to begin
2. **3-2-1 Countdown**: Get ready!
3. **60 Second Rounds**: Slice as many fruits as possible
4. **Round Results**: See who won the round
5. **3 Rounds Total**: Best of 3 rounds wins!

### Tips
- Chain slices quickly for combo bonuses
- Watch for bomb fuses - they glow red!
- Star fruits are worth 5x regular fruits
- Center slices have a chance for critical hits

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
│   │   └── GameRenderer.java      # All rendering code
│   ├── objects/
│   │   ├── GameObject.java        # Fruits, bombs, etc.
│   │   ├── ObjectType.java        # Object type definitions
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
├── lib/
│   └── TUIO11_Client.jar          # TUIO library
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
- TUIO Protocol: http://www.tuio.org/
- Based on **SuperPong** architecture: https://github.com/shaman79/SuperPong

## License

MIT License - Feel free to use and modify!
