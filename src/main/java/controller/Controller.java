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

    public Controller(ViewInterface view) {
        this.model = new Board();
        this.view = view;
        view.setController(this);

        startGameLoop();
    }

    private void startGameLoop() {
        boolean isWhitesTurn = true;
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
