package view;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import controller.Controller;
import game.CellStatus;
import game.Point;
import game.PointInterface;

public class GUI implements ViewInterface {
    private final JFrame frame = new JFrame();
    private final JPanel outer = new JPanel(new BorderLayout());
    private final JLabel turnStatus = new JLabel("Setting up game...");

    private JPanel boardPanel;
    private JButton[][] boardCells;
    private Image[] pieceSprites = new Image[4];

    private Controller controller;
    private boolean waitingForShot;
    private volatile boolean isTurnOngoing;
    private String playerName;

    public GUI(Controller controller) {
        this.controller = controller;
        loadSprites();
        initialiseOuter();
       
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setIconImage(pieceSprites[2]);
        frame.setTitle("Amazons by Guy McCombe");
        frame.setSize(750,750);
        frame.add(outer);
        frame.setVisible(true);

    }

    private void initialiseOuter() {
        outer.setBorder(new EmptyBorder(8, 8, 8, 8));

        turnStatus.setHorizontalAlignment(JLabel.CENTER);
        outer.add(turnStatus, BorderLayout.PAGE_START);

        initialiseBoard();
        outer.add(boardPanel, BorderLayout.CENTER);
    }

    private void initialiseBoard() {
        CellStatus[][] board = controller.getBoard();
        boardCells = new JButton[board.length][board[0].length];
        boardPanel = new JPanel(new GridLayout(boardCells.length + 1, boardCells[0].length + 1));

        boardPanel.add(new JLabel(""));
        for (int j = 0; j < boardCells[0].length; j++) {
            JLabel label = new JLabel(j+"");
            label.setVerticalAlignment(JLabel.CENTER);
            label.setHorizontalAlignment(JLabel.CENTER);
            boardPanel.add(label);
        }

        for (int i = 0; i < boardCells.length; i++) {
            JLabel label = new JLabel(i+"");
            label.setVerticalAlignment(JLabel.CENTER);
            label.setHorizontalAlignment(JLabel.CENTER);
            boardPanel.add(label);

            for (int j = 0; j < boardCells[0].length; j++) {
                JButton button = new JButton();
                button.setIcon(new ImageIcon(
                    new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)
                ));
                button.setBackground(Color.WHITE);
                button.setActionCommand(j + "," + i);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        String actionCommand = evt.getActionCommand();
                        int[] splitToInts = Arrays.stream(actionCommand.split(","))
                                .mapToInt(Integer::parseInt)
                                .toArray();
                        buttonPressedAtPoint(new Point(splitToInts[0], splitToInts[1]));
                    }
                });
                boardCells[i][j] = button;
                boardPanel.add(button);
            }
        }
    }

    private void buttonPressedAtPoint(PointInterface pointOfPressed) {
        if (waitingForShot) {
            fireShotAtPoint(pointOfPressed);
        } else {
            selectOrMoveTo(pointOfPressed);
        }
    }

    private void fireShotAtPoint(PointInterface point) {
        try {
            controller.shootAtPoint(point);
            updateBoard(new PointInterface[]{});
            waitingForShot = false;
            isTurnOngoing = false;
        } catch (Exception e) {
            System.out.printf("Click at point %s ignored. It is %s's turn and waiting for shot is %b.%n", point.toString(), playerName, waitingForShot);
        }
    }

    private void selectOrMoveTo(PointInterface point) {
        try {
            PointInterface[] targets = controller.makeMoveToPointAndGetShots(point);
            waitingForShot = true;
            turnStatus.setText(playerName + ", shoot an arrow.");
            updateBoard(targets);            
        } catch (Exception e) {
            try {
                PointInterface[] targets = controller.selectAmazonAtPointAndGetMoves(point);
                updateBoard(targets);
            } catch (Exception e2) {
                System.out.printf("Click at point %s ignored. It is %s's turn and waiting for shot is %b.%n", point.toString(), playerName, waitingForShot);
            }
        }
    }

    private void loadSprites() {
        try {
            URL url = getClass().getResource("sprites.png");
            BufferedImage image = ImageIO.read(new File(url.getPath()));
            for (int i = 0; i < pieceSprites.length; i++) {
                pieceSprites[i] = image.getSubimage(0, i*64, 64, 64);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void displayATurn(boolean isWhiteTurn) {
        isTurnOngoing = true;
        playerName = isWhiteTurn ? "WHITE" : "BLACK";
        turnStatus.setText(playerName + ", it is your turn to move!");
        updateBoard(new PointInterface[]{});
        while(isTurnOngoing);
        System.out.printf("%s's turn is done.", playerName);
    }

    private void updateBoard(PointInterface[] targets) {
        CellStatus[][] board = controller.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (Arrays.stream(targets).anyMatch(new Point(j, i)::equals)) {
                    boardCells[i][j].setIcon(new ImageIcon(pieceSprites[3]));
                } else {
                    switch (board[i][j]) {
                        case BLACK_AMAZON:
                            boardCells[i][j].setIcon(new ImageIcon(pieceSprites[0]));
                            break;
                        case WHITE_AMAZON:
                            boardCells[i][j].setIcon(new ImageIcon(pieceSprites[1]));
                            break;
                        case ARROW:
                            boardCells[i][j].setIcon(new ImageIcon(pieceSprites[2]));
                            break;
                        case EMPTY:
                            boardCells[i][j].setIcon(new ImageIcon(
                                new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)
                            ));
                            break;
                    }
                }
            }
        }
    }

    public void displayWinner(boolean winnerIsWhite) {
        int dialogButton = JOptionPane.YES_NO_OPTION;
        String winner = winnerIsWhite ? "White" : "Black";
        String message = "Congratulations! " + winner + ", you win!\nPlay again?";
        String title = winner + " wins!";
        int dialogResult = JOptionPane.showConfirmDialog(frame, message, title, dialogButton);
        if(dialogResult == 0) {
            frame.setVisible(false);
            frame.dispose();
            controller.newGame();
        } else {
            System.exit(0);
        } 
    }
}