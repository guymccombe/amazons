package view;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import controller.Controller;
import game.CellStatus;
import game.Point;
import game.PointInterface;


public class AIEnvironment implements ViewInterface {
    private Controller controller;

    public void displayATurn(boolean isWhiteTurn) {
        BufferedImage[] stateImages = generateStateImages(isWhiteTurn);
        try {
            writeStateToFile(stateImages);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private BufferedImage[] generateStateImages(boolean isWhiteTurn) {
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
        String path = "src\\main\\state\\";
        for (int i = 0; i < images.length; i++) {
            File file = new File(path + i + ".gif");
            ImageIO.write(images[i], "gif", file);
            System.out.printf("Wrote image %d to path: %s", i, file.getAbsolutePath());
        }
    }

    public void displayWinner(boolean winnerIsWhite) {
        int reward = calculateReward(winnerIsWhite);
        System.out.println(reward);
    }

    int calculateReward(boolean whiteWinner) {
        CellStatus winningAmazon = whiteWinner ? CellStatus.WHITE_AMAZON : CellStatus.BLACK_AMAZON;
        CellStatus[][] board = controller.getBoard();
        ArrayList<PointInterface> listOfAvailableCells = new ArrayList<>();
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                if (board[x][y] == winningAmazon) {
                    for (int xSearch = -1; xSearch < 2; xSearch ++) {
                        for (int ySearch = -1; ySearch < 2; ySearch++) {
                            addEmptyNeighbours(board, xSearch, ySearch, listOfAvailableCells);
                        }
                    } 
                }
            }
        }
        return listOfAvailableCells.size();
    }

    private void addEmptyNeighbours(
        CellStatus[][] board, int x, int y, ArrayList<PointInterface> listOfAvailableCells) {
        if (board[x][y] != CellStatus.EMPTY || listOfAvailableCells.contains(new Point(x,y))) {
            return;
        } else {
            listOfAvailableCells.add(new Point(x,y));
            for (int xSearch = -1; xSearch < 2; xSearch ++) {
                for (int ySearch = -1; ySearch < 2; ySearch ++) {
                    addEmptyNeighbours(board, xSearch, ySearch, listOfAvailableCells);
                }
            }
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
    
}