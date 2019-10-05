package game;

public interface BoardInterface {
    public CellStatus getCellStatusAtPoint(PointInterface point) throws PointOutOfBoundsException;
    public void placeAmazonsAtPoints(PointInterface[] points) throws OverlappingAmazonsException, PointOutOfBoundsException;
    public PointInterface[] getArrayOfValidTargetsFromPoint(PointInterface point) throws PointOutOfBoundsException, InvalidMoveException;
}