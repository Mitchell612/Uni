//Mitchell Hartland - 22007285
//Tiernan De Lacy - 22008375
//Caspar Rollo - 21010371

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.io.IOException;


public class Game extends GameEngine {
    private enum GameState {
        MENU, PLAYING, GAME_OVER, HELP, SETTINGS
    }

    private enum Direction {
        LEFT, RIGHT
    }

    private GameState gameState = GameState.MENU;
    private Direction lastDirection = Direction.RIGHT;

    private double elapsedTime = 0; // Tracks time elapsed for score calculations
    private int score = 0; // Tracks the player's score
    private int platformCounter = 0;

    private double playerX = 230;
    private double playerY = 340;
    private final double playerWidth = 40;
    private final double playerHeight = 64;
    private double playerVelocityX = 0;
    private double playerVelocityY = 0;
    private final double gravity = 1.0;
    private final double jumpStrength = -15;
    private boolean onGround = true;
    private boolean canDoubleJump = false;

    private double scrollSpeed = 2.0;
    private ArrayList<Platform> platforms;
    private double platformWidth = 50;
    private double platformHeight = 40;
    private double minPlatformGap = 80;
    private double maxPlatformGap = 140;
    private double lastPlatformX = -1;
    private double minHorizontalGap = 30;

    private Image spriteRight, spriteLeft, spriteJumpRight, spriteJumpLeft, platformSprite, deathZoneSprite, collisionBypassSprite, secondChanceSprite;

    private ArrayList<PowerUp> powerUps;  // List to hold all power-ups
    private boolean collisionBypass = false;  // Flag to manage collision bypass state
    private boolean scrollPause = false;  // Flag to manage scroll pause state

    private boolean secondChanceActive = false;
    private boolean justLanded = false;
    

    public Game(int width, int height) {
        super(width, height);
    }

    @Override
    public void init() {
        setWindowSize(500, 700);
        platforms = new ArrayList<>();
        powerUps = new ArrayList<>();
        loadAudioClips();
        loadSprites();
        generateInitialPlatforms();
    }

    private void loadSprites() {
        spriteRight = loadImage("pictures/sprite_right.png");
        spriteLeft = loadImage("pictures/sprite_left.png");
        spriteJumpRight = loadImage("pictures/sprite_jump_right.png");
        spriteJumpLeft = loadImage("pictures/sprite_jump_left.png");
        platformSprite = loadImage("pictures/platformsprite.png");
        deathZoneSprite = loadImage("pictures/deathzonesprite.png");
        collisionBypassSprite = loadImage("pictures/collisionbypass.png");
        secondChanceSprite = loadImage("pictures/secondchance.png");
    }

    private Clip jumpClip, landClip, dieClip;

private void loadAudioClips() {
    try {
        jumpClip = loadClip("sounds/jump.wav");
        landClip = loadClip("sounds/land.wav");
        dieClip = loadClip("sounds/die.wav");
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private Clip loadClip(String filePath) throws IOException {
    try {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath));
        Clip clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        return clip;
    } catch (Exception e) {
        e.printStackTrace();
        throw new IOException("Failed to load audio clip: " + filePath);
    }
}

private void playClip(Clip clip) {
    if (clip != null) {
        clip.setFramePosition(0);
        clip.start();
    }
}

    public void relocatePlayerToTop() {
        playerY = 0;  // Move player to the top of the game screen
        playerVelocityY = 0;  // Reset vertical velocity
    }
    public void activateSecondChance() {
        secondChanceActive = true;
    }

    private void spawnPowerUpOnPlatform(Platform platform) {
        double x = platform.getX() + (platform.getWidth() - 30) / 2; // Adjust to center on platform
        double y = platform.getY() - 75;
    
        int randomChoice = (int) (Math.random() * 2);  // Randomly choose between two power-ups
    
        PowerUp newPowerUp;
        if (randomChoice == 0) {
            newPowerUp = new CollisionBypassPowerUp(x, y, collisionBypassSprite, platform);
        } else {
            newPowerUp = new SecondChancePowerUp(x, y, secondChanceSprite, platform);
        }
        powerUps.add(newPowerUp);
        System.out.println("Spawned Power-Up: " + newPowerUp.getClass().getSimpleName() + " at (" + x + ", " + y + ")");
    }
    
