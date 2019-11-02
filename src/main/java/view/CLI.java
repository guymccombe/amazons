package view;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

import controller.Controller;
import game.CellStatus;
import game.Point;
import game.PointInterface;

public class CLI implements ViewInterface {
    private Controller controller;
    private Scanner input = new Scanner(System.in);

    public CLI(Controller controller) {
        this.controller = controller;
    }

    public void displayATurn(boolean isWhiteTurn) {
        String playerName = isWhiteTurn ? "WHITE" : "BLACK";
        System.out.printf("%s: It is your turn to move!%n", playerName);
        displayBoard(new PointInterface[]{});
        PointInterface[] eligibleMoves = selectAmazonAndReturnEligibleMoves();
        PointInterface[] eligibleShots = moveAmazonOrChangeSelectionAndReturnEligible(eligibleMoves);
        getShotAndFire(eligibleShots);
    }

    private void displayBoard(PointInterface[] eligiblePoints) {
        CellStatus[][] board = controller.getBoard();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            builder.append("  " + i);
        }
        System.out.println(builder.toString());

        for (int i = 0; i < board.length; i++) {
            System.out.print(i);
            for (int j = 0; j < board[i].length; j++) {
                String symbol = "";
                if (Arrays.stream(eligiblePoints).anyMatch(new Point(j, i)::equals)) {
                    symbol = " ○ ";
                } else {
                    switch(board[i][j]) {
                        case BLACK_AMAZON:
                            symbol = " B ";
                            break;
                        case WHITE_AMAZON:
                            symbol = " W ";
                            break;
                        case ARROW:
                            symbol = " X ";
                            break;
                        case EMPTY:
                            symbol = " - ";
                            break;
                    }
                }
                System.out.print(symbol);
            }
            System.out.println("");               
        }
    }

    private PointInterface[] selectAmazonAndReturnEligibleMoves() {
        System.out.println("Enter the coordinate of one of your amazons:");
        PointInterface selectionPoint = readPointFromConsole();
        try {
            PointInterface[] eligiblePoints = controller.selectAmazonAtPointAndGetMoves(selectionPoint);
            return eligiblePoints;
        } catch (Exception e) {
            printException(e);
            return selectAmazonAndReturnEligibleMoves();
        }
    }

    private PointInterface readPointFromConsole() {
        try {
            System.out.print("X > ");
            int x = input.nextInt();
            System.out.print("Y > ");
            int y = input.nextInt();
            return new Point(x, y);
        } catch (InputMismatchException e) {
            System.out.println("That wasn't an int, please try again.");
            input.next();
            return readPointFromConsole();
        }
    }

    private void printException(Exception e) {
        System.out.println(e.getMessage());
    }

    private PointInterface[] moveAmazonOrChangeSelectionAndReturnEligible(PointInterface[] currentlyEligible) {
        displayBoard(currentlyEligible);
        System.out.println("Make a move or select a different amazon:");

        PointInterface point = readPointFromConsole();
        if (Arrays.stream(currentlyEligible).anyMatch(point::equals)) {
            try {
                PointInterface[] eligibleShots = controller.makeMoveToPointAndGetShots(point);
                return eligibleShots;
            } catch (Exception e) {
                printException(e);
                return moveAmazonOrChangeSelectionAndReturnEligible(currentlyEligible);
            }

        } else {
            try {
                PointInterface[] eligibleMoves = controller.selectAmazonAtPointAndGetMoves(point);
                return moveAmazonOrChangeSelectionAndReturnEligible(eligibleMoves);
            } catch (Exception e) {
                printException(e);
                return moveAmazonOrChangeSelectionAndReturnEligible(currentlyEligible);
            }
        }
    }

    private void getShotAndFire(PointInterface[] eligibleShots) {
        displayBoard(eligibleShots);
        System.out.println("Fire an arrow!");
        PointInterface point = readPointFromConsole();
        try {
            controller.shootAtPoint(point);
            System.out.printf("%n%n%n");
        } catch (Exception e) {
            printException(e);
            getShotAndFire(eligibleShots);
        }
    }

    public void displayWinner(boolean winnerIsWhite) {
        displayBoard(new PointInterface[]{});
        String winnerName = winnerIsWhite ? "WHITE" : "BLACK";
        System.out.printf("Congratulions, %s, you are the winner!%n Press enter to restart.", winnerName);
        input.next();
        controller.newGame();
    }
}