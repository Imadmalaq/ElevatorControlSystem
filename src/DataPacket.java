import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataPacket that = (DataPacket) o;
        return Objects.equals(time, that.time) &&
                Objects.equals(floor, that.floor) &&
                Objects.equals(direction, that.direction) &&
                Objects.equals(carButton, that.carButton);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, floor, direction, carButton);
    }
}

