package game;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CellTest {

    @Test
    public void defaultStatusIsEmpty() {
        CellInterface cell = new Cell();
        assertTrue(cell.getStatus() == CellStatus.EMPTY);
    }

    public void setStatusByConstructor() {
        CellInterface cell = new Cell(CellStatus.ARROW);
        assertTrue(cell.getStatus() == CellStatus.ARROW);
    }

    @Test
    public void setStatusChangesStatus() {
        CellInterface cell = new Cell();
        cell.setStatus(CellStatus.ARROW);
        assertTrue(cell.getStatus() == CellStatus.ARROW);
    }
}
