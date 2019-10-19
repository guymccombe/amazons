package game;

public class Placement {
    private PointInterface point;
    private boolean isWhite;

    public Placement(PointInterface point, boolean isWhite) {
        this.isWhite = isWhite;
        this.point = point;
    }

    public PointInterface getPoint() {
        return point;
    }

    public boolean isWhite() {
        return isWhite;
    }
}
