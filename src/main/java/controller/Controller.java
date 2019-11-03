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
        this.model = new Board();
        this.view = useGUI ? new GUI(this) : new CLI(this);

        startGameLoop();
    }

    private void startGameLoop() {
        boolean isWhitesTurn = true;
        while (model.isWhiteTheWinner() == null) {
            view.displayATurn(isWhitesTurn);
            isWhitesTurn = !isWhitesTurn;
            System.out.println(model.isWhiteTheWinner());
        }
        view.displayWinner(true);
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
        new Controller(true);
    }

    public static void main(String args[]) {
        //TODO use args to change between display modes
        new Controller(true);
    }
}
