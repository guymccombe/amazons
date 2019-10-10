package game;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class BoardTest {

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

    @Test(expected = OverlappingAmazonsException.class)
    public void placingOverlappingAmazonsThrowsException() throws OverlappingAmazonsException, PointOutOfBoundsException {
        PointInterface point = new Point(0,0);
        board.placeAmazonsAtPoints(new PointInterface[]{point, point});
    }

    @Test(expected = PointOutOfBoundsException.class)
    public void placingAmazonOutOfBoundsThrowsException() throws OverlappingAmazonsException, PointOutOfBoundsException {
        PointInterface point = new Point(-1,-1);
        board.placeAmazonsAtPoints(new PointInterface[]{point});
    }

    @Test(expected = PointOutOfBoundsException.class)
    public void incorrectPlacementResultsInNoChange() throws OverlappingAmazonsException, PointOutOfBoundsException {
        PointInterface testPoint = new Point(1,2);
        PointInterface invalidPoint = new Point(-1,-1);
        PointInterface testPoint2 = new Point(2,1);
        boolean result = true;
        
        result &= board.getCellStatusAtPoint(testPoint) == CellStatus.EMPTY;
        result &= board.getCellStatusAtPoint(invalidPoint) == CellStatus.EMPTY;
        result &= board.getCellStatusAtPoint(testPoint2) == CellStatus.EMPTY;

        board.placeAmazonsAtPoints(new PointInterface[]{testPoint, invalidPoint, testPoint2});

        result &= board.getCellStatusAtPoint(testPoint) == CellStatus.EMPTY;
        result &= board.getCellStatusAtPoint(invalidPoint) == CellStatus.EMPTY;
        result &= board.getCellStatusAtPoint(testPoint2) == CellStatus.EMPTY;

        assertTrue(result);
    }

    @Test
    public void selectAmazonWithValidParameters() throws PointOutOfBoundsException, OverlappingAmazonsException, AmazonSelectionException {
        BoardInterface myBoard = new Board(1);
        PointInterface point = new Point(0,0);
        myBoard.placeAmazonsAtPoints(new PointInterface[]{point});
        myBoard.selectAmazonAtPoint(point);
    }

    @Test(expected = PointOutOfBoundsException.class)
    public void selectAmazonWithOOBPointThrowsException() throws PointOutOfBoundsException, OverlappingAmazonsException, AmazonSelectionException {
        PointInterface point = new Point(-1, -1);
        board.selectAmazonAtPoint(point);
    }

    @Test(expected = AmazonSelectionException.class)
    public void selectNonExistantAmazonThrowsException() throws PointOutOfBoundsException, AmazonSelectionException {
        PointInterface point = new Point(0, 0);
        board.selectAmazonAtPoint(point);
    }

    @Test
    public void deselectAmazonAfterSelecting() throws PointOutOfBoundsException, OverlappingAmazonsException, AmazonSelectionException {
        BoardInterface myBoard = new Board(1);
        PointInterface point = new Point(0, 0);
        myBoard.placeAmazonsAtPoints(new PointInterface[]{point});
        myBoard.selectAmazonAtPoint(point);
        myBoard.deselectAmazon();
    }

    @Test(expected = AmazonSelectionException.class)
    public void deselectAmazonWithNoSelection() throws AmazonSelectionException {
        board.deselectAmazon();
    }
    
    @Test(expected = AmazonSelectionException.class)
    public void getTargetWithoutSelectingThrowsException() throws AmazonSelectionException, PointOutOfBoundsException {
        board.getValidTargetsAroundSelectedAmazon();
    }

    @Test
    public void getTargetOnFullBoardReturnsEmptyArray() throws PointOutOfBoundsException, OverlappingAmazonsException, AmazonSelectionException {
        BoardInterface smallBoard = new Board(1);
        PointInterface point = new Point(0,0);
        smallBoard.placeAmazonsAtPoints(new PointInterface[]{point});
        smallBoard.selectAmazonAtPoint(point);
        PointInterface[] targets = smallBoard.getValidTargetsAroundSelectedAmazon();
        assertTrue(targets.length == 0);
    }

    @Test
    public void getTargetOnEmptyBoardReturnsStarShape() throws PointOutOfBoundsException, OverlappingAmazonsException, AmazonSelectionException {
        BoardInterface emptyBoard = new Board(5);
        PointInterface point = new Point(2,2);
        emptyBoard.placeAmazonsAtPoints(new PointInterface[]{point});
        emptyBoard.selectAmazonAtPoint(point);

        PointInterface[] targets = emptyBoard.getValidTargetsAroundSelectedAmazon();
        PointInterface[] expected = new PointInterface[]{
            new Point(0, 0), new Point(2, 0), new Point(4, 0),
            new Point(1, 1), new Point(2, 1), new Point(3, 1),
            new Point(0, 2), new Point(1, 2), new Point(3, 2), new Point(4, 2),
            new Point(1, 3), new Point(2, 3), new Point(3, 3),
            new Point(0, 4), new Point(2, 4), new Point(4, 4)
        };

        HashSet<PointInterface> targetsAsHashSet = new HashSet<PointInterface>(Arrays.asList(targets));
        HashSet<PointInterface> expectedAsHashSet = new HashSet<PointInterface>(Arrays.asList(expected));

        assertTrue(targetsAsHashSet.equals(expectedAsHashSet));
    }

    @Test
    public void getTargetOnBoardWithBlockers() throws PointOutOfBoundsException, OverlappingAmazonsException, AmazonSelectionException {
        BoardInterface boardWithBlockers = new Board(5);
        PointInterface searchPoint = new Point(2,2);
        boardWithBlockers.placeAmazonsAtPoints(new PointInterface[]{searchPoint, new Point(1,1), new Point(2,4)});
        boardWithBlockers.selectAmazonAtPoint(searchPoint);

        PointInterface[] targets = boardWithBlockers.getValidTargetsAroundSelectedAmazon();
        PointInterface[] expected = new PointInterface[]{
                                new Point(2, 0), new Point(4, 0),
                                new Point(2, 1), new Point(3, 1),
            new Point(0, 2), new Point(1, 2), new Point(3, 2), new Point(4, 2),
            new Point(1, 3), new Point(2, 3), new Point(3, 3),
            new Point(0, 4),                     new Point(4, 4)
        };

        HashSet<PointInterface> targetsAsHashSet = new HashSet<PointInterface>(Arrays.asList(targets));
        HashSet<PointInterface> expectedAsHashSet = new HashSet<PointInterface>(Arrays.asList(expected));

        assertTrue(targetsAsHashSet.equals(expectedAsHashSet));
    }
}
