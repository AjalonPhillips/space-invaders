/**
 * GameModel - Contains all game logic and state.
 * This class has NO Swing imports - it handles pure game logic.
 * 
 * Responsibilities:
 * - Manage game state (score, lives, level, game over)
 * - Handle player ship position and movement
 * - Manage alien positions, movement patterns, and shooting
 * - Handle bullet positions and collisions
 * - Manage game entities (aliens, player, bullets, obstacles)
 * - Provide game loop update logic
 * - Handle win/lose conditions
 */
public class GameModel {
    // ==================== Constants ====================
    private static final int BOARD_WIDTH = 800;
    private static final int BOARD_HEIGHT = 600;
    private static final int ALIEN_ROWS = 5;
    private static final int ALIEN_COLS = 11;
    private static final int ALIEN_WIDTH = 40;
    private static final int ALIEN_HEIGHT = 30;
    private static final int ALIEN_PADDING = 15;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 30;
    private static final int BULLET_WIDTH = 5;
    private static final int BULLET_HEIGHT = 15;
    private static final int PLAYER_SPEED = 8;
    private static final int ALIEN_SPEED = 5;
    private static final int BULLET_SPEED = 10;
    private static final int ALIEN_DROP = 20;
    private static final double ALIEN_FIRE_CHANCE = 0.001; // Per alien per tick
    private static final int BASE_TIMER_INTERVAL = 50; // Base interval in ms
    private static final int MIN_TIMER_INTERVAL = 10; // Fastest interval
    private static final double SPEED_INCREASE_FACTOR = 0.95; // Multiply interval by this each alien hit
    private static final int ALIEN_VERTICAL_MARGIN = 50; // Top margin for alien formation    
    // ==================== Shields ====================
    private static final int NUM_SHIELDS = 4;
    private static final int SHIELD_WIDTH = 60;
    private static final int SHIELD_HEIGHT = 40;
    private static final int SHIELD_MAX_HEALTH = 3;
    private static final int SHIELD_Y = BOARD_HEIGHT - 150;
    
    // ==================== Game State ====================
    private int score;
    private int lives;
    private boolean gameOver;
    private boolean gameWon;
    
    // ==================== Player ====================
    private int playerX;
    private int playerY;
    
    // ==================== Aliens ====================
    private boolean[][] aliens; // [row][col]
    private int alienDirection; // 1 = right, -1 = left
    private int alienMinX;
    private int alienMaxX;
    private int alienY; // Vertical position of the alien formation
    private int aliensDestroyed = 0; // Track for speed increase
    
    // ==================== Player Bullet ====================
    private boolean playerBulletActive;
    private int playerBulletX;
    private int playerBulletY;
    
    // ==================== Alien Bullets ====================
    private static final int MAX_ALIEN_BULLETS = 10;
    private boolean[] alienBulletActive;
    private int[] alienBulletX;
    private int[] alienBulletY;
    
    // ==================== Shields ====================
    private int[] shieldX;
    private int[] shieldY;
    private int[] shieldWidth;
    private int[] shieldHeight;
    private int[] shieldHealth;
    
