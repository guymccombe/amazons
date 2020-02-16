package controller;

import java.util.Arrays;

import game.AmazonSelectionException;
import game.Board;
import game.BoardInterface;
import game.CellStatus;
import game.InvalidMoveException;
import game.PointInterface;
import game.PointOutOfBoundsException;
import view.AIEnvironment;
import view.CLI;
import view.GUI;
import view.ViewInterface;

public class Controller {
    private ViewInterface view;
    private BoardInterface model;
    private boolean isWhitesTurn = true;
    private BoardInterface checkpoint;
    private boolean wasWhitesTurn;

    public Controller(ViewInterface view) {
        this.model = new Board();
        this.view = view;
        view.setController(this);

        System.out.println("Starting Java gameloop...");

        startGameLoop();
    }

    private void startGameLoop() {
        while (model.isWhiteTheWinner() == null) {
            view.displayATurn(isWhitesTurn);
            isWhitesTurn = !isWhitesTurn;
        }
        view.displayWinner(model.isWhiteTheWinner());
    }

    public CellStatus[][] getBoard() {
        return model.getBoardStatuses();
    }

    public PointInterface[] selectAmazonAtPointAndGetMoves(PointInterface point)
            throws PointOutOfBoundsException, AmazonSelectionException {
        return model.selectAmazonAtPointAndReturnMoveTargets(point);
    }

    public PointInterface[] makeMoveToPointAndGetShots(PointInterface point)
            throws AmazonSelectionException, PointOutOfBoundsException, InvalidMoveException {
        return model.moveSelectedAmazonToPointAndReturnShootTargets(point);
    }

    public void shootAtPoint(PointInterface point)
            throws PointOutOfBoundsException, InvalidMoveException {
        model.shootAtPoint(point);
    }

    public void newGame(ViewInterface view) {
        new Controller(view);
    }

    public void saveCheckpoint() {
        checkpoint = (BoardInterface) model.clone();
        wasWhitesTurn = isWhitesTurn;
    }

    public void rollback() {
        model = checkpoint;
        isWhitesTurn = wasWhitesTurn;
    }

    public static void main(String args[]) {
        if (args.length > 0) {
            if (Arrays.stream(args).anyMatch("-env"::equals)) {
                new Controller(new AIEnvironment());
            } else if (Arrays.stream(args).anyMatch("-GUI"::equals)) {
                new Controller(new GUI());
            }
        } else {
            new Controller(new CLI());
        }
    }
}
