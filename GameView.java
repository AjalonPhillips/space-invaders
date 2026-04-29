import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

/**
 * GameView - Handles all rendering and display.
 * This class extends JPanel and is hosted in a JFrame.
 * 
 * Responsibilities:
 * - Render the game board and background
 * - Draw player ship
 * - Draw aliens and their animations
 * - Draw bullets
 * - Draw score, lives, and level information
 * - Draw game over / victory screens
 * - Handle visual effects (explosions, etc.)
 * - Provide paintComponent method for Swing rendering
 * 
 * NOTE: This class only READS from the model - it never modifies game state.
 */
public class GameView extends JPanel {
    private GameModel model;

    // Colors
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color PLAYER_COLOR = Color.GREEN;
    private static final Color ALIEN_COLOR = Color.CYAN;
    private static final Color PLAYER_BULLET_COLOR = Color.YELLOW;
    private static final Color ALIEN_BULLET_COLOR = Color.RED;
    private static final Color TEXT_COLOR = Color.WHITE;

    // Fonts
    private static final Font SCORE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Font GAME_OVER_FONT = new Font("Arial", Font.BOLD, 48);

    public GameView(GameModel model) {
        this.model = model;
        setBackground(BACKGROUND_COLOR);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(model.getBoardWidth(), model.getBoardHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw score and lives (top of screen)
        drawHUD(g);

        // Draw aliens
        drawAliens(g);

        // Draw shields
        drawShields(g);

        // Draw player
        drawPlayer(g);

        // Draw bullets
        drawBullets(g);

        // Draw game over / victory message
        if (model.isGameOver() || model.isGameWon()) {
            drawGameOverMessage(g);
        } else if (model.isPaused()) {
            drawPauseMenu(g);
        }
    }

    private void drawHUD(Graphics g) {
        g.setColor(TEXT_COLOR);
        g.setFont(SCORE_FONT);

        // Draw score on left
        g.drawString("Score: " + model.getScore(), 20, 30);

        // Draw instruction in center
        String instr = "ESC to pause";
        int instrWidth = g.getFontMetrics().stringWidth(instr);
        g.drawString(instr, (getWidth() - instrWidth) / 2, 30);

        // Draw lives on right
        g.drawString("Lives: " + model.getLives(), getWidth() - 120, 30);
    }

    private void drawPlayer(Graphics g) {
        g.setColor(PLAYER_COLOR);
        int x = model.getPlayerX();
        int y = model.getPlayerY();
        int w = model.getPlayerWidth();
        int h = model.getPlayerHeight();

        // Draw player as a simple shape (triangle-ish)
        int[] xPoints = {
                x + w / 2, // Top center
                x, // Bottom left
                x + w // Bottom right
        };
        int[] yPoints = {
                y, // Top
                y + h, // Bottom left
                y + h // Bottom right
        };
        g.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawAliens(Graphics g) {
        g.setColor(ALIEN_COLOR);
        boolean[][] aliens = model.getAliens();
        int alienWidth = model.getAlienWidth();
        int alienHeight = model.getAlienHeight();

        for (int row = 0; row < aliens.length; row++) {
            for (int col = 0; col < aliens[row].length; col++) {
                if (aliens[row][col]) {
                    int x = model.getAlienX(col);
                    int y = model.getAlienY(row);

                    // Draw alien as a rectangle with some character
                    g.fillRect(x, y, alienWidth, alienHeight);

                    // Add simple eyes
                    g.setColor(BACKGROUND_COLOR);
                    g.fillRect(x + 8, y + 8, 6, 6);
                    g.fillRect(x + alienWidth - 14, y + 8, 6, 6);
                    g.setColor(ALIEN_COLOR);
                }
            }
        }
    }

    private void drawShields(Graphics g) {
        int numShields = model.getNumShields();
        int[] shieldX = model.getShieldX();
        int[] shieldY = model.getShieldY();
        int[] shieldWidth = model.getShieldWidth();
        int[] shieldHeight = model.getShieldHeight();
        int[] shieldHealth = model.getShieldHealth();

        for (int i = 0; i < numShields; i++) {
            if (shieldHealth[i] > 0) {
                // Color transitions from green (full health) to red (low health)
                // Health 3 = green, 2 = yellow, 1 = red
                Color shieldColor;
                if (shieldHealth[i] == 3) {
                    shieldColor = new Color(0, 255, 0);
                } else if (shieldHealth[i] == 2) {
                    shieldColor = new Color(255, 255, 0);
                } else {
                    shieldColor = new Color(139, 0, 0); // Dim red
                }

                g.setColor(shieldColor);
                g.fillRect(shieldX[i], shieldY[i], shieldWidth[i], shieldHeight[i]);
            }
        }
    }

    private void drawBullets(Graphics g) {
        int w = model.getBulletWidth();
        int h = model.getBulletHeight();

        // Draw player bullets
        g.setColor(PLAYER_BULLET_COLOR);
        boolean[] playerActive = model.getPlayerBulletActive();
        int[] playerBulletX = model.getPlayerBulletX();
        int[] playerBulletY = model.getPlayerBulletY();

        for (int i = 0; i < playerActive.length; i++) {
            if (playerActive[i]) {
                g.fillRect(playerBulletX[i], playerBulletY[i], w, h);
            }
        }

        // Draw alien bullets
        g.setColor(ALIEN_BULLET_COLOR);
        boolean[] alienActive = model.getAlienBulletActive();
        int[] alienBulletX = model.getAlienBulletX();
        int[] alienBulletY = model.getAlienBulletY();

        for (int i = 0; i < alienActive.length; i++) {
            if (alienActive[i]) {
                g.fillRect(alienBulletX[i], alienBulletY[i], w, h);
            }
        }
    }

    private void drawGameOverMessage(Graphics g) {
        g.setColor(TEXT_COLOR);
        g.setFont(GAME_OVER_FONT);

        String message;
        if (model.isGameWon()) {
            message = "YOU WIN!";
        } else {
            message = "GAME OVER";
        }

        // Center the message
        int messageWidth = g.getFontMetrics().stringWidth(message);
        int x = (getWidth() - messageWidth) / 2;
        int y = getHeight() / 2;

        g.drawString(message, x, y);

        // Draw final score
        g.setFont(SCORE_FONT);
        String finalScore = "Final Score: " + model.getScore();
        int scoreWidth = g.getFontMetrics().stringWidth(finalScore);
        int scoreX = (getWidth() - scoreWidth) / 2;
        g.drawString(finalScore, scoreX, y + 40);

        // Draw restart instruction
        String restartText = "Press R or ENTER to restart";
        int restartWidth = g.getFontMetrics().stringWidth(restartText);
        int restartX = (getWidth() - restartWidth) / 2;
        g.drawString(restartText, restartX, y + 80);
    }

    private void drawPauseMenu(Graphics g) {
        // Semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(TEXT_COLOR);
        g.setFont(GAME_OVER_FONT);
        String title = "PAUSED";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (getWidth() - titleWidth) / 2, getHeight() / 2 - 40);

        g.setFont(SCORE_FONT);
        String resume = "Press ESC to Resume";
        String restart = "Press R to Restart";

        int resumeWidth = g.getFontMetrics().stringWidth(resume);
        int restartWidth = g.getFontMetrics().stringWidth(restart);

        g.drawString(resume, (getWidth() - resumeWidth) / 2, getHeight() / 2 + 20);
        g.drawString(restart, (getWidth() - restartWidth) / 2, getHeight() / 2 + 60);
    }

    // Positioning logic is now handled by the model.
}