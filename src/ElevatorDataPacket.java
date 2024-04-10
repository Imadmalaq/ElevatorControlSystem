/**
 * Represents a data packet containing the state of an elevator. This includes the current floor,
 * the network port the elevator is listening on, and the elevator's unique identifier.
 *
 * @version 1.0
 * @since 2024-04-10
 * @author Humam Khalil
 * @author Imad Mohamed
 * @author Michael Rochefort
 * @author Kieran Rourke
 * @author Kyle Taticek
 */
public class ElevatorDataPacket {

	 private int currentFloor;
	 private int elevatorPort;
	 private int id;

	/**
	 * Constructs a new ElevatorDataPacket with specified details about the elevator's state.
	 *
	 * @param currentFloor The floor number where the elevator currently is.
	 * @param elevatorPort The network port number the elevator is listening on for commands.
	 * @param id           The unique identifier of the elevator.
	 */
	 public ElevatorDataPacket(int currentFloor, int elevatorPort, int id) {
		 this.currentFloor = currentFloor;
		 this.elevatorPort = elevatorPort;
		 this.id = id;
	 }

	/**
	 * Gets the current floor where the elevator is located.
	 *
	 * @return The current floor of the elevator.
	 */
	 public int getCurrentFloor() {
		  return currentFloor;
	 }

	/**
	 * Sets the elevator's unique identifier.
	 *
	 * @param id The unique ID to assign to the elevator.
	 */
	 public void setId(int id) {
		  this.id = id;
	 }

	 /**
	 * Gets the elevator's unique identifier.
	 *
	 * @return The ID of the elevator.
	 */
	 public int getId() {
		  return id;
	 }

	/**
	 * Gets the network port number the elevator listens on for commands.
	 *
	 * @return The network port number of the elevator.
	 */
	 public int getElevatorPort() {
		  return elevatorPort;
	 }

	/**
	 * Sets the network port number the elevator should listen on for commands.
	 *
	 * @param elevatorPort The port number to set for the elevator.
	 */
	 public void setElevatorPort(int elevatorPort) {
		  this.elevatorPort = elevatorPort;
	 }

	/**
	 * Sets the current floor of the elevator.
	 *
	 * @param currentFloor The floor number to set as the elevator's current location.
	 */
	 public void setCurrentFloor(int currentFloor) {
		  this.currentFloor = currentFloor;
	 }

}
