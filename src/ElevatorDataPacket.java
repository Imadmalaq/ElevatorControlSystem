public class ElevatorDataPacket {

	 private int currentFloor;
	 private int elevatorPort;
	 private int id;

	 public ElevatorDataPacket(int currentFloor, int elevatorPort, int id) {
		 this.currentFloor = currentFloor;
		 this.elevatorPort = elevatorPort;
		 this.id = id;
	 }

	 public int getCurrentFloor() {
		  return currentFloor;
	 }

	 public void setId(int id) {
		  this.id = id;
	 }

	 public int getId() {
		  return id;
	 }

	 public int getElevatorPort() {
		  return elevatorPort;
	 }

	 public void setElevatorPort(int elevatorPort) {
		  this.elevatorPort = elevatorPort;
	 }

	 public void setCurrentFloor(int currentFloor) {
		  this.currentFloor = currentFloor;
	 }

}
