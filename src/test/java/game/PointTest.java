package game;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PointTest {

    @Test
    public void createPointWithNormalInput() {
        PointInterface point = new Point(1, 1);
        assertTrue(point instanceof PointInterface);
    }

    @Test
    public void getXReturnsExpected() {
        PointInterface point = new Point(1, 1);
        assertTrue(point.getX() == 1);
    }

    @Test
    public void getYReturnsExpected() {
        PointInterface point = new Point(1, 1);
        assertTrue(point.getY() == 1);
    }

    @Test
    public void twoPointsWithSameCoordAreEqual() {
        PointInterface pointOne = new Point(1, 1);
        PointInterface pointTwo = new Point(1, 1);
        assertTrue(pointOne.equals(pointTwo));
    }

    @Test
    public void twoPointsWithSameCoordHaveTheSameHashCode() {
        PointInterface pointOne = new Point(1, 1);
        PointInterface pointTwo = new Point(1, 1);
        assertTrue(pointOne.hashCode() == pointTwo.hashCode());
    }
}
