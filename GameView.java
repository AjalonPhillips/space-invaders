import javax.swing.JPanel;
import javax.swing.JFrame;
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
 */
public class GameView extends JPanel {
    // GameView will receive updates from GameController
    // It will render based on GameModel state
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Rendering logic will go here
    }
}