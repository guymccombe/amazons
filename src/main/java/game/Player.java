package game;

public class Player implements PlayerInterface {
    private AmazonInterface[] amazons;
    private BoardInterface board;

    public Player(PointInterface[] initialPointsOfAmazons, BoardInterface board) {
        int numberOfAmazons = initialPointsOfAmazons.length;
        this.amazons = new AmazonInterface[numberOfAmazons];
        for (int i = 0; i < numberOfAmazons; i++) {
            this.amazons[i] = new Amazon(initialPointsOfAmazons[i]);
        }
        
        this.board = board;
    }

    public void movePieceTo(PointInterface start, PointInterface end) throws Exception {
        //board.move();
    }
}
