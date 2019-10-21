package game;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class BoardTest {

    BoardInterface board = new Board();

    @Test
    public void selectAmazonWithValidParameters() throws PointOutOfBoundsException, AmazonSelectionException {
        PointInterface point = new Point(0,0);
        BoardInterface myBoard = new Board(1, new Placement[]{new Placement(point, true)});
        myBoard.selectAmazonAtPointAndReturnMoveTargets(point);
    }

    @Test(expected = PointOutOfBoundsException.class)
    public void selectAmazonWithOOBPointThrowsException() throws PointOutOfBoundsException, AmazonSelectionException {
        PointInterface point = new Point(-1, -1);
        board.selectAmazonAtPointAndReturnMoveTargets(point);
    }

    @Test(expected = AmazonSelectionException.class)
    public void selectNonExistantAmazonThrowsException() throws PointOutOfBoundsException, AmazonSelectionException {
        PointInterface point = new Point(0, 0);
        board.selectAmazonAtPointAndReturnMoveTargets(point);
    }

    @Test(expected = AmazonSelectionException.class)
    public void selectBlackAmazonOnWhiteMoveThrowsException() throws PointOutOfBoundsException, AmazonSelectionException {
        PointInterface point = new Point(0, 0);
        BoardInterface localBoard = new Board(1, new Placement[]{new Placement(point, false)});
        localBoard.selectAmazonAtPointAndReturnMoveTargets(point);
    }

    @Test(expected = AmazonSelectionException.class)
    public void selectWhiteInsteadOfBlack() throws PointOutOfBoundsException, AmazonSelectionException, InvalidMoveException {
        BoardInterface localBoard = new Board(2, new Placement[]{
            new Placement(new Point(0,0), true), new Placement(new Point(1,1), false)
        });
        localBoard.selectAmazonAtPointAndReturnMoveTargets(new Point(0,0));
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(0,1));
        localBoard.shootAtPoint(new Point(1,0));
        localBoard.selectAmazonAtPointAndReturnMoveTargets(new Point(0,1));
    }

    @Test(expected = AmazonSelectionException.class)
    public void selectAmazonInsteadOfShootingThrowsException() throws PointOutOfBoundsException, AmazonSelectionException, InvalidMoveException {
        BoardInterface localBoard = new Board();
        PointInterface[] targets = localBoard.selectAmazonAtPointAndReturnMoveTargets(new Point(0, 3));
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(targets[0]);
        localBoard.selectAmazonAtPointAndReturnMoveTargets(new Point(3, 0));
    }


    
    @Test
    public void getTargetOnFullBoardReturnsEmptyArray() throws PointOutOfBoundsException, AmazonSelectionException {
        PointInterface point = new Point(0,0);
        BoardInterface smallBoard = new Board(1, new Placement[]{new Placement(point, true)});
        PointInterface[] targets = smallBoard.selectAmazonAtPointAndReturnMoveTargets(point);
        assertTrue(targets.length == 0);
    }

    @Test
    public void getTargetOnEmptyBoardReturnsStarShape() throws PointOutOfBoundsException, AmazonSelectionException {
        PointInterface point = new Point(2,2);
        BoardInterface emptyBoard = new Board(5, new Placement[]{new Placement(point, true)});
        PointInterface[] targets = emptyBoard.selectAmazonAtPointAndReturnMoveTargets(point);

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
    public void getTargetOnBoardWithBlockers() throws PointOutOfBoundsException, AmazonSelectionException {
        PointInterface searchPoint = new Point(2,2);        
        Placement[] placements = new Placement[]{
            new Placement(searchPoint, true), new Placement(new Point(1,1), true), new Placement(new Point(2,4), false)
        };
        BoardInterface boardWithBlockers = new Board(5, placements);
        PointInterface[] targets = boardWithBlockers.selectAmazonAtPointAndReturnMoveTargets(searchPoint);
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




    @Test
    public void makeAValidMove() throws PointOutOfBoundsException, AmazonSelectionException, InvalidMoveException {
        PointInterface point = new Point(0,0);
        Placement[] placements = new Placement[]{new Placement(point, true)};
        BoardInterface localBoard = new Board(2, placements);
        localBoard.selectAmazonAtPointAndReturnMoveTargets(point);
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(1,1));
    }

    @Test(expected = InvalidMoveException.class)
    public void moveOutOfBounds() throws PointOutOfBoundsException, AmazonSelectionException, InvalidMoveException {
        BoardInterface localBoard = new Board();
        localBoard.selectAmazonAtPointAndReturnMoveTargets(new Point(0,3));
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(-1,3));
    }

    @Test(expected = InvalidMoveException.class)
    public void moveToAPositionNotInTarget() throws PointOutOfBoundsException, AmazonSelectionException, InvalidMoveException {
        BoardInterface localBoard = new Board();
        localBoard.selectAmazonAtPointAndReturnMoveTargets(new Point(0,3));
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(1,5));
    }

    @Test
    public void cellIsEmptyAfterMovingFromIt() throws PointOutOfBoundsException, AmazonSelectionException, InvalidMoveException {
        BoardInterface localBoard = new Board();
        PointInterface point = new Point(0,3);
        localBoard.selectAmazonAtPointAndReturnMoveTargets(point);
        PointInterface[] targets = localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(0,4));
        HashSet<PointInterface> targetsAsSet = new HashSet<>(Arrays.asList(targets));
        assertTrue(targetsAsSet.contains(point));
    }

    @Test
    public void validShot() throws PointOutOfBoundsException, AmazonSelectionException, InvalidMoveException {
        PointInterface point = new Point(0,0);
        BoardInterface localBoard = new Board(2, new Placement[]{new Placement(point, true)});
        localBoard.selectAmazonAtPointAndReturnMoveTargets(point);
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(0,1));
        localBoard.shootAtPoint(new Point(1,1));
    }

    @Test(expected = InvalidMoveException.class)
    public void shootingOutOfBounds() throws AmazonSelectionException, PointOutOfBoundsException, InvalidMoveException {
        PointInterface point = new Point(0,0);
        BoardInterface localBoard = new Board(2, new Placement[]{new Placement(point, true)});
        localBoard.selectAmazonAtPointAndReturnMoveTargets(point);
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(0,1));
        localBoard.shootAtPoint(new Point(-1,1));
    }

    @Test(expected = InvalidMoveException.class)
    public void shootingOutOfTargets() throws AmazonSelectionException, PointOutOfBoundsException, InvalidMoveException {
        PointInterface point = new Point(0,0);
        BoardInterface localBoard = new Board(3, new Placement[]{new Placement(point, true)});
        localBoard.selectAmazonAtPointAndReturnMoveTargets(point);
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(0,1));
        localBoard.shootAtPoint(new Point(2,2));
    }

    @Test
    public void movedAmazonsAndFiredArrowsBlockTarget() throws AmazonSelectionException, PointOutOfBoundsException, InvalidMoveException {
        PointInterface point = new Point(0,0);
        BoardInterface localBoard = new Board(2, new Placement[]{new Placement(point, true), new Placement(new Point(1,1), false)});
        localBoard.selectAmazonAtPointAndReturnMoveTargets(point);
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(0,1));
        localBoard.shootAtPoint(new Point(1,0));
        
        PointInterface[] targets = localBoard.selectAmazonAtPointAndReturnMoveTargets(new Point(1,1));
        HashSet<PointInterface> targetsAsSet = new HashSet<>(Arrays.asList(targets));
        assertTrue(!targetsAsSet.contains(new Point(0,1)) && !targetsAsSet.contains(new Point(1,0)));
    }



    @Test
    public void isGameFinishedOn0SizeBoard() {
        BoardInterface localBoard = new Board(0, new Placement[0]);
        assertTrue(!localBoard.isWhiteTheWinner());
    }

    @Test
    public void blackWins() throws AmazonSelectionException, PointOutOfBoundsException, InvalidMoveException {
        Placement[] placements = new Placement[]{
            new Placement(new Point(0,0), true), new Placement(new Point(1,1), false)
        };
        BoardInterface localBoard = new Board(2, placements);

        localBoard.selectAmazonAtPointAndReturnMoveTargets(new Point(0,0));
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(1,0));
        localBoard.shootAtPoint(new Point(0,0));
        
        localBoard.selectAmazonAtPointAndReturnMoveTargets(new Point(1,1));
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(0,1));
        localBoard.shootAtPoint(new Point(1,1));

        assertTrue(!localBoard.isWhiteTheWinner());
    }

    @Test
    public void whiteWins() throws AmazonSelectionException, PointOutOfBoundsException, InvalidMoveException {
        Placement[] placements = new Placement[]{
            new Placement(new Point(0,1), true), new Placement(new Point(1,0), false)
        };
        BoardInterface localBoard = new Board(3, placements);

        localBoard.selectAmazonAtPointAndReturnMoveTargets(new Point(0,1));
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(1,1));
        localBoard.shootAtPoint(new Point(0,1));
        
        localBoard.selectAmazonAtPointAndReturnMoveTargets(new Point(1,0));
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(0,0));
        localBoard.shootAtPoint(new Point(1,0));

        localBoard.selectAmazonAtPointAndReturnMoveTargets(new Point(1,1));
        localBoard.moveSelectedAmazonToPointAndReturnShootTargets(new Point(2,2));
        localBoard.shootAtPoint(new Point(1,1));

        assertTrue(localBoard.isWhiteTheWinner());
    }

    @Test
    public void testWinnerInUnfinishedGame() {
        assertTrue(board.isWhiteTheWinner() == null);
    }
}