    // ==================== Constructor ====================
    public GameModel() {
        score = 0;
        lives = 3;
        gameOver = false;
        gameWon = false;
        
        // Initialize player position (bottom center)
        playerX = BOARD_WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = BOARD_HEIGHT - PLAYER_HEIGHT - 10;
        
        // Initialize aliens
        aliens = new boolean[ALIEN_ROWS][ALIEN_COLS];
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                aliens[row][col] = true;
            }
        }
        alienDirection = 1;
        alienMinX = ALIEN_PADDING;
        alienMaxX = BOARD_WIDTH - ALIEN_PADDING - ALIEN_WIDTH;
        alienY = ALIEN_VERTICAL_MARGIN;
        
        // Initialize player bullet (inactive)
        playerBulletActive = false;
        playerBulletX = 0;
        playerBulletY = 0;
        
        // Initialize alien bullets
        alienBulletActive = new boolean[MAX_ALIEN_BULLETS];
        alienBulletX = new int[MAX_ALIEN_BULLETS];
        alienBulletY = new int[MAX_ALIEN_BULLETS];
        for (int i = 0; i < MAX_ALIEN_BULLETS; i++) {
            alienBulletActive[i] = false;
        }
        
        // Initialize shields
        shieldX = new int[NUM_SHIELDS];
        shieldY = new int[NUM_SHIELDS];
        shieldWidth = new int[NUM_SHIELDS];
        shieldHeight = new int[NUM_SHIELDS];
        shieldHealth = new int[NUM_SHIELDS];
        
        int shieldSpacing = BOARD_WIDTH / (NUM_SHIELDS + 1);
        for (int i = 0; i < NUM_SHIELDS; i++) {
            shieldX[i] = shieldSpacing * (i + 1) - SHIELD_WIDTH / 2;
            shieldY[i] = SHIELD_Y;
            shieldWidth[i] = SHIELD_WIDTH;
            shieldHeight[i] = SHIELD_HEIGHT;
            shieldHealth[i] = SHIELD_MAX_HEALTH;
        }
    }
    
    // ==================== Player Movement ====================
    public void movePlayerLeft() {
        if (playerX > 0) {
            playerX -= PLAYER_SPEED;
            if (playerX < 0) playerX = 0;
        }
    }
    
    public void movePlayerRight() {
        if (playerX < BOARD_WIDTH - PLAYER_WIDTH) {
            playerX += PLAYER_SPEED;
            if (playerX > BOARD_WIDTH - PLAYER_WIDTH) {
                playerX = BOARD_WIDTH - PLAYER_WIDTH;
            }
        }
    }
    
    // ==================== Player Shooting ====================
    public void firePlayerBullet() {
        if (!playerBulletActive) {
            playerBulletActive = true;
            playerBulletX = playerX + PLAYER_WIDTH / 2 - BULLET_WIDTH / 2;
            playerBulletY = playerY;
        }
    }
    
    // ==================== Alien Movement ====================
    private boolean shouldAliensMoveDown() {
        // Check if any alien has reached the edge
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                if (aliens[row][col]) {
                    int alienX = getAlienX(col);
                    if (alienDirection == 1 && alienX >= alienMaxX) {
                        return true;
                    }
                    if (alienDirection == -1 && alienX <= alienMinX) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void moveAliens() {
        if (shouldAliensMoveDown()) {
            alienDirection *= -1; // Reverse direction
            alienY += ALIEN_DROP; // Move all aliens down
        } else {
            // Move aliens horizontally based on direction and speed
            // Speed increases as aliens get closer to the bottom
            double speedMultiplier = 1.0 + (alienY / (double)BOARD_HEIGHT) * 2;
            int currentSpeed = (int)(ALIEN_SPEED * speedMultiplier);
            alienMinX += alienDirection * currentSpeed;
            alienMaxX += alienDirection * currentSpeed;
        }
    }
    
    // ==================== Alien Shooting ====================
    private void alienShoot() {
        // Find all living aliens
        java.util.List<int[]> livingAliens = new java.util.ArrayList<>();
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                if (aliens[row][col]) {
                    livingAliens.add(new int[]{row, col});
                }
            }
        }
        
        // Random chance for each alien to fire
        for (int[] alien : livingAliens) {
            if (Math.random() < ALIEN_FIRE_CHANCE) {
                // Find an inactive bullet slot
                for (int i = 0; i < MAX_ALIEN_BULLETS; i++) {
                    if (!alienBulletActive[i]) {
                        alienBulletActive[i] = true;
                        alienBulletX[i] = getAlienX(alien[1]) + ALIEN_WIDTH / 2 - BULLET_WIDTH / 2;
                        alienBulletY[i] = getAlienY(alien[0]) + ALIEN_HEIGHT;
                        break;
                    }
                }
            }
        }
    }
    
    // ==================== Bullet Updates ====================
    private void updatePlayerBullet() {
        if (playerBulletActive) {
            playerBulletY -= BULLET_SPEED;
            if (playerBulletY < 0) {
                playerBulletActive = false;
            }
        }
    }
    
    private void updateAlienBullets() {
        for (int i = 0; i < MAX_ALIEN_BULLETS; i++) {
            if (alienBulletActive[i]) {
                alienBulletY[i] += BULLET_SPEED;
                if (alienBulletY[i] > BOARD_HEIGHT) {
                    alienBulletActive[i] = false;
                }
            }
        }
    }
    
    // ==================== Collision Detection ====================
    private void checkCollisions() {
        // Check player bullet vs aliens
        if (playerBulletActive) {
            for (int row = 0; row < ALIEN_ROWS; row++) {
                for (int col = 0; col < ALIEN_COLS; col++) {
                    if (aliens[row][col]) {
                        int alienX = getAlienX(col);
                        int alienY = getAlienY(row);
                        
                        if (rectIntersect(playerBulletX, playerBulletY, BULLET_WIDTH, BULLET_HEIGHT,
                                          alienX, alienY, ALIEN_WIDTH, ALIEN_HEIGHT)) {
                            // Hit!
                            aliens[row][col] = false;
                            playerBulletActive = false;
                            aliensDestroyed++;
                            score += (ALIEN_ROWS - row) * 10; // Higher rows = more points
                            checkWinCondition();
                            return;
                        }
                    }
                }
            }
        }
        
        // Check alien bullets vs player
        for (int i = 0; i < MAX_ALIEN_BULLETS; i++) {
            if (alienBulletActive[i]) {
                if (rectIntersect(alienBulletX[i], alienBulletY[i], BULLET_WIDTH, BULLET_HEIGHT,
                                  playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT)) {
                    // Player hit!
                    alienBulletActive[i] = false;
                    lives--;
                    if (lives <= 0) {
                        gameOver = true;
                    }
                }
            }
        }
        
        // Check if aliens reached the bottom
        for (int col = 0; col < ALIEN_COLS; col++) {
            for (int row = 0; row < ALIEN_ROWS; row++) {
                if (aliens[row][col]) {
                    int alienY = getAlienY(row);
                    if (alienY + ALIEN_HEIGHT >= playerY) {
                        gameOver = true;
                    }
                }
            }
        }
        
        // Check player bullet vs shields
        if (playerBulletActive) {
            for (int i = 0; i < NUM_SHIELDS; i++) {
                if (shieldHealth[i] > 0 && 
                    rectIntersect(playerBulletX, playerBulletY, BULLET_WIDTH, BULLET_HEIGHT,
                                  shieldX[i], shieldY[i], shieldWidth[i], shieldHeight[i])) {
                    playerBulletActive = false;
                    shieldHealth[i]--;
                    return;
                }
            }
        }
        
        // Check alien bullets vs shields
        for (int i = 0; i < MAX_ALIEN_BULLETS; i++) {
            if (alienBulletActive[i]) {
                for (int s = 0; s < NUM_SHIELDS; s++) {
                    if (shieldHealth[s] > 0 &&
                        rectIntersect(alienBulletX[i], alienBulletY[i], BULLET_WIDTH, BULLET_HEIGHT,
                                      shieldX[s], shieldY[s], shieldWidth[s], shieldHeight[s])) {
                        alienBulletActive[i] = false;
                        shieldHealth[s]--;
                        break;
                    }
                }
            }
        }
    }
    
    private boolean rectIntersect(int x1, int y1, int w1, int h1,
                                   int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 &&
               y1 < y2 + h2 && y1 + h1 > y2;
    }
    
    // ==================== Win/Lose Conditions ====================
    public void checkWinCondition() {
        boolean anyAliensLeft = false;
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                if (aliens[row][col]) {
                    anyAliensLeft = true;
                    break;
                }
            }
        }
        if (!anyAliensLeft) {
            gameWon = true;
        }
    }
    
    // ==================== Main Update Method ====================
    public void update() {
        if (gameOver || gameWon) {
            return;
        }
        
        moveAliens();
        alienShoot();
        updatePlayerBullet();
        updateAlienBullets();
        checkCollisions();
    }
    
    // ==================== Helper Methods ====================
    private int getAlienX(int col) {
        return ALIEN_PADDING + col * (ALIEN_WIDTH + ALIEN_PADDING);
    }
    
    private int getAlienY(int row) {
        return alienY + row * (ALIEN_HEIGHT + ALIEN_PADDING);
    }
    
    // ==================== Getters ====================
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public int getPlayerWidth() { return PLAYER_WIDTH; }
    public int getPlayerHeight() { return PLAYER_HEIGHT; }
    
    public boolean[][] getAliens() { return aliens; }
    public int getAlienWidth() { return ALIEN_WIDTH; }
    public int getAlienHeight() { return ALIEN_HEIGHT; }
    public int getAlienDirection() { return alienDirection; }
    public int getAlienY() { return alienY; }
    
    public boolean isPlayerBulletActive() { return playerBulletActive; }
    public int getPlayerBulletX() { return playerBulletX; }
    public int getPlayerBulletY() { return playerBulletY; }
    public int getBulletWidth() { return BULLET_WIDTH; }
    public int getBulletHeight() { return BULLET_HEIGHT; }
    
    public boolean[] getAlienBulletActive() { return alienBulletActive; }
    public int[] getAlienBulletX() { return alienBulletX; }
    public int[] getAlienBulletY() { return alienBulletY; }
    public int getMaxAlienBullets() { return MAX_ALIEN_BULLETS; }
    
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
    
    public int getBoardWidth() { return BOARD_WIDTH; }
    public int getBoardHeight() { return BOARD_HEIGHT; }
    
    // ==================== Shield Getters ====================
    public int getNumShields() { return NUM_SHIELDS; }
    public int[] getShieldX() { return shieldX; }
    public int[] getShieldY() { return shieldY; }
    public int[] getShieldWidth() { return shieldWidth; }
    public int[] getShieldHeight() { return shieldHeight; }
    public int[] getShieldHealth() { return shieldHealth; }
    
    // ==================== Timer Interval ====================
    /**
     * Returns the recommended timer interval in milliseconds.
     * Decreases as more aliens are destroyed, making the game faster.
     */
    public int getTimerInterval() {
        int interval = (int)(BASE_TIMER_INTERVAL * Math.pow(SPEED_INCREASE_FACTOR, aliensDestroyed));
        return Math.max(interval, MIN_TIMER_INTERVAL);
    }
}