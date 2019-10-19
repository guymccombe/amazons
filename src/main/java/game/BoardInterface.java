package game;

public interface BoardInterface {
    public PointInterface[] selectAmazonAtPointAndReturnMoveTargets(PointInterface point) throws PointOutOfBoundsException, AmazonSelectionException;
    public PointInterface[] moveSelectedAmazonToPointAndReturnShootTargets(PointInterface point) throws AmazonSelectionException, PointOutOfBoundsException, InvalidMoveException;
    public void shootAtPoint(PointInterface point) throws PointOutOfBoundsException, InvalidMoveException;
    public boolean isGameFinished();
    public boolean isWhiteTheWinner() throws GameInProgressException;
}
