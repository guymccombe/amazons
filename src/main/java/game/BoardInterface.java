package game;

public interface BoardInterface {
    public CellStatus getCellStatusAtPoint(PointInterface point) throws PointOutOfBoundsException;
    public void placeAmazonsAtPoints(PointInterface[] points) throws OverlappingAmazonsException, PointOutOfBoundsException;
    public void selectAmazonAtPoint(PointInterface point) throws PointOutOfBoundsException, AmazonSelectionException;
    public void deselectAmazon() throws AmazonSelectionException;
    public PointInterface[] getValidTargetsAroundSelectedAmazon() throws AmazonSelectionException, PointOutOfBoundsException;
    public void moveSelectedAmazonToPoint(PointInterface point) throws AmazonSelectionException, PointOutOfBoundsException, InvalidMoveException;
    public void shootAtPoint(PointInterface point) throws PointOutOfBoundsException, InvalidMoveException;
}