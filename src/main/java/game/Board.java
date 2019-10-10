package game;

import java.util.ArrayList;

public class Board implements BoardInterface {
    private CellInterface[][] cells;
    private PointInterface[] currentTargets = null;
    private PointInterface pointOfSelected = null;
    
    public Board() {
        cells = new CellInterface[10][10];
        initialiseCells();
    }

    public Board(int dimension) {
        cells = new CellInterface[dimension][dimension];
        initialiseCells();
    }

    public Board(int xLength, int yLength) {
        cells = new CellInterface[yLength][xLength];
        initialiseCells();
    }

    private void initialiseCells() {
        for (int i = 0; i < cells.length; i++) {
            CellInterface[] row = cells[i];
            for (int j = 0; j < row.length; j++) {
                row[j] = new Cell();
            }
        }
    }

    public CellStatus getCellStatusAtPoint(PointInterface point) throws PointOutOfBoundsException {
        if (isPointWithinBoard(point)) {
            CellInterface cell = cells[point.getY()][point.getX()];
            CellStatus status = cell.getStatus();
            return status;
        } else {
            throw new PointOutOfBoundsException(point);
        }
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

    private void setCellStatusAtPoint(PointInterface point, CellStatus status) throws PointOutOfBoundsException {
        if (isPointWithinBoard(point)) {
            cells[point.getY()][point.getX()].setStatus(status);
        } else {
            throw new PointOutOfBoundsException(point);
        }
    }

    public void placeAmazonsAtPoints(PointInterface[] points) throws OverlappingAmazonsException, PointOutOfBoundsException{
        for (int i = 0; i < points.length; i++) {
            PointInterface point = points[i];
            try {
                if (getCellStatusAtPoint(point) == CellStatus.EMPTY) {
                    setCellStatusAtPoint(point, CellStatus.AMAZON);
                } else {
                    undoPlacementOfAmazons(points, i);
                    throw new OverlappingAmazonsException("Placement of amazon at point: " + point.toString() + " failed as the Cell is not empty.");
                }
            } catch (PointOutOfBoundsException e) {
                undoPlacementOfAmazons(points, i);
                throw e;
            }
        }
    }

    private void undoPlacementOfAmazons(PointInterface[] points, int indexOfFailedPlacement) throws PointOutOfBoundsException{
        for (int i = 0; i < indexOfFailedPlacement; i++) {
            PointInterface point = points[i];
            setCellStatusAtPoint(point, CellStatus.EMPTY);
        }
    }
    
    public void selectAmazonAtPoint(PointInterface point) throws PointOutOfBoundsException, AmazonSelectionException {
        if (pointOfSelected == null) {
            if (getCellStatusAtPoint(point) == CellStatus.AMAZON) {
                pointOfSelected = point;
            } else {
                throw new AmazonSelectionException("Targeted point is not an Amazon.");
            }
        } else {
            throw new AmazonSelectionException("An Amazon is already selected.");
        }

    }

    public void deselectAmazon() throws AmazonSelectionException {
        if (pointOfSelected != null) {
            pointOfSelected = null;
        } else {
            throw new AmazonSelectionException("No Amazon is selected.");
        }

    }
    
    public PointInterface[] getValidTargetsAroundSelectedAmazon() throws AmazonSelectionException, PointOutOfBoundsException {
        if (pointOfSelected != null) {
            ArrayList<PointInterface> outList = new ArrayList<>();
            outList.addAll(pollInXYDirection( 0,-1));
            outList.addAll(pollInXYDirection( 1,-1));
            outList.addAll(pollInXYDirection( 1, 0));
            outList.addAll(pollInXYDirection( 1, 1));
            outList.addAll(pollInXYDirection( 0, 1));
            outList.addAll(pollInXYDirection(-1, 1));
            outList.addAll(pollInXYDirection(-1, 0));
            outList.addAll(pollInXYDirection(-1,-1));
    
            int size = outList.size();
            currentTargets = new PointInterface[size];
            for (int i = 0; i < size; i++) {
                currentTargets[i] = outList.get(i);
            }
            return currentTargets;
        } else {
            throw new AmazonSelectionException("No Amazon is selected.");
        }
    }

    private ArrayList<PointInterface> pollInXYDirection(int xIncrement, int yIncrement) throws PointOutOfBoundsException {
        int x = pointOfSelected.getX() + xIncrement, y = pointOfSelected.getY() + yIncrement;
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

    public void moveSelectedAmazonToPoint(PointInterface point)
            throws AmazonSelectionException, PointOutOfBoundsException, InvalidMoveException {
        // TODO Auto-generated method stub

    }

    public void shootAtPoint(PointInterface point) throws PointOutOfBoundsException, InvalidMoveException {
        // TODO Auto-generated method stub

    }
}