    public void setSecondChanceActive(boolean active) {
        secondChanceActive = active;
    }
    

    private void generateInitialPlatforms() {
        double startingPlatformX = playerX - (playerWidth);
        double startingPlatformY = playerY + playerHeight;  // Just below the player

        platforms.add(new Platform(startingPlatformX, startingPlatformY, platformWidth, platformHeight));
        double y = startingPlatformY - minPlatformGap - playerHeight;
        while (y > 0) {
            double x;
            do {
                x = Math.random() * (mWidth - platformWidth);
            } while (lastPlatformX != -1 && Math.abs(lastPlatformX - x) < minHorizontalGap);
            lastPlatformX = x;
            platforms.add(new Platform(x, y, platformWidth, platformHeight));
            y -= playerHeight + 30 + Math.random() * (maxPlatformGap - minPlatformGap);
        }
    }

    private void generateNewPlatforms() {
        // Generate new platforms when the last platform is about to go off-screen
        if (platforms.get(platforms.size() - 1).getY() > playerY - mHeight) {
            double newPlatformY = platforms.get(platforms.size() - 1).getY() - minPlatformGap - Math.random() * (maxPlatformGap - minPlatformGap);
            double randomWidth = platformWidth + Math.random() * (platformWidth * 3.00); // Random width up to double the default
            double newPlatformX = Math.random() * (mWidth - randomWidth);
    
            Platform newPlatform = new Platform(newPlatformX, newPlatformY, randomWidth, platformHeight);
            platforms.add(newPlatform);
            System.out.println("Generated new platform at (" + newPlatformX + ", " + newPlatformY + ")");
    
            // Increment the platform counter
            platformCounter++;
    
            // Check if this is the 10th platform
            if (platformCounter == 10) {
                spawnPowerUpOnPlatform(newPlatform);
                platformCounter = 0;  // Reset the counter after spawning a power-up
            }
        }
    }
    
    
    private void LoadGame() {
        playerX = 200;
        playerY = 316; // Adjust so the player starts on the platform
        playerVelocityX = 0;
        playerVelocityY = 0;
        scrollSpeed = 2.0; // Reset scroll speed
        elapsedTime = 0; // Reset elapsed time
        score = 0; // Reset score
        platforms.clear();
        platforms.add(new Platform(200, 380, platformWidth, platformHeight));
        generateInitialPlatforms();
        gameState = GameState.PLAYING;
    }

    @Override
    public void update(double dt) {
        if (gameState == GameState.PLAYING) {
            updateGame(dt);
        }
    }

