package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Board implements BoardInterface, Cloneable {
    private CellInterface[][] cells;
    private PointInterface[] currentTargets;
    private PointInterface pointOfSelected;
    private MoveStatus nextMove = MoveStatus.WHITE_MOVE;
    private ArrayList<PointInterface> pointsOfWhiteAmazons = new ArrayList<>();
    private ArrayList<PointInterface> pointsOfBlackAmazons = new ArrayList<>();
    private boolean isWhiteTheWinner;

    public Board() {
        cells = new CellInterface[10][10];

        Placement[] defaultPlacement = new Placement[] {new Placement(new Point(3, 0), true),
                new Placement(new Point(6, 0), true), new Placement(new Point(0, 3), true),
                new Placement(new Point(9, 3), true), new Placement(new Point(3, 9), false),
                new Placement(new Point(6, 9), false), new Placement(new Point(0, 6), false),
                new Placement(new Point(9, 6), false)};

        initialiseCells(defaultPlacement);
    }

    public Board(int dimension, Placement[] amazonPlacements) {
        cells = new CellInterface[dimension][dimension];
        initialiseCells(amazonPlacements);
    }

    public Board(int xLength, int yLength, Placement[] amazonPlacements) {
        cells = new CellInterface[yLength][xLength];
        initialiseCells(amazonPlacements);
    }

    private void initialiseCells(Placement[] placements) {
        for (int i = 0; i < cells.length; i++) {
            CellInterface[] row = cells[i];
            for (int j = 0; j < row.length; j++) {
                row[j] = new Cell();
            }
        }

        try {
            for (Placement placement : placements) {
                if (placement.isWhite()) {
                    setCellStatusAtPoint(placement.getPoint(), CellStatus.WHITE_AMAZON);
                    pointsOfWhiteAmazons.add(placement.getPoint());
                } else {
                    setCellStatusAtPoint(placement.getPoint(), CellStatus.BLACK_AMAZON);
                    pointsOfBlackAmazons.add(placement.getPoint());
                }
            }
        } catch (PointOutOfBoundsException oobException) {
            System.err.println(oobException.getMessage());
        }
    }

    private CellStatus getCellStatusAtPoint(PointInterface point) throws PointOutOfBoundsException {
        if (isPointWithinBoard(point)) {
            CellInterface cell = cells[point.getY()][point.getX()];
            CellStatus status = cell.getStatus();
            return status;
        } else {
            throw new PointOutOfBoundsException(point);
        }
    }

    public CellStatus[][] getBoardStatuses() {
        CellStatus[][] output = new CellStatus[cells.length][cells[0].length];
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                output[i][j] = cells[i][j].getStatus();
            }
        }
        return output;
    }

    private boolean isPointWithinBoard(PointInterface point) {
        boolean isPointWithinBoard = true;
        isPointWithinBoard &= point.getX() > -1;
        isPointWithinBoard &= point.getX() < getMaximumX();
        isPointWithinBoard &= point.getY() > -1;
        isPointWithinBoard &= point.getY() < getMaximumY();
        return isPointWithinBoard;
    }

    private int getMaximumX() {
        int maximum = cells[0].length;
        return maximum;
    }

    private int getMaximumY() {
        int maximum = cells.length;
        return maximum;
    }

    private void setCellStatusAtPoint(PointInterface point, CellStatus status)
            throws PointOutOfBoundsException {
        if (isPointWithinBoard(point)) {
            cells[point.getY()][point.getX()].setStatus(status);
        } else {
            throw new PointOutOfBoundsException(point);
        }
    }

    public PointInterface[] selectAmazonAtPointAndReturnMoveTargets(PointInterface point)
            throws PointOutOfBoundsException, AmazonSelectionException {
        if (nextMove == MoveStatus.WHITE_MOVE || nextMove == MoveStatus.BLACK_MOVE) {
            if ((nextMove == MoveStatus.WHITE_MOVE
                    && getCellStatusAtPoint(point) == CellStatus.WHITE_AMAZON)
                    || (nextMove == MoveStatus.BLACK_MOVE
                            && getCellStatusAtPoint(point) == CellStatus.BLACK_AMAZON)) {
                pointOfSelected = point;
                currentTargets = getValidTargetsAroundSelectedAmazon();
                return currentTargets;
            } else {
                throw new AmazonSelectionException("Targeted point " + point.toString()
                        + " is not an Amazon or is the wrong colour. It is: "
                        + getCellStatusAtPoint(point));
            }
        } else {
            throw new AmazonSelectionException(
                    "You cannot select an Amazon during the shooting stage of a move.");
        }

    }

    private PointInterface[] getValidTargetsAroundSelectedAmazon()
            throws AmazonSelectionException, PointOutOfBoundsException {
        if (pointOfSelected != null) {
            return getValidTargetsAroundPoint(pointOfSelected);
        } else {
            throw new AmazonSelectionException("No Amazon is selected.");
        }
    }

    private PointInterface[] getValidTargetsAroundPoint(PointInterface point)
            throws PointOutOfBoundsException {
        ArrayList<PointInterface> outList = new ArrayList<>();
        outList.addAll(pollInXYDirection(0, -1, point));
        outList.addAll(pollInXYDirection(1, -1, point));
        outList.addAll(pollInXYDirection(1, 0, point));
        outList.addAll(pollInXYDirection(1, 1, point));
        outList.addAll(pollInXYDirection(0, 1, point));
        outList.addAll(pollInXYDirection(-1, 1, point));
        outList.addAll(pollInXYDirection(-1, 0, point));
        outList.addAll(pollInXYDirection(-1, -1, point));

        int size = outList.size();
        PointInterface[] targets = new PointInterface[size];
        for (int i = 0; i < size; i++) {
            targets[i] = outList.get(i);
        }
        return targets;
    }

    private ArrayList<PointInterface> pollInXYDirection(int xIncrement, int yIncrement,
            PointInterface point) throws PointOutOfBoundsException {
        int x = point.getX() + xIncrement, y = point.getY() + yIncrement;
        ArrayList<PointInterface> outList = new ArrayList<>();

        while (x > -1 && x < getMaximumX() && y > -1 && y < getMaximumY()) {
            PointInterface currentPointBeingPolled = new Point(x, y);
            if (getCellStatusAtPoint(currentPointBeingPolled) == CellStatus.EMPTY) {
                outList.add(currentPointBeingPolled);
                x += xIncrement;
                y += yIncrement;
            } else {
                return outList;
            }
        }
        return outList;
    }

    public PointInterface[] moveSelectedAmazonToPointAndReturnShootTargets(PointInterface point)
            throws AmazonSelectionException, PointOutOfBoundsException, InvalidMoveException {
        if (pointOfSelected != null) {
            if ((getCellStatusAtPoint(pointOfSelected) == CellStatus.WHITE_AMAZON
                    && nextMove == MoveStatus.WHITE_MOVE)
                    || (getCellStatusAtPoint(pointOfSelected) == CellStatus.BLACK_AMAZON
                            && nextMove == MoveStatus.BLACK_MOVE)) {
                HashSet<PointInterface> targetsAsSet = new HashSet<>(Arrays.asList(currentTargets));
                if (targetsAsSet.contains(point)) {
                    setCellStatusAtPoint(point, getCellStatusAtPoint(pointOfSelected));
                    setCellStatusAtPoint(pointOfSelected, CellStatus.EMPTY);
                    if (nextMove == MoveStatus.WHITE_MOVE) {
                        pointsOfWhiteAmazons.remove(pointOfSelected);
                        pointsOfWhiteAmazons.add(point);
                        nextMove = MoveStatus.WHITE_SHOOT;
                    } else {
                        pointsOfBlackAmazons.remove(pointOfSelected);
                        pointsOfBlackAmazons.add(point);
                        nextMove = MoveStatus.BLACK_SHOOT;
                    }
                    pointOfSelected = point;
                    currentTargets = getValidTargetsAroundSelectedAmazon();
                    return currentTargets;
                } else {
                    throw new InvalidMoveException(
                            "The point: " + point.toString() + " is not a valid target.");
                }
            } else {
                throw new InvalidMoveException("It is not your turn to move an Amazon.");
            }
        } else {
            throw new AmazonSelectionException("You cannot move without selecting an Amazon.");
        }
    }

    public void shootAtPoint(PointInterface point)
            throws PointOutOfBoundsException, InvalidMoveException {
        if ((getCellStatusAtPoint(pointOfSelected) == CellStatus.WHITE_AMAZON
                && nextMove == MoveStatus.WHITE_SHOOT)
                || (getCellStatusAtPoint(pointOfSelected) == CellStatus.BLACK_AMAZON
                        && nextMove == MoveStatus.BLACK_SHOOT)) {
            HashSet<PointInterface> targetsAsSet = new HashSet<>(Arrays.asList(currentTargets));
            if (targetsAsSet.contains(point)) {
                setCellStatusAtPoint(point, CellStatus.ARROW);
                pointOfSelected = null;
                currentTargets = null;
                nextMove = (nextMove == MoveStatus.WHITE_SHOOT) ? MoveStatus.BLACK_MOVE
                        : MoveStatus.WHITE_MOVE;
            } else {
                throw new InvalidMoveException(
                        "The point: " + point.toString() + " is not a valid target.");
            }
        } else {
            throw new InvalidMoveException("It is not your turn to shoot.");
        }

    }

    private boolean isGameFinished() {
        try {
            if (nextMove == MoveStatus.BLACK_SHOOT || nextMove == MoveStatus.WHITE_SHOOT) {
                return false;
            }
            if (nextMove == MoveStatus.WHITE_MOVE) {
                boolean gameOver = true;
                for (PointInterface pointOfAmazon : pointsOfWhiteAmazons) {
                    gameOver &= (getValidTargetsAroundPoint(pointOfAmazon).length == 0);
                }
                if (gameOver) {
                    isWhiteTheWinner = false;
                }
                return gameOver;
            } else {
                boolean gameOver = true;
                for (PointInterface pointOfAmazon : pointsOfBlackAmazons) {
                    gameOver &= (getValidTargetsAroundPoint(pointOfAmazon).length == 0);
                }
                if (gameOver) {
                    isWhiteTheWinner = true;
                }
                return gameOver;
            }
        } catch (PointOutOfBoundsException pointOutOfBoundsException) {
            System.err.println(pointOutOfBoundsException.getMessage()
                    + "\n This should never occur! Check the storage of amazons.");
            return true;
        }
    }

    public Boolean isWhiteTheWinner() {
        if (isGameFinished()) {
            return isWhiteTheWinner;
        } else {
            return null;
        }
    }

    @Override
    public Object clone() {
        try {
            return (BoardInterface) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }
}
