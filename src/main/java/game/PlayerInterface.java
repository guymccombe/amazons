package game;

public interface PlayerInterface {
    public void selectActiveAmazonAtPoint(PointInterface point);
    public void deselectActiveAmazon();
    public PointInterface[] getPointsOfValidMoves();
    public void moveActiveAmazonToPoint(PointInterface point) throws AmazonSelectionException;
    public PointInterface[] getPointsOfValidShots();
    public void shootAtPoint(PointInterface point) throws AmazonSelectionException;
}