    private void updateGame(double dt) {
        elapsedTime += dt;
        scrollSpeed += dt * 0.05;
        score = (int) (Math.pow(elapsedTime, 2) * scrollSpeed);
    
        playerVelocityY += gravity;
        if (!scrollPause) {
            playerX += playerVelocityX;
            playerY += playerVelocityY;
        }
    
        for (Platform platform : platforms) {
            platform.setY(platform.getY() + scrollSpeed);
    
            // Update positions of power-ups that are on this platform
            for (PowerUp powerUp : powerUps) {
                if (powerUp.getPlatform() == platform) {
                    powerUp.setY(powerUp.getY() + scrollSpeed);
                }
            }
        }
    
        // Check for power-up collection
        Iterator<PowerUp> it = powerUps.iterator();
        while (it.hasNext()) {
            PowerUp powerUp = it.next();
            if (Math.abs(powerUp.getX() - playerX) < playerWidth &&
                Math.abs(powerUp.getY() - playerY) < playerHeight &&
                playerX + playerWidth > powerUp.getX() &&
                playerX < powerUp.getX() + powerUp.getWidth() &&
                playerY + playerHeight > powerUp.getY() &&
                playerY < powerUp.getY() + powerUp.getHeight()) {
                powerUp.applyEffect(this);
                it.remove();
                System.out.println("Collected Power-Up: " + powerUp.getClass().getSimpleName());
            }
        }
    
        playerY += scrollSpeed;
        generateNewPlatforms();
        platforms.removeIf(platform -> platform.getY() > mHeight);
    
        if (playerX < 0) playerX = 0;
        if (playerX + playerWidth > mWidth) playerX = mWidth - playerWidth;
    
        onGround = false;
        for (Platform platform : platforms) {
            // Normal collision detection when moving down
            if (playerVelocityY > 0 && playerY + playerHeight >= platform.getY() && playerY + playerHeight <= platform.getY() + platform.getHeight()
            && playerX + playerWidth > platform.getX() && playerX < platform.getX() + platform.getWidth()) {
                if (!justLanded) {
                    playClip(landClip);
                    justLanded = true;
                }
                playerY = platform.getY() - playerHeight;
                playerVelocityY = 0;
                onGround = true;
                canDoubleJump = false;
                break;
            }
    
            // Bypass collision when moving up and collisionBypass is active
            if (playerVelocityY < 0 && collisionBypass && playerY <= platform.getY() + platform.getHeight() && playerY >= platform.getY()
                    && playerX + playerWidth > platform.getX() && playerX < platform.getX() + platform.getWidth()) {
                // Do nothing, bypass collision
            } else {
                // Normal collision detection for other cases
                if (playerY <= platform.getY() + platform.getHeight() && playerY >= platform.getY()
                        && playerX + playerWidth > platform.getX() && playerX < platform.getX() + platform.getWidth()) {
                    playerY = platform.getY() + platform.getHeight();
                    playerVelocityY = 0;
                }
    
                if (playerX + playerWidth >= platform.getX() && playerX < platform.getX()
                        && playerY + playerHeight > platform.getY() && playerY < platform.getY() + platform.getHeight()) {
                    playerX = platform.getX() - playerWidth;
                    playerVelocityX = 0;
                }
    
                if (playerX <= platform.getX() + platform.getWidth() && playerX + playerWidth > platform.getX() + platform.getWidth()
                        && playerY + playerHeight > platform.getY() && playerY < platform.getY() + platform.getHeight()) {
                    playerX = platform.getX() + platform.getWidth();
                    playerVelocityX = 0;
                }
            }
        }
    
        checkGameOver();
    }

    private void checkGameOver() {
        if (playerY > mHeight) {
            if (secondChanceActive) {
                relocatePlayerToTop();  // Move player to the top of the game screen
                secondChanceActive = false;  // Consume the second chance
            } else {
                playClip(dieClip);
                gameState = GameState.GAME_OVER;
            }
        }
    }

    public void setCollisionBypass(boolean bypass) {
        collisionBypass = bypass;
    }

    public void setScrollPause(boolean pause) {
        scrollPause = pause;
    }

    public void relocatePlayerToHighestPlatform() {
        // Find the highest platform that is below the current player Y position
        Platform highest = null;
        for (Platform platform : platforms) {
            if ((highest == null || platform.getY() < highest.getY()) && platform.getY() > playerY) {
                highest = platform;
            }
        }

        if (highest != null) {
            playerY = highest.getY() - playerHeight;  // Place the player on top of the highest platform
        }
    }

    @Override
    public void paintComponent() {
        clearBackground(mWidth, mHeight);

        switch (gameState) {
            case MENU:
                drawMenu();
                break;
            case PLAYING:
                drawGame();
                break;
            case GAME_OVER:
                drawGameOver();
                break;
            case HELP:
                drawHelp();
                break;
            case SETTINGS:
                drawSettings();
                break;
        }
    }
        private void drawMenu() {
            changeBackgroundColor(Color.BLACK);
            clearBackground(mWidth, mHeight);
            changeColor(Color.WHITE);
            drawBoldText(20, mHeight / 2 - 60, "Press P to Play", "Arial", 30);
            drawBoldText(20, mHeight / 2, "Press H for Help", "Arial", 30);
            drawBoldText(20, mHeight / 2 + 60, "Press S for Settings", "Arial", 30);
        }

