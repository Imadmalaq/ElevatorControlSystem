import java.io.Serializable;
import java.util.Objects;

public class DataPacket implements Serializable {
    private final String time;
    private final String floor;
    private final String direction;
    private final String carButton;
    private final String faultType; // New field for fault type

    // Updated constructor to include faultType
    public DataPacket(String time, String floor, String direction, String carButton, String faultType) {
        this.time = time;
        this.floor = floor;
        this.direction = direction;
        this.carButton = carButton;
        this.faultType = faultType;
    }

    // Getters for all fields, including the new faultType field
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

    public String getFaultType() { // Getter for faultType
        return faultType;
    }

    // Updated toString method to include faultType
    @Override
    public String toString() {
        return time + " " + floor + " " + direction + " " + carButton + " " + faultType;
    }

    // Updated equals method to include faultType
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataPacket that = (DataPacket) o;
        return Objects.equals(time, that.time) &&
                Objects.equals(floor, that.floor) &&
                Objects.equals(direction, that.direction) &&
                Objects.equals(carButton, that.carButton) &&
                Objects.equals(faultType, that.faultType);
    }

    // Updated hashCode method to include faultType
    @Override
    public int hashCode() {
        return Objects.hash(time, floor, direction, carButton, faultType);
    }
}
