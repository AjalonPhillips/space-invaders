import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * GameController - Wires together GameModel and GameView.
 * Contains the main method and manages the game loop.
 * 
 * Responsibilities:
 * - Initialize GameModel and GameView
 * - Create and configure the JFrame window
 * - Set up input handling (keyboard listeners)
 * - Run the game loop (timer-based updates)
 * - Coordinate between Model and View
 * - Handle user input and pass to Model
 * - Update View based on Model state changes
 */
public class GameController {
    private GameModel model;
    private GameView view;
    private JFrame frame;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameController controller = new GameController();
            controller.init();
        });
    }
    
    private void init() {
        // Create and wire components
        model = new GameModel();
        view = new GameView();
        
        // Create and configure the JFrame
        frame = new JFrame("Space Invaders");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(view);
        frame.setVisible(true);
    }
}