        private void drawGame() {
            changeBackgroundColor(Color.BLACK);
            clearBackground(mWidth, mHeight);
            changeColor(Color.GRAY);
        
            for (Platform platform : platforms) {
                drawImage(platformSprite, platform.getX(), platform.getY(), platform.getWidth(), platform.getHeight());
            }
        
            drawImage(deathZoneSprite, 0, mHeight - 20, mWidth, 20);
        
            for (PowerUp powerUp : powerUps) {
                drawImage(powerUp.getSprite(), powerUp.getX(), powerUp.getY(), powerUp.getWidth(), powerUp.getHeight());
            }
        
            Image playerSprite = getPlayerSprite();
            drawImage(playerSprite, playerX, playerY, playerWidth, playerHeight);
            changeColor(Color.WHITE);
            drawText(10, 20, "Score: " + score, "Arial", 15);
        }

    private void drawGameOver() {
        changeBackgroundColor(Color.BLACK);
        clearBackground(mWidth, mHeight);
        changeColor(Color.WHITE);
        drawBoldText(mWidth / 2 - 100, mHeight / 2, "Game Over! Press R to Restart", "Arial", 20);
    }

    private Image getPlayerSprite() {
        if (playerVelocityY != 0) {
            return lastDirection == Direction.RIGHT ? spriteJumpRight : spriteJumpLeft;
        } else {
            return lastDirection == Direction.RIGHT ? spriteRight : spriteLeft;
        }
    }

    private void drawHelp() {
        changeBackgroundColor(Color.BLACK);
        clearBackground(mWidth, mHeight);
        changeColor(Color.WHITE);
        drawBoldText(20, mHeight / 2 - 30, "Help coming soon", "Arial", 30);
        drawBoldText(20, mHeight / 2 + 30, "Press ESC to return", "Arial", 20);
    }

    private void drawSettings() {
        changeBackgroundColor(Color.BLACK);
        clearBackground(mWidth, mHeight);
        changeColor(Color.WHITE);
        drawBoldText(20, mHeight / 2 - 30, "Settings", "Arial", 30);
        drawBoldText(20, mHeight / 2 + 30, "Press ESC to return", "Arial", 20);
    }

    @Override
public void keyPressed(KeyEvent event) {
    switch (gameState) {
        case MENU:
            if (event.getKeyCode() == KeyEvent.VK_P) {
                LoadGame();
                gameState = GameState.PLAYING;
            } else if (event.getKeyCode() == KeyEvent.VK_H) {
                gameState = GameState.HELP;
            } else if (event.getKeyCode() == KeyEvent.VK_S) {
                gameState = GameState.SETTINGS;
            }
            break;
        case PLAYING:
            if (event.getKeyCode() == KeyEvent.VK_SPACE || event.getKeyCode() == KeyEvent.VK_UP) {
                if (onGround || canDoubleJump) {
                    playerVelocityY = jumpStrength;
                    onGround = false;
                    justLanded = false; // Reset landing flag on jump
                    canDoubleJump = !onGround;
                    playClip(jumpClip);
                }
            } else if (event.getKeyCode() == KeyEvent.VK_LEFT) {
                playerVelocityX = -5;
                lastDirection = Direction.LEFT;
            } else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
                playerVelocityX = 5;
                lastDirection = Direction.RIGHT;
            } else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
                if (!onGround) {
                    playerVelocityY = 10; // Faster fall
                }
            } else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                gameState = GameState.MENU;
            }
            break;
        case GAME_OVER:
            if (event.getKeyCode() == KeyEvent.VK_R) {
                LoadGame();
            } else if (event.getKeyCode() == KeyEvent.VK_M) {
                gameState = GameState.MENU;
            }
            break;
        case HELP:
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                gameState = GameState.MENU;
            }
            break;
        case SETTINGS:
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                gameState = GameState.MENU;
            }
            break;
    }
}

    @Override
    public void keyReleased(KeyEvent event) {
        if (gameState == GameState.PLAYING) {
            if (event.getKeyCode() == KeyEvent.VK_LEFT || event.getKeyCode() == KeyEvent.VK_RIGHT) {
                playerVelocityX = 0;
            }
        }
    }

    public static void main(String[] args) {
        Game game = new Game(500, 500);
        createGame(game, 30);
    }
}