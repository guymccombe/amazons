package view;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import controller.Controller;
import game.AmazonSelectionException;
import game.CellStatus;
import game.InvalidMoveException;
import game.Point;
import game.PointInterface;

public class AIEnvironment implements ViewInterface {
    private Controller controller;

    public void displayATurn(boolean isWhiteTurn) {
        generateNextState(isWhiteTurn);
        processNextMove();
    }

    private void generateNextState(boolean isWhiteTurn) {
        try {
            BufferedImage[] stateImages = generateStateImages(isWhiteTurn);
            writeStateToFile(stateImages);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private BufferedImage[] generateStateImages(boolean isWhiteTurn) throws IOException {
        BufferedImage[] images = new BufferedImage[3];
        for (int i = 0; i < images.length; i++) {
            images[i] = new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_BINARY);
        }

        CellStatus[][] cells = controller.getBoard();
        for (int x = 0; x < cells.length; x++) {
            for (int y = 0; y < cells[x].length; y++) {
                switch (cells[x][y]) {
                    case EMPTY:
                        break;
                    case BLACK_AMAZON:
                        if (isWhiteTurn) {
                            images[1].setRGB(x, y, Color.WHITE.getRGB());
                        } else {
                            images[0].setRGB(x, y, Color.WHITE.getRGB());
                        }
                        break;
                    case WHITE_AMAZON:
                        if (isWhiteTurn) {
                            images[0].setRGB(x, y, Color.WHITE.getRGB());
                        } else {
                            images[1].setRGB(x, y, Color.WHITE.getRGB());
                        }
                        break;
                    case ARROW:
                        images[2].setRGB(x, y, Color.WHITE.getRGB());
                        break;
                }
            }
        }
        return images;
    }

    private void writeStateToFile(BufferedImage[] images) throws IOException {
        String path = "src\\main\\interfaces\\state\\";
        for (int i = 0; i < images.length; i++) {
            File file = new File(path + i + ".png");
            ImageIO.write(images[i], "png", file);
        }
    }

    private void processNextMove() {
        PointInterface[] movePoints = getMovesFromFile();
        clearMoveFile();
        try {
            controller.selectAmazonAtPointAndGetMoves(movePoints[0]);
            controller.makeMoveToPointAndGetShots(movePoints[1]);
            controller.shootAtPoint(movePoints[2]);
        } catch (AmazonSelectionException | InvalidMoveException e) {
            System.out.println(e.toString());
            processNextMove();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private PointInterface[] getMovesFromFile() {
        String path = "src\\main\\interfaces\\move\\next.MOVE";
        File file = new File(path);
        PointInterface[] move = new PointInterface[3];
        while (file.length() == 0);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            for (int i = 0; i < move.length; i++) {
                String line = reader.readLine();
                String[] lineArr = line.split("\\s+");
                int x = Integer.parseInt(lineArr[0]);
                int y = Integer.parseInt(lineArr[1]);
                PointInterface point = new Point(x, y);
                move[i] = point;
            }
            return move;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void clearMoveFile() {
        String path = "src\\main\\interfaces\\move\\next.MOVE";
        try (PrintWriter pw = new PrintWriter(path)) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayWinner(boolean winnerIsWhite) {
        int reward = calculateReward(winnerIsWhite);
        BufferedImage rewardImage = generateRewardImage(reward, winnerIsWhite);
        try {
            writeStateToFile(new BufferedImage[] {rewardImage});
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private int calculateReward(boolean whiteWinner) {
        CellStatus winningAmazon = whiteWinner ? CellStatus.WHITE_AMAZON : CellStatus.BLACK_AMAZON;
        CellStatus[][] board = controller.getBoard();
        ArrayList<PointInterface> listOfAvailableCells = new ArrayList<>();
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                if (board[x][y] == winningAmazon) {
                    for (int xSearch = -1; xSearch < 2; xSearch++) {
                        for (int ySearch = -1; ySearch < 2; ySearch++) {
                            addEmptyNeighbours(board, xSearch, ySearch, listOfAvailableCells);
                        }
                    }
                }
            }
        }
        return listOfAvailableCells.size();
    }

    private void addEmptyNeighbours(CellStatus[][] board, int x, int y,
            ArrayList<PointInterface> listOfAvailableCells) {
        if (x < 0 || y < 0 || x >= board.length || y >= board.length) {
            return;
        }
        if (board[x][y] != CellStatus.EMPTY || listOfAvailableCells.contains(new Point(x, y))) {
            return;
        } else {
            listOfAvailableCells.add(new Point(x, y));
            for (int xSearch = -1; xSearch < 2; xSearch++) {
                for (int ySearch = -1; ySearch < 2; ySearch++) {
                    addEmptyNeighbours(board, xSearch, ySearch, listOfAvailableCells);
                }
            }
        }
    }

    private BufferedImage generateRewardImage(int reward, boolean winnerIsWhite) {
        if (reward == 0) {
            reward++;
        }
        BufferedImage img = new BufferedImage(1, reward, BufferedImage.TYPE_BYTE_BINARY);
        if (winnerIsWhite) {
            img.setRGB(0, 0, Color.WHITE.getRGB());
        }
        return img;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

}
