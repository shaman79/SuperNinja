package com.superninja;

import com.superninja.config.GameConfig;
import com.superninja.engine.GameEngine;
import com.superninja.input.InputSimulator;
import com.superninja.input.TouchListener;
import com.superninja.input.TouchManager;
import com.superninja.input.TouchPoint;
import com.superninja.objects.GameObject;
import com.superninja.objects.Player;
import com.superninja.render.EmojiLoader;
import com.superninja.render.GameRenderer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

/**
 * Main game class for SuperNinja using Canvas with BufferStrategy
 * for high-performance active rendering with page flipping.
 * 
 * This approach bypasses Swing's painting mechanism for smoother framerates.
 * 
 * Designed for MultiTaction MT553 (55" 1920x1080 multitouch display)
 * using TUIO protocol for touch input.
 */
public class SuperNinjaGame extends Canvas implements Runnable,
        InputSimulator.KeyboardListener, GameEngine.GameEventListener, TouchListener {
    
    private static final String TITLE = "SuperNinja";
    private static final int NUM_BUFFERS = 2; // Double buffering
    
    // Window
    private Frame frame;
    private boolean fullscreen;
    private GraphicsDevice graphicsDevice;
    
    // Game state
    private boolean running;
    private Thread gameThread;
    private BufferStrategy bufferStrategy;
    
    // Subsystems
    private GameEngine engine;
    private GameRenderer renderer;
    private TouchManager touchManager;
    private InputSimulator inputSimulator;
    
    // Screen dimensions
    private int screenWidth;
    private int screenHeight;
    
    // Performance tracking
    private int fps;
    private int ups;
    private double avgFrameTime = 0;
    
    public SuperNinjaGame() {
        this.fullscreen = GameConfig.FULLSCREEN;
        
        // Get actual screen size from the system
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        this.screenWidth = dm.getWidth();
        this.screenHeight = dm.getHeight();
        
        // Canvas settings
        setBackground(Color.BLACK);
        setIgnoreRepaint(true); // We handle our own rendering
    }
    
    /**
     * Initialize all game systems
     */
    public void init() {
        System.out.println("Initializing SuperNinja...");
        
        // Preload emoji images
        EmojiLoader.loadEmojis();
        
        // Create window
        createWindow();
        
        // Wait for buffer strategy to be ready
        BufferStrategy bs = null;
        for (int i = 0; i < 10 && bs == null; i++) {
            try {
                createBufferStrategy(NUM_BUFFERS);
                bs = getBufferStrategy();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
        
        if (bs == null) {
            System.err.println("Failed to create BufferStrategy!");
            return;
        }
        bufferStrategy = bs;
        
        // Initialize renderer
        renderer = new GameRenderer(screenWidth, screenHeight);
        
        // Initialize game engine
        engine = new GameEngine(screenWidth, screenHeight);
        engine.addListener(this);
        
        // Initialize input
        initInput();
        
        // Recalculate config values for actual screen size
        GameConfig.recalculatePixelValues(screenWidth, screenHeight);
        
        System.out.println("SuperNinja initialized. Screen: " + screenWidth + "x" + screenHeight);
    }
    
    private void createWindow() {
        frame = new Frame(TITLE);
        frame.setBackground(Color.BLACK);
        frame.setIgnoreRepaint(true);
        
        // Get graphics device
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        graphicsDevice = ge.getDefaultScreenDevice();
        
        // Get screen bounds (accounts for taskbar and scaling)
        Rectangle screenBounds = ge.getMaximumWindowBounds();
        DisplayMode dm = graphicsDevice.getDisplayMode();
        
        System.out.println("Display mode: " + dm.getWidth() + "x" + dm.getHeight());
        System.out.println("Screen bounds: " + screenBounds);
        
        if (fullscreen) {
            // Fullscreen: undecorated window at screen size
            frame.setUndecorated(true);
            screenWidth = screenBounds.width;
            screenHeight = screenBounds.height;
        } else {
            // Windowed mode - use config size or 80% of screen
            screenWidth = Math.min(GameConfig.DISPLAY_WIDTH, (int)(screenBounds.width * 0.8));
            screenHeight = Math.min(GameConfig.DISPLAY_HEIGHT, (int)(screenBounds.height * 0.8));
        }
        
        // Same initialization for both modes
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        frame.add(this);
        frame.pack();
        
        if (fullscreen) {
            frame.setLocation(0, 0);
        } else {
            frame.setLocationRelativeTo(null);
        }
        
        frame.setVisible(true);
        
        System.out.println("Canvas size: " + getWidth() + "x" + getHeight());
        
        // Window listener
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stop();
            }
        });
        
        requestFocus();
        
        // Force a repaint to ensure the frame is drawn
        frame.repaint();
    }
    
    private void initInput() {
        // Initialize TUIO touch manager
        touchManager = new TouchManager();
        touchManager.addListener(engine);
        touchManager.addListener(this);
        
        // Try to connect to TUIO
        boolean tuioConnected = touchManager.connect(GameConfig.TUIO_PORT);
        if (!tuioConnected) {
            System.out.println("TUIO not available - using mouse simulation");
        } else {
            System.out.println("TUIO connected on port " + GameConfig.TUIO_PORT);
        }
        
        // Initialize input simulator (mouse/keyboard)
        inputSimulator = new InputSimulator(screenWidth, screenHeight);
        inputSimulator.addTouchListener(engine);
        inputSimulator.addTouchListener(this);
        inputSimulator.addKeyListener(this);
        inputSimulator.attachTo(this);
    }
    
    /**
     * Start the game
     */
    public synchronized void start() {
        if (running) return;
        
        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.setPriority(Thread.MAX_PRIORITY);
        gameThread.start();
        
        System.out.println("SuperNinja started!");
        System.out.println("Controls:");
        System.out.println("  Touch/Mouse - Slice fruits");
        System.out.println("  ESC         - Exit game");
        System.out.println("  F2          - Toggle debug mode");
        System.out.println("  R           - Restart game");
    }
    
    /**
     * Stop the game
     */
    public synchronized void stop() {
        if (!running) return;
        
        running = false;
        
        // Disconnect TUIO
        if (touchManager != null) {
            touchManager.disconnect();
        }
        
        try {
            if (gameThread != null) {
                gameThread.join(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Dispose frame
        if (frame != null) {
            frame.dispose();
        }
        
        System.exit(0);
    }
    
    /**
     * Main game loop with fixed timestep and variable rendering
     */
    @Override
    public void run() {
        // Wait for window to be ready
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            return;
        }
        
        // Timing
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        
        double nsPerUpdate = 1_000_000_000.0 / GameConfig.TARGET_FPS;
        double delta = 0;
        
        int frames = 0;
        int updates = 0;
        
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerUpdate;
            lastTime = now;
            
            // Update at fixed timestep
            while (delta >= 1) {
                double deltaTime = 1.0 / GameConfig.TARGET_FPS;
                update(deltaTime);
                updates++;
                delta--;
            }
            
            // Render
            render();
            frames++;
            
            // FPS counter
            if (System.currentTimeMillis() - timer >= 1000) {
                timer = System.currentTimeMillis();
                fps = frames;
                ups = updates;
                frames = 0;
                updates = 0;
                
                if (GameConfig.TUIO_DEBUG) {
                    System.out.println("FPS: " + fps + " UPS: " + ups);
                }
            }
            
            // Small sleep to prevent 100% CPU usage
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Update game state
     */
    private void update(double deltaTime) {
        // Update engine
        engine.update(deltaTime);
        
        // Update renderer
        renderer.update(deltaTime);
        
        // Update TUIO status
        renderer.setTuioStatus(touchManager.isConnected(), touchManager.getTouchCount());
    }
    
    /**
     * Render using BufferStrategy (active rendering with page flipping)
     */
    private void render() {
        // BufferStrategy can become invalid
        if (bufferStrategy == null) {
            createBufferStrategy(NUM_BUFFERS);
            bufferStrategy = getBufferStrategy();
            return;
        }
        
        long renderStart = System.nanoTime();
        
        // Render loop - may need multiple attempts if buffer is lost
        do {
            do {
                Graphics2D g2d = null;
                try {
                    g2d = (Graphics2D) bufferStrategy.getDrawGraphics();
                    
                    // Always use getWidth/getHeight - they return correct scaled values
                    int w = getWidth();
                    int h = getHeight();
                    
                    // Fallback if dimensions are still invalid
                    if (w <= 0 || h <= 0) {
                        w = screenWidth;
                        h = screenHeight;
                    }
                    
                    // Clear background
                    g2d.setColor(new Color(10, 10, 20));
                    g2d.fillRect(0, 0, w, h);
                    
                    // Render game if ready
                    if (engine != null && renderer != null) {
                        // Update all components screen size if needed
                        if (w != renderer.getScreenWidth() || h != renderer.getScreenHeight()) {
                            screenWidth = w;
                            screenHeight = h;
                            engine.setScreenSize(w, h);
                            renderer.setScreenSize(w, h);
                            if (inputSimulator != null) {
                                inputSimulator.setScreenSize(w, h);
                            }
                        }
                        
                        // Main render
                        renderer.render(g2d, engine, w, h);
                        
                        // Debug info overlay
                        if (GameConfig.TUIO_DEBUG) {
                            g2d.setColor(Color.YELLOW);
                            g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
                            g2d.drawString("FPS: " + fps + " | Frame: " + String.format("%.1f", avgFrameTime) + "ms | " + w + "x" + h, 10, h - 30);
                        }
                    } else {
                        // Show loading message
                        g2d.setColor(Color.WHITE);
                        g2d.setFont(new Font("SansSerif", Font.BOLD, 32));
                        g2d.drawString("Loading...", w/2 - 60, h/2);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Render error: " + e.getMessage());
                } finally {
                    if (g2d != null) {
                        g2d.dispose();
                    }
                }
            } while (bufferStrategy.contentsRestored()); // Repeat if contents were restored
            
            // Show the buffer
            bufferStrategy.show();
            
        } while (bufferStrategy.contentsLost()); // Repeat if contents were lost
        
        // Sync to prevent tearing (helps on some systems)
        Toolkit.getDefaultToolkit().sync();
        
        // Track frame time for performance monitoring
        long renderEnd = System.nanoTime();
        double frameTime = (renderEnd - renderStart) / 1_000_000.0;
        avgFrameTime = avgFrameTime * 0.95 + frameTime * 0.05; // Smooth average
    }
    
    // ========================================
    // KeyboardListener Implementation
    // ========================================
    
    @Override
    public void onKeyPressed(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_ESCAPE -> stop();
            case KeyEvent.VK_F2 -> GameConfig.TUIO_DEBUG = !GameConfig.TUIO_DEBUG;
            case KeyEvent.VK_R -> engine.restart();
        }
    }
    
    @Override
    public void onKeyReleased(int keyCode) {
        // Not used
    }
    
    // ========================================
    // TouchListener Implementation
    // ========================================
    
    @Override
    public void onTouchDown(TouchPoint touch) {
        // Handle any game-start logic
    }
    
    @Override
    public void onTouchMove(TouchPoint touch) {
        // Handled by engine
    }
    
    @Override
    public void onTouchUp(TouchPoint touch) {
        // Handled by engine
    }
    
    // ========================================
    // GameEventListener Implementation
    // ========================================
    
    @Override
    public void onStateChanged(GameEngine.GameState newState) {
        if (GameConfig.TUIO_DEBUG) {
            System.out.println("Game state: " + newState);
        }
    }
    
    @Override
    public void onRoundEnd(Player roundWinner, int round) {
        String winner = roundWinner != null ? roundWinner.getName() : "No one";
        System.out.println("Round " + round + " ended. Winner: " + winner);
    }
    
    @Override
    public void onGameOver(Player winner) {
        System.out.println("Game Over! " + winner.getName() + " wins with " + 
                          winner.getScore() + " points!");
    }
    
    @Override
    public void onSlice(GameObject obj, int playerId, int points, boolean critical) {
        if (GameConfig.TUIO_DEBUG) {
            System.out.println("Player " + playerId + " sliced " + obj.getType().getName() + 
                              " for " + points + " pts" + (critical ? " (CRITICAL!)" : ""));
        }
    }
    
    @Override
    public void onBombHit(int playerId) {
        System.out.println("Player " + playerId + " hit a bomb!");
    }
    
    // ========================================
    // Main Entry Point
    // ========================================
    
    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  SUPER NINJA - Fruit Slicing");
        System.out.println("  Competitive Multitouch Game");
        System.out.println("=================================");
        
        // Parse command line arguments
        for (String arg : args) {
            switch (arg.toLowerCase()) {
                case "-w", "--windowed" -> GameConfig.FULLSCREEN = false;
                case "-d", "--debug" -> GameConfig.TUIO_DEBUG = true;
                case "-h", "--help" -> {
                    System.out.println("SuperNinja - Competitive Fruit Ninja Clone");
                    System.out.println("Usage: java -jar superninja.jar [options]");
                    System.out.println("Options:");
                    System.out.println("  -w, --windowed  Run in windowed mode");
                    System.out.println("  -d, --debug     Enable debug output");
                    System.out.println("  -h, --help      Show this help");
                    System.out.println("\nControls:");
                    System.out.println("  Touch/Mouse    Slice fruits");
                    System.out.println("  ESC            Exit game");
                    System.out.println("  F2             Toggle debug mode");
                    System.out.println("  R              Restart game");
                    System.exit(0);
                }
            }
        }
        
        // Use EDT for Swing components
        SuperNinjaGame game = new SuperNinjaGame();
        game.init();
        game.start();
    }
}
