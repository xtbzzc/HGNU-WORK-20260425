import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Random;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class MinesweeperGame extends JFrame {

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;

    private int rows = 16;
    private int cols = 16;
    private int mineCount = 40;

    private Cell[][] cells;
    private MineButton[][] buttons;

    private JPanel boardPanel;

    private boolean gameOver = false;
    private boolean firstClick = true;

    private ImageIcon mineIcon;
    private ImageIcon loseIcon;

    // 音乐
    private MediaPlayer mediaPlayer;

    private String[] musicFiles = {
            "C:\\Users\\27987\\Downloads\\张叶蕾 - 还是分开.mp3",
            "C:\\Users\\27987\\Downloads\\雨爱-杨丞琳.mp3",
            "C:\\Users\\27987\\Downloads\\杨丞琳 - 带我走.mp3",
            "C:\\Users\\27987\\Downloads\\薛之谦 - 动物世界.mp3",
            "C:\\Users\\27987\\Downloads\\恋人-李荣浩.mp3"
    };

    public MinesweeperGame() {

        setTitle("Java 扫雷游戏");

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationRelativeTo(null);

        // 初始化 JavaFX
        new JFXPanel();

        initIcons();

        JPanel topPanel = new JPanel();

        // 重新开始
        JButton resetButton = new JButton("重新游戏");

        resetButton.setFont(
                new Font("微软雅黑", Font.BOLD, 16)
        );

        resetButton.addActionListener(e -> initializeGame());

        topPanel.add(resetButton);

        // 难度
        String[] difficulties = {
                "初级",
                "中级",
                "高级"
        };

        JComboBox<String> difficultyBox =
                new JComboBox<>(difficulties);

        difficultyBox.setFont(
                new Font("微软雅黑", Font.BOLD, 16)
        );

        difficultyBox.setSelectedIndex(1);

        difficultyBox.addActionListener(e -> {

            String level =
                    (String) difficultyBox.getSelectedItem();

            switch (level) {

                case "初级":
                    rows = 9;
                    cols = 9;
                    mineCount = 10;
                    break;

                case "中级":
                    rows = 16;
                    cols = 16;
                    mineCount = 40;
                    break;

                case "高级":
                    rows = 24;
                    cols = 24;
                    mineCount = 99;
                    break;
            }

            initIcons();

            initializeGame();
        });

        topPanel.add(difficultyBox);

        // 音乐切换
        String[] musicNames = {
                "还是分开",
                "雨爱",
                "带我走",
                "动物世界",
                "恋人"
        };

        JComboBox<String> musicBox =
                new JComboBox<>(musicNames);

        musicBox.setFont(
                new Font("微软雅黑", Font.BOLD, 16)
        );

        musicBox.addActionListener(
                e -> switchMusic(musicBox.getSelectedIndex())
        );

        topPanel.add(musicBox);

        // 暂停音乐
        JButton pauseButton = new JButton("暂停音乐");

        pauseButton.setFont(
                new Font("微软雅黑", Font.BOLD, 16)
        );

        pauseButton.addActionListener(e -> {

            if (mediaPlayer == null) return;

            if (mediaPlayer.getStatus()
                    == MediaPlayer.Status.PLAYING) {

                mediaPlayer.pause();

                pauseButton.setText("播放音乐");

            } else {

                mediaPlayer.play();

                pauseButton.setText("暂停音乐");
            }
        });

        topPanel.add(pauseButton);

        add(topPanel, BorderLayout.NORTH);

        boardPanel = new JPanel();

        add(boardPanel, BorderLayout.CENTER);

        // 默认音乐
        switchMusic(0);

        initializeGame();

        setVisible(true);
    }

    // 初始化图片
    private void initIcons() {

        int cellSize =
                Math.min(WINDOW_WIDTH, WINDOW_HEIGHT)
                        / Math.max(rows, cols);

        // 雷图片
        mineIcon = new ImageIcon(
                new ImageIcon(
                        "C:\\Users\\27987\\Downloads\\mine.png"
                ).getImage().getScaledInstance(
                        cellSize,
                        cellSize,
                        Image.SCALE_SMOOTH
                )
        );

        // 踩雷表情包
        loseIcon = new ImageIcon(
                new ImageIcon(
                        "C:\\Users\\27987\\Downloads\\emoji.png"
                ).getImage().getScaledInstance(
                        120,
                        120,
                        Image.SCALE_SMOOTH
                )
        );
    }

    // 音乐切换
    private void switchMusic(int index) {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        try {

            Media media = new Media(
                    new File(musicFiles[index])
                            .toURI()
                            .toString()
            );

            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setCycleCount(
                    MediaPlayer.INDEFINITE
            );

            mediaPlayer.play();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // 初始化游戏
    private void initializeGame() {

        gameOver = false;

        firstClick = true;

        boardPanel.removeAll();

        boardPanel.setLayout(
                new GridLayout(rows, cols)
        );

        cells = new Cell[rows][cols];

        buttons = new MineButton[rows][cols];

        int cellSize =
                Math.min(WINDOW_WIDTH, WINDOW_HEIGHT)
                        / Math.max(rows, cols);

        for (int row = 0; row < rows; row++) {

            for (int col = 0; col < cols; col++) {

                cells[row][col] = new Cell();

                MineButton btn =
                        new MineButton(row, col);

                btn.setPreferredSize(
                        new Dimension(cellSize, cellSize)
                );

                btn.setFont(
                        new Font(
                                "微软雅黑",
                                Font.BOLD,
                                Math.max(12, cellSize / 2)
                        )
                );

                btn.setMargin(
                        new Insets(0, 0, 0, 0)
                );

                btn.addMouseListener(
                        new MouseAdapter() {

                            @Override
                            public void mouseClicked(
                                    MouseEvent e
                            ) {

                                int r = btn.getRow();

                                int c = btn.getCol();

                                if (gameOver
                                        || cells[r][c].isRevealed) {

                                    return;
                                }

                                // 左键
                                if (SwingUtilities
                                        .isLeftMouseButton(e)) {

                                    if (firstClick) {

                                        generateMines(r, c);

                                        calculateAdjacentMines();

                                        firstClick = false;
                                    }

                                    if (cells[r][c].isFlagged)
                                        return;

                                    revealCell(r, c);

                                    if (checkWin()) {

                                        gameOver = true;

                                        JOptionPane.showMessageDialog(
                                                null,
                                                "恭喜你赢了！"
                                        );
                                    }
                                }

                                // 右键
                                else if (SwingUtilities
                                        .isRightMouseButton(e)) {

                                    toggleFlag(r, c);
                                }
                            }
                        });

                buttons[row][col] = btn;

                boardPanel.add(btn);
            }
        }

        boardPanel.revalidate();

        boardPanel.repaint();
    }

    // 生成雷
    private void generateMines(
            int safeRow,
            int safeCol
    ) {

        Random random = new Random();

        int placed = 0;

        while (placed < mineCount) {

            int r = random.nextInt(rows);

            int c = random.nextInt(cols);

            if ((r == safeRow && c == safeCol)
                    || cells[r][c].isMine) {

                continue;
            }

            cells[r][c].isMine = true;

            placed++;
        }
    }

    // 计算数字
    private void calculateAdjacentMines() {

        for (int row = 0; row < rows; row++) {

            for (int col = 0; col < cols; col++) {

                if (cells[row][col].isMine)
                    continue;

                int count = 0;

                for (int i = -1; i <= 1; i++) {

                    for (int j = -1; j <= 1; j++) {

                        int r = row + i;

                        int c = col + j;

                        if (r >= 0
                                && r < rows
                                && c >= 0
                                && c < cols
                                && cells[r][c].isMine) {

                            count++;
                        }
                    }
                }

                cells[row][col].adjacentMines = count;
            }
        }
    }

    // 翻开格子
    private void revealCell(int row, int col) {

        if (row < 0 || row >= rows
                || col < 0 || col >= cols)
            return;

        if (cells[row][col].isRevealed)
            return;

        cells[row][col].isRevealed = true;

        MineButton btn = buttons[row][col];

        // 踩雷
        if (cells[row][col].isMine) {

            btn.setIcon(mineIcon);

            btn.setBackground(Color.RED);

            revealAllMines();

            gameOver = true;

            JLabel label = new JLabel(
                    "你踩到雷啦！",
                    loseIcon,
                    JLabel.CENTER
            );

            label.setFont(
                    new Font("微软雅黑", Font.BOLD, 20)
            );

            label.setHorizontalTextPosition(
                    JLabel.CENTER
            );

            label.setVerticalTextPosition(
                    JLabel.BOTTOM
            );

            JOptionPane.showMessageDialog(
                    null,
                    label,
                    "游戏结束",
                    JOptionPane.PLAIN_MESSAGE
            );

            return;
        }

        // 数字
        if (cells[row][col].adjacentMines > 0) {

            btn.setText(
                    String.valueOf(
                            cells[row][col]
                                    .adjacentMines
                    )
            );

            btn.setForeground(
                    getNumberColor(
                            cells[row][col]
                                    .adjacentMines
                    )
            );
        }

        btn.setBackground(Color.LIGHT_GRAY);

        // 自动展开
        if (cells[row][col].adjacentMines == 0) {

            for (int i = -1; i <= 1; i++) {

                for (int j = -1; j <= 1; j++) {

                    if (i != 0 || j != 0) {

                        revealCell(
                                row + i,
                                col + j
                        );
                    }
                }
            }
        }
    }

    // 插旗
    private void toggleFlag(int row, int col) {

        if (cells[row][col].isRevealed)
            return;

        cells[row][col].isFlagged =
                !cells[row][col].isFlagged;

        MineButton btn = buttons[row][col];

        if (cells[row][col].isFlagged) {

            btn.setText("旗");

            btn.setForeground(Color.BLUE);

        } else {

            btn.setText("");
        }
    }

    // 显示所有雷
    private void revealAllMines() {

        for (int row = 0; row < rows; row++) {

            for (int col = 0; col < cols; col++) {

                if (cells[row][col].isMine) {

                    buttons[row][col]
                            .setIcon(mineIcon);

                    buttons[row][col]
                            .setBackground(Color.RED);
                }
            }
        }
    }

    // 胜利
    private boolean checkWin() {

        for (int row = 0; row < rows; row++) {

            for (int col = 0; col < cols; col++) {

                if (!cells[row][col].isMine
                        && !cells[row][col].isRevealed) {

                    return false;
                }
            }
        }

        return true;
    }


    // 数字颜色
    private Color getNumberColor(int num) {

        switch (num) {

            case 1:
                return new Color(0, 102, 255);

            case 2:
                return new Color(0, 153, 0);

            case 3:
                return Color.RED;

            case 4:
                return new Color(128, 0, 0);

            case 5:
                return new Color(139, 0, 0);

            case 6:
                return new Color(178, 34, 34);

            case 7:
                return new Color(255, 69, 0);

            case 8:
                return new Color(75, 0, 130);

            default:
                return Color.BLACK;
        }
    }

    public static void main(String[] args) {

        // 修复中文乱码
        UIManager.put(
                "OptionPane.messageFont",
                new Font("微软雅黑", Font.BOLD, 18)
        );

        UIManager.put(
                "OptionPane.buttonFont",
                new Font("微软雅黑", Font.BOLD, 16)
        );

        SwingUtilities.invokeLater(
                MinesweeperGame::new
        );
    }
}

// 格子
class Cell {

    boolean isMine = false;

    int adjacentMines = 0;

    boolean isRevealed = false;

    boolean isFlagged = false;
}

// 按钮
class MineButton extends JButton {

    private int row;

    private int col;

    public MineButton(int row, int col) {

        this.row = row;

        this.col = col;
    }

    public int getRow() {

        return row;
    }

    public int getCol() {

        return col;
    }
}