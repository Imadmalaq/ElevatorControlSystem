import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a data packet containing information about a request or command
 * within an elevator system. This includes a timestamp, floor number, direction,
 * car button, and fault type.
 *
 * @version 1.0
 * @since 2024-04-10
 * @author Humam Khalil
 * @author Imad Mohamed
 * @author Michael Rochefort
 * @author Kieran Rourke
 * @author Kyle Taticek
 */
public class DataPacket implements Serializable {
    private final String time;
    private final String floor;
    private final String direction;
    private final String carButton;
    private final String faultType; // field for fault type

    /**
     * Constructs a new DataPacket with specified details.
     *
     * @param time The timestamp of the event.
     * @param floor The floor number involved in the request or event.
     * @param direction The direction of the request, if applicable.
     * @param carButton The car button pressed, if applicable.
     * @param faultType The type of fault, if any.
     */
    public DataPacket(String time, String floor, String direction, String carButton, String faultType) {
        this.time = time;
        this.floor = floor;
        this.direction = direction;
        this.carButton = carButton;
        this.faultType = faultType;
    }

    /**
     * Gets the time of the instruction.
     *
     * @return The current time as a String.
     */
    public String getTime() {
        return time;
    }

    /**
     * Gets the floor to be moved to.
     *
     * @return The floor as a String.
     */
    public String getFloor() {
        return floor;
    }

    /**
     * Gets the direction the Car is moving.
     *
     * @return The Car direction as a String.
     */
    public String getDirection() {
        return direction;
    }

    /**
     * Gets the car button that was pressed.
     *
     * @return The Car button as a String.
     */
    public String getCarButton() {
        return carButton;
    }

    /**
     * Gets the fault type of the data packet.
     *
     * @return The fault type as a String.
     */
    public String getFaultType() { // Getter for faultType
        return faultType;
    }

    /**
     * Returns a string representation of the data packet, concatenating all its fields.
     *
     * @return A string representation of the DataPacket.
     */
    @Override
    public String toString() {
        return time + " " + floor + " " + direction + " " + carButton + " " + faultType;
    }

    /**
     * Compares this DataPacket to another object for equality.
     *
     * @param o The object to compare this DataPacket against.
     * @return true if the given object represents a DataPacket equivalent to this data packet, false otherwise.
     */
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

    /**
     * Generates a hash code for this DataPacket.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(time, floor, direction, carButton, faultType);
    }
}
