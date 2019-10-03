package game;

public interface BoardInterface {
    public CellStatus getCellStatusAtPoint(PointInterface point) throws PointOutOfBoundsException;
}