import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DotsAndBoxesGame extends JFrame {
    private GamePanel gamePanel;
    private JLabel statusLabel;

    public DotsAndBoxesGame() {
        setTitle("Dots and Boxes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel();
        statusLabel = new JLabel("Player 1's turn (Blue)");

        add(gamePanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        gamePanel.setStatusLabel(statusLabel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new DotsAndBoxesGame();
    }
}

class GamePanel extends JPanel implements MouseListener {
    private final int gridSize = 6; // Number of dots per row and column
    private final int dotSize = 10;
    private final int padding = 40;
    private final int cellSize = 60;

    private boolean[][] hLines, vLines;
    private int[][] boxes; // 0 - unclaimed, 1 - player 1, 2 - player 2
    private int currentPlayer = 1;
    private int[] scores = {0, 0}; // Index 0 for Player 1, 1 for Player 2
    private JLabel statusLabel;

    public GamePanel() {
        setPreferredSize(new Dimension(
                padding * 2 + cellSize * (gridSize - 1),
                padding * 2 + cellSize * (gridSize - 1)
        ));
        setBackground(Color.WHITE);
        addMouseListener(this);

        hLines = new boolean[gridSize][gridSize - 1];
        vLines = new boolean[gridSize - 1][gridSize];
        boxes = new int[gridSize - 1][gridSize - 1];
    }

    public void setStatusLabel(JLabel label) {
        this.statusLabel = label;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw dots
        g.setColor(Color.BLACK);
        for (int i = 0; i < gridSize; i++) {
            int y = padding + i * cellSize;
            for (int j = 0; j < gridSize; j++) {
                int x = padding + j * cellSize;
                g.fillOval(x - dotSize / 2, y - dotSize / 2, dotSize, dotSize);
            }
        }

        // Draw horizontal lines
        for (int i = 0; i < gridSize; i++) {
            int y = padding + i * cellSize;
            for (int j = 0; j < gridSize - 1; j++) {
                int x = padding + j * cellSize;
                if (hLines[i][j]) {
                    g.setColor(Color.BLUE); // Blue lines for Player 1
                    g.drawLine(x, y, x + cellSize, y);
                }
            }
        }

        // Draw vertical lines
        for (int i = 0; i < gridSize - 1; i++) {
            int y = padding + i * cellSize;
            for (int j = 0; j < gridSize; j++) {
                int x = padding + j * cellSize;
                if (vLines[i][j]) {
                    g.setColor(Color.RED); // Red lines for Player 2
                    g.drawLine(x, y, x, y + cellSize);
                }
            }
        }

        // Draw boxes
        for (int i = 0; i < gridSize - 1; i++) {
            int y = padding + i * cellSize;
            for (int j = 0; j < gridSize - 1; j++) {
                int x = padding + j * cellSize;
                if (boxes[i][j] != 0) {
                    if (boxes[i][j] == 1) {
                        g.setColor(new Color(135, 206, 250)); // Light Blue
                    } else {
                        g.setColor(new Color(255, 182, 193)); // Light Pink
                    }
                    g.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                    g.setColor(Color.BLACK);
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int xClick = e.getX();
        int yClick = e.getY();

        int row = (yClick - padding + cellSize / 2) / cellSize;
        int col = (xClick - padding + cellSize / 2) / cellSize;

        if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) return;

        boolean madeMove = false;
        boolean boxCompleted = false;

        // Horizontal line click detection
        if (Math.abs((yClick - padding) % cellSize) < 10 && col < gridSize - 1) {
            if (!hLines[row][col]) {
                hLines[row][col] = true;
                madeMove = true;
                boxCompleted = checkForBoxes(row, col, true);
            }
        }
        // Vertical line click detection
        else if (Math.abs((xClick - padding) % cellSize) < 10 && row < gridSize - 1) {
            if (!vLines[row][col]) {
                vLines[row][col] = true;
                madeMove = true;
                boxCompleted = checkForBoxes(row, col, false);
            }
        }

        if (madeMove) {
            repaint();
            if (!boxCompleted) {
                currentPlayer = (currentPlayer == 1) ? 2 : 1;
            }
            updateStatus();
            checkForGameOver();
        }
    }

    private boolean checkForBoxes(int row, int col, boolean isHorizontal) {
        boolean boxMade = false;

        if (isHorizontal) {
            // Check above
            if (row > 0 && hLines[row - 1][col] && vLines[row - 1][col] && vLines[row - 1][col + 1]) {
                if (boxes[row - 1][col] == 0) {
                    boxes[row - 1][col] = currentPlayer;
                    scores[currentPlayer - 1]++;
                    boxMade = true;
                }
            }
            // Check below
            if (row < gridSize - 1 && hLines[row + 1][col] && vLines[row][col] && vLines[row][col + 1]) {
                if (boxes[row][col] == 0) {
                    boxes[row][col] = currentPlayer;
                    scores[currentPlayer - 1]++;
                    boxMade = true;
                }
            }
        } else {
            // Check left
            if (col > 0 && vLines[row][col - 1] && hLines[row][col - 1] && hLines[row + 1][col - 1]) {
                if (boxes[row][col - 1] == 0) {
                    boxes[row][col - 1] = currentPlayer;
                    scores[currentPlayer - 1]++;
                    boxMade = true;
                }
            }
            // Check right
            if (col < gridSize - 1 && vLines[row][col + 1] && hLines[row][col] && hLines[row + 1][col]) {
                if (boxes[row][col] == 0) {
                    boxes[row][col] = currentPlayer;
                    scores[currentPlayer - 1]++;
                    boxMade = true;
                }
            }
        }

        return boxMade;
    }

    private void updateStatus() {
        if (statusLabel != null) {
            String playerText = (currentPlayer == 1) ? "Player 1's turn (Blue)" : "Player 2's turn (Red)";
            statusLabel.setText(String.format("%s | Scores - Player 1: %d, Player 2: %d",
                    playerText, scores[0], scores[1]));
        }
    }

    private void checkForGameOver() {
        int totalBoxes = (gridSize - 1) * (gridSize - 1);
        int claimedBoxes = scores[0] + scores[1];

        if (claimedBoxes == totalBoxes) {
            String winner;
            if (scores[0] > scores[1]) {
                winner = "Player 1 wins!";
            } else if (scores[0] < scores[1]) {
                winner = "Player 2 wins!";
            } else {
                winner = "It's a tie!";
            }
            int response = JOptionPane.showConfirmDialog(this,
                    String.format("Game Over!\n%s\nDo you want to play again?", winner),
                    "Game Over",
                    JOptionPane.YES_NO_OPTION);

            if (response == JOptionPane.YES_OPTION) {
                resetGame();
            } else {
                System.exit(0);
            }
        }
    }

    private void resetGame() {
        hLines = new boolean[gridSize][gridSize - 1];
        vLines = new boolean[gridSize - 1][gridSize];
        boxes = new int[gridSize - 1][gridSize - 1];
        currentPlayer = 1;
        scores = new int[]{0, 0};
        updateStatus();
        repaint();
    }

    // Unused mouse events
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
