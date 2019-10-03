package game;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AmazonTest {

    @Test
    public void getPositionReturnsExpected() {
        PointInterface point = new Point(1,1);
        AmazonInterface amazon = new Amazon(point);

        PointInterface newPoint = new Point(1,1);
        assertTrue(amazon.getPosition().equals(newPoint));
    }

    @Test
    public void setPositionUpdatesPosition() {
        PointInterface point = new Point(1,1);
        AmazonInterface amazon = new Amazon(point);
        PointInterface newPoint = new Point(5,5);
        amazon.setPosition(newPoint);
        
        assertTrue(amazon.getPosition().equals(newPoint));
    }
}