# TUIO Library Setup

SuperNinja uses the TUIO 1.1 protocol for multitouch input. You need to add the TUIO library to use touch input.

## Option 1: Use Maven (Recommended)

The TUIO library is already configured in `pom.xml` and should be downloaded automatically when building.

## Option 2: Manual Installation

If Maven can't find the TUIO library online, you can install it manually:

1. Download TUIO11 Java library from: https://github.com/mkalten/TUIO11_Client/releases

2. Create a `lib` folder in the project root:
   ```
   mkdir lib
   ```

3. Copy `TUIO11_Client.jar` to the `lib` folder

4. Install to local Maven repository:
   ```bash
   mvn install:install-file -Dfile=lib/TUIO11_Client.jar -DgroupId=org.tuio -DartifactId=TUIO11_Client -Dversion=1.1.0 -Dpackaging=jar
   ```

## Testing Without TUIO Hardware

You can test the game without physical TUIO hardware:

### Using TUIO Simulator

1. Download from: https://github.com/mkalten/TUIO11_Simulator
2. Run the simulator (it listens on port 3333)
3. Start SuperNinja
4. Create virtual touch points in the simulator

### Using Mouse Input

Simply run the game - it includes mouse simulation for testing. Click and drag to simulate touches.

## Troubleshooting

### "TUIO not available" Message

This is normal if you don't have a TUIO source running. The game falls back to mouse input automatically.

### Connection Issues

- Ensure no other application is using UDP port 3333
- Check firewall settings
- Verify TUIO device/simulator is running before starting the game

### Custom TUIO Port

Modify `GameConfig.java`:
```java
public static int TUIO_PORT = 3334;  // Change to your port
```

Or specify at runtime (feature to be added in future versions).
