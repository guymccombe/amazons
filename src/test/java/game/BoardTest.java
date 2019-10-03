package game;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BoardTest {

    /*
    @Test
    public void defaultBoardSizeIsSix() {
        BoardInterface board = new Board();
        assertTrue(board.getBoardDimension() == 6);
    }

    @Test
    public void setBoardSizeByConstructor() {
        BoardInterface board = new Board(10);
        assertTrue(board.getBoardDimension() == 10);
    } */

    BoardInterface board = new Board(10);

    @Test(expected = PointOutOfBoundsException.class)
    public void getStatusWithNegativePointThrowsException() throws PointOutOfBoundsException {
        PointInterface point = new Point(-1, -1);
        board.getCellStatusAtPoint(point);
    }

    @Test(expected = PointOutOfBoundsException.class)
    public void getStatusPointLargerThanBoundsThrowsException() throws PointOutOfBoundsException {
        PointInterface point = new Point(10, 10);
        board.getCellStatusAtPoint(point);
    }

    @Test
    public void getStatusInBounds() throws PointOutOfBoundsException {
        PointInterface point = new Point(0, 0);
        CellStatus status = board.getCellStatusAtPoint(point);
        assertTrue(status != null);
    }

}