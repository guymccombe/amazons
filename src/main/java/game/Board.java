package game;

public class Board implements BoardInterface {
    private CellInterface[][] cells;
    
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
            throw new PointOutOfBoundsException("Point not in board.");
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
}