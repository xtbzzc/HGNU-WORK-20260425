import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GomokuGame extends JFrame {
    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 36;
    private static final int PADDING = 32;
    private static final int STONE_SIZE = 28;
    private static final int TURN_TIME_LIMIT = 20;

    private final int[][] board = new int[BOARD_SIZE][BOARD_SIZE]; // 0=empty,1=black,2=white
    private int currentPlayer = 1;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private int remainingSeconds = TURN_TIME_LIMIT;

    private final JLabel statusLabel = new JLabel("点击“开始下棋”后，黑棋先手");
    private final JLabel infoLabel = new JLabel("当前颜色：- | 剩余时间：20s");
    private final JButton startBtn = new JButton("开始下棋");
    private final BoardPanel boardPanel = new BoardPanel();

    private final Timer turnTimer = new Timer(1000, e -> {
        if (!gameStarted || gameOver) {
            return;
        }

        remainingSeconds--;
        updateInfoLabel();

        if (remainingSeconds <= 0) {
            gameOver = true;
            String loser = currentPlayer == 1 ? "黑棋" : "白棋";
            String winner = currentPlayer == 1 ? "白棋" : "黑棋";
            statusLabel.setText(loser + "超时，" + winner + "获胜！");
            JOptionPane.showMessageDialog(
                    GomokuGame.this,
                    loser + "超时未落子，" + winner + "获胜！",
                    "游戏结束",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    });

    public GomokuGame() {
        setTitle("五子棋 (Java Swing)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));
        row1.add(statusLabel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton restartBtn = new JButton("重新开始");
        actionPanel.add(startBtn);
        actionPanel.add(restartBtn);
        row1.add(actionPanel, BorderLayout.EAST);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        row2.add(infoLabel);

        topPanel.add(row1);
        topPanel.add(row2);

        startBtn.addActionListener(e -> startGame());
        restartBtn.addActionListener(e -> resetBoardToPendingStart());

        add(topPanel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        turnTimer.setInitialDelay(1000);
    }

    private void startGame() {
        if (gameOver) {
            return;
        }
        gameStarted = true;
        startBtn.setEnabled(false);
        remainingSeconds = TURN_TIME_LIMIT;
        statusLabel.setText("游戏开始，当前回合：黑棋");
        updateInfoLabel();
        if (!turnTimer.isRunning()) {
            turnTimer.start();
        }
    }

    private void resetBoardToPendingStart() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c] = 0;
            }
        }
        currentPlayer = 1;
        gameOver = false;
        gameStarted = false;
        remainingSeconds = TURN_TIME_LIMIT;
        startBtn.setEnabled(true);
        statusLabel.setText("点击“开始下棋”后，黑棋先手");
        updateInfoLabel();
        boardPanel.repaint();
        turnTimer.stop();
    }

    private void updateInfoLabel() {
        String currentColor = gameStarted && !gameOver ? (currentPlayer == 1 ? "黑棋" : "白棋") : "-";
        infoLabel.setText("当前颜色：" + currentColor + " | 剩余时间：" + remainingSeconds + "s");
    }

    private boolean checkWin(int row, int col) {
        int player = board[row][col];
        int[][] directions = {
                {0, 1},  // 横向
                {1, 0},  // 纵向
                {1, 1},  // 主对角线
                {1, -1}  // 副对角线
        };

        for (int[] d : directions) {
            int count = 1;
            count += countContinuous(row, col, d[0], d[1], player);
            count += countContinuous(row, col, -d[0], -d[1], player);
            if (count >= 5) {
                return true;
            }
        }
        return false;
    }

    private int countContinuous(int row, int col, int dr, int dc, int player) {
        int count = 0;
        int r = row + dr;
        int c = col + dc;
        while (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == player) {
            count++;
            r += dr;
            c += dc;
        }
        return count;
    }

    private boolean isBoardFull() {
        for (int[] rows : board) {
            for (int cell : rows) {
                if (cell == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private class BoardPanel extends JPanel {
        public BoardPanel() {
            int side = PADDING * 2 + (BOARD_SIZE - 1) * CELL_SIZE;
            setPreferredSize(new Dimension(side, side));
            setBackground(new Color(235, 196, 126));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!gameStarted || gameOver) return;

                    int x = e.getX();
                    int y = e.getY();

                    int col = Math.round((x - PADDING) / (float) CELL_SIZE);
                    int row = Math.round((y - PADDING) / (float) CELL_SIZE);

                    if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
                        return;
                    }

                    int cx = PADDING + col * CELL_SIZE;
                    int cy = PADDING + row * CELL_SIZE;
                    if (Math.abs(x - cx) > CELL_SIZE / 2 || Math.abs(y - cy) > CELL_SIZE / 2) {
                        return;
                    }

                    if (board[row][col] != 0) return;

                    board[row][col] = currentPlayer;
                    repaint();

                    if (checkWin(row, col)) {
                        gameOver = true;
                        turnTimer.stop();
                        statusLabel.setText((currentPlayer == 1 ? "黑棋" : "白棋") + "获胜！");
                        updateInfoLabel();
                        JOptionPane.showMessageDialog(
                                GomokuGame.this,
                                (currentPlayer == 1 ? "黑棋" : "白棋") + "获胜！",
                                "游戏结束",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }

                    if (isBoardFull()) {
                        gameOver = true;
                        turnTimer.stop();
                        statusLabel.setText("平局");
                        updateInfoLabel();
                        JOptionPane.showMessageDialog(
                                GomokuGame.this,
                                "棋盘已满，平局！",
                                "游戏结束",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }

                    currentPlayer = (currentPlayer == 1) ? 2 : 1;
                    remainingSeconds = TURN_TIME_LIMIT;
                    statusLabel.setText("当前回合：" + (currentPlayer == 1 ? "黑棋" : "白棋"));
                    updateInfoLabel();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 画网格
            g2.setColor(new Color(86, 55, 20));
            for (int i = 0; i < BOARD_SIZE; i++) {
                int pos = PADDING + i * CELL_SIZE;
                g2.drawLine(PADDING, pos, PADDING + (BOARD_SIZE - 1) * CELL_SIZE, pos);
                g2.drawLine(pos, PADDING, pos, PADDING + (BOARD_SIZE - 1) * CELL_SIZE);
            }

            // 画棋子
            for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    if (board[r][c] == 0) continue;

                    int x = PADDING + c * CELL_SIZE - STONE_SIZE / 2;
                    int y = PADDING + r * CELL_SIZE - STONE_SIZE / 2;

                    if (board[r][c] == 1) {
                        g2.setColor(Color.BLACK);
                        g2.fillOval(x, y, STONE_SIZE, STONE_SIZE);
                    } else {
                        g2.setColor(Color.WHITE);
                        g2.fillOval(x, y, STONE_SIZE, STONE_SIZE);
                        g2.setColor(Color.GRAY);
                        g2.drawOval(x, y, STONE_SIZE, STONE_SIZE);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GomokuGame().setVisible(true));
    }
}
