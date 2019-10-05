package game;

public class Player implements PlayerInterface {
    private AmazonInterface[] amazons;
    private BoardInterface board;

    public Player(PointInterface[] initialPointsOfAmazons, BoardInterface board) {
        this.board = board;
        setUpAmazons(initialPointsOfAmazons);        
    }

    private void setUpAmazons(PointInterface[] initialPointsOfAmazons) {
        int numberOfAmazons = initialPointsOfAmazons.length;
        this.amazons = new AmazonInterface[numberOfAmazons];
        for (int i = 0; i < numberOfAmazons; i++) {
            this.amazons[i] = new Amazon(initialPointsOfAmazons[i]);
        }
    }

    

    public void selectActiveAmazonAtPoint(PointInterface point){}
    public void deselectActiveAmazon(){}
    public PointInterface[] getPointsOfValidMoves(){return null;}
    public void moveActiveAmazonToPoint(PointInterface point) throws InvalidMoveException{}
    public PointInterface[] getPointsOfValidShots(){return null;}
    public void shootAtPoint(PointInterface point) throws InvalidMoveException{}
}
