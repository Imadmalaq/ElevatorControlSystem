import java.io.Serializable;

public class DataPacket implements Serializable {
    private final String time;
    private final String floor;
    private final String direction;
    private final String carButton;

    public DataPacket(String time, String floor, String direction, String carButton) {
        this.time = time;
        this.floor = floor;
        this.direction = direction;
        this.carButton = carButton;
    }

    public String getTime() {
        return time;
    }

    public String getFloor() {
        return floor;
    }

    public String getDirection() {
        return direction;
    }

    public String getCarButton() {
        return carButton;
    }

    public String toString() {
        return time + " " + floor + " " + direction + " " + carButton;
    }
}
