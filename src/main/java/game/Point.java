package game;

public class Point implements PointInterface {
    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    };

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof PointInterface)) {
            return false;
        }

        PointInterface pointInterface = (PointInterface) object;
        boolean out = (x == pointInterface.getX()) && (y == pointInterface.getY());
        return out;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + x;
        hash = 31 * hash + y;
        return hash;
    }
}