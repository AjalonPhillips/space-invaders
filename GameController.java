import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
public class GameController implements ActionListener, KeyListener {
    private GameModel model;
    private GameView view;
    private JFrame frame;
    private Timer gameTimer;
    
    // Timer interval in milliseconds (60 FPS = ~16ms)
    private static final int TICK_INTERVAL = 16;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameController controller = new GameController();
            controller.init();
        });
    }
    
    private void init() {
        // Create and wire components
        model = new GameModel();
        view = new GameView(model);
        
        // Create and configure the JFrame
        frame = new JFrame("Space Invaders");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set view as content pane directly (no wrapper panel)
        frame.setContentPane(view);
        frame.setResizable(false);
        
        // Pack to get proper size based on view's preferred size
        frame.pack();
        
        view.setFocusable(true);
        view.addKeyListener(this);
        
        frame.setVisible(true);
        
        // Start the game loop
        gameTimer = new Timer(TICK_INTERVAL, this);
        gameTimer.start();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Update the model
        model.update();
        
        // Redraw the view
        view.repaint();
        
        // Stop the timer if game is over or won
        if (model.isGameOver() || model.isGameWon()) {
            gameTimer.stop();
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // Movement: Left arrow or A key
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            model.movePlayerLeft();
        }
        // Movement: Right arrow or D key
        else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            model.movePlayerRight();
        }
        // Fire: Spacebar
        else if (key == KeyEvent.VK_SPACE) {
            model.firePlayerBullet();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        // Not needed for this game
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not needed for this game
    }
}