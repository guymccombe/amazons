package controller;

import game.AmazonSelectionException;
import game.Board;
import game.BoardInterface;
import game.CellStatus;
import game.InvalidMoveException;
import game.PointInterface;
import game.PointOutOfBoundsException;
import view.CLI;
import view.GUI;
import view.ViewInterface;

public class Controller {
    private ViewInterface view;
    private BoardInterface model;

    public Controller(boolean useGUI) {
        this.view = useGUI ? new GUI(this) : new CLI(this);
        this.model = new Board();

        startGameLoop();
    }

    public Controller(ViewInterface view) {
        this.view = view;
        this.model = new Board();

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

    public void shootAtPoint(PointInterface point) throws PointOutOfBoundsException, InvalidMoveException {
        model.shootAtPoint(point);
    }

    public void newGame() {
        new Controller(view);
    }

    public static void main(String args[]) {
        new Controller(false);
    }
}
