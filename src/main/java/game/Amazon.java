package game;

public class Amazon implements AmazonInterface {
    private PointInterface position;

    public Amazon(PointInterface initialPosition) {
        this.position = initialPosition;
    }

    public void setPosition(PointInterface newPosition) {
        this.position = newPosition;
    }

    public PointInterface getPosition() {
        return position;
    }
}