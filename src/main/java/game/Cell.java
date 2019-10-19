package game;

public class Cell implements CellInterface {
    private CellStatus status;

    public Cell() {
        this.status = CellStatus.EMPTY;
    }

    public Cell(CellStatus status) {
        this.status = status;
    }

    public void setStatus(CellStatus status) {
        this.status = status;
    }

    public CellStatus getStatus() {
        return status;
    }
}
