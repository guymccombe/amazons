package game;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PlayerTest {

    private BoardInterface board = new Board();

    @Test(expected = OverlappingAmazonsException.class)
    public void placingOverlappingAmazonsThrowsException() {
        PointInterface[] pointsOfAmazons = new Point[]{new Point(1,1)};
        PlayerInterface playerOne = new Player(pointsOfAmazons, board);
        PlayerInterface playerTwo = new Player(pointsOfAmazons, board);
    }

    @Test
    public void selectingAnIncorrectLocationThrowsException() {
        PointInterface[] pointsOfAmazons = new Point[]{new Point(1,1)};
        PlayerInterface player = new Player(pointsOfAmazons, board);
    }
}