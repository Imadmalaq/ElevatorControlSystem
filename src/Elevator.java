import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * The Elevator class simulates the behavior of an elevator within a building,
 * communicating with a central scheduler to receive floor requests and notify of its actions.
 * It operates as a thread to simulate real-time elevator movements and state changes.
 *
 * @version 1.0
 * @since 2024-04-10
 * @author Humam Khalil
 * @author Imad Mohamed
 * @author Michael Rochefort
 * @author Kieran Rourke
 * @author Kyle Taticek
 */
public class Elevator extends Thread {

    private int id;
    private DataPacket currentDataPacket;
    private ElevatorState currentState;

    private int initialFloor;
    private int currentFloor;
    private int targetFloor;

    private String direction;
    private boolean testModeEnabled = false; // Flag to indicate if test mode is enabled

    private boolean hasReachedInitialFloor = false;

    /**
     * Enum representing the various states an elevator can be in during its operation.
     */
    public enum ElevatorState {
        IDLE, MOVING, NOTIFY_SCHEDULER, DOOR_OPENING, DOOR_CLOSING, FAULT
    }

    /**
     * Constructs a new Elevator instance, initially setting its state to IDLE.
     */
    public Elevator() {
        this.currentState = ElevatorState.IDLE;
    }

    /**
     * Enables test mode for the elevator, preventing it from entering the main operational loop.
     */
    public void enableTestMode() {
        this.testModeEnabled = true;
    }

    /**
     * Gets the current state of the elevator.
     * @return The current ElevatorState.
     */
    public ElevatorState getCurrentState() {
        return currentState;
    }

    /**
     * Sets the current state of the elevator.
     * @param state The new state to set.
     */
    public void setCurrentState(ElevatorState state) {
        this.currentState = state;
    }

    /**
     * Sets the identifier for the elevator.
     * @param id The ID to set for the elevator.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Communicates with the scheduler to receive data packets indicating floor requests.
     */
    public void getDataFromScheduler() {
        try {
            // Prepare a buffer to store incoming data
            byte[] sendBuffer = (id + " Get Request Elevator").getBytes();
            byte[] returnBuffer = new byte[MainSystem.buffer_size];

            // Create a DatagramPacket for sending the request and receiving the response
            DatagramPacket request = new DatagramPacket(sendBuffer, sendBuffer.length, MainSystem.address, MainSystem.Scheduler_Elevator_Port_Number);
            DatagramPacket response = new DatagramPacket(returnBuffer, returnBuffer.length, MainSystem.address, MainSystem.Elevator_Port_Number + id);

            // Send the request and receive the response
            MainSystem.rpc_send(request, response, id);
            MainSystem.printReceivePacketData(response);
            MainSystem.sendAcknowledgment(response);

            // Extract the response data into a string
            String responseData = new String(response.getData(), 0, response.getLength());

            // Check if the response is a valid DataPacket
            if (Floor.isValidDataPacket(responseData)) {
                // Deserialize the data from the received packet into a DataPacket object
                DataPacket receivedData = Floor.processStringIntoDataPacket(responseData);
                if (receivedData != null) {
                    this.currentDataPacket = receivedData;
                    System.out.println("Elevator received data: " + receivedData.toString());

                    // Handle fault if faultType is not "NF"
                    if (!"NF".equals(receivedData.getFaultType())) {
                        handleFault(receivedData.getFaultType());
                        return; // Exit the method if there's a fault, halting further processing
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in getDataFromScheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends data to the scheduler, typically to notify it of state changes or completed tasks.
     * @param data The string data to send to the scheduler.
     */
    public void sendDataToScheduler(String data) {
        try {
            // Serialize the DataPacket object into a byte array - Need some adjustments
            byte[] dataToSend = (this.id + data).getBytes();

            // Specify the IP address and port of the Scheduler
            int schedulerPort = MainSystem.Scheduler_Elevator_Port_Number; // Need to choose an actual port number

            // Create a DatagramPacket for sending data
            DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, MainSystem.address, schedulerPort);

            // Create a DatagramSocket for sending the packet
            DatagramSocket socket = new DatagramSocket();
            socket.send(sendPacket);
            MainSystem.waitForAck(socket);
            socket.close();

        } catch (Exception e) {
            System.out.println("Exception in sendDataToScheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Notifies the scheduler that the elevator has reached a specific floor.
     */
    public void notifySchedulerFloorReached() {
        System.out.println("Elevator arrived at floor "+ currentFloor + ", notifying scheduler \n");
        DataPacket notificationPacket = new DataPacket(
                Long.toString(System.currentTimeMillis()), // Use current time as timestamp
                Integer.toString(currentFloor),
                direction,
                Integer.toString(targetFloor), "NF"); // Use currentFloor for both floor and carButton as a simple notification

        sendDataToScheduler(notificationPacket.toString());
    }

    /**
     * Sets the current data packet for testing or operational purposes.
     * @param packet The DataPacket to set as the current data packet.
     */
    public void setCurrentDataPacket(DataPacket packet) {
        this.currentDataPacket = packet;
    }

    /**
     * Retrieves the current data packet the elevator is processing.
     * @return The current DataPacket.
     */
    public DataPacket getCurrentDataPacket() {
        return this.currentDataPacket;
    }

    /**
     * Directly sets the elevator's current floor, used for testing or initialization.
     * @param floor The floor number to set as the current floor.
     */
    public void setCurrentFloor(int floor) {
        this.currentFloor = floor;
    }

    /**
     * Retrieves the current floor of the elevator.
     * @return The current floor number.
     */
    public int getCurrentFloor() {
        return this.currentFloor;
    }

    /**
     * Sets the target floor for the elevator to move to.
     * @param floor The target floor number.
     */
    public void setTargetFloor(int floor) {
        this.targetFloor = floor;
    }

    /**
     * Retrieves the target floor number.
     * @return The target floor number.
     */
    public int getTargetFloor() {
        return this.targetFloor;
    }

    /**
     * Handles elevator moving up floors.
     */
    private void handleIncrementingFloor() {
        if (hasReachedInitialFloor) {
            if (direction.equals("Up")) {
                currentFloor++;
            } else {
                currentFloor--;
            }
        } else {
            if (currentFloor < initialFloor) {
                currentFloor++;
            } else {
                currentFloor--;
            }
        }
    }

    /**
     * The main run loop for the elevator, handling its state machine and responding to scheduler requests.
     */
    @Override
    public void run() {
        if (testModeEnabled) {
            System.out.println("Test mode is enabled. The run method will not start an infinite loop.");
            return; // Exit to prevent infinite loop when in test mode
        }
        while(true){
            switch (currentState) {
                case IDLE:
                    //Get the request from the scheduler
                    getDataFromScheduler();
                    if (currentDataPacket != null) {
                        //If there is a request, move to the floor. We change to the moving state
                        System.out.println("Moving to floor: " + currentDataPacket.getFloor());
                        targetFloor = Integer.parseInt(currentDataPacket.getCarButton());
                        hasReachedInitialFloor = false;
                        initialFloor = Integer.parseInt(currentDataPacket.getFloor());
                        direction = currentDataPacket.getDirection();
                        currentState = ElevatorState.MOVING;
                    }
                    break;
                case MOVING:
                    // The elevator has now arrived at the floor, notify the scheduler that it has arrived
                    notifySchedulerFloorReached();
                    currentState = ElevatorState.NOTIFY_SCHEDULER;
                    break;
                case NOTIFY_SCHEDULER:
                    //The if statement below handles the case where the elevator would not stop at a floor
                    //So for example: if we wanted 1 Up 3, floor 2 would be handled in this if statement
                    if(currentFloor != initialFloor && currentFloor != targetFloor){
                        //This is what handles if the elevator is going up or down
                        handleIncrementingFloor();

                        //So the state goes back to moving, as the elevator still has floors to travel
                        System.out.println("Moving to floor: " + currentFloor);
                        currentState = ElevatorState.MOVING;
                    }else if (hasReachedInitialFloor){
                        // When the elevator reaches a floor that it must actually stop at, the doors will open
                        // Then it changes to the door opening state
                        System.out.println("Door opening at floor: " + currentFloor);
                        currentState = ElevatorState.DOOR_OPENING;
                    } else {
                        if(currentFloor == initialFloor) {
                            System.out.println("Door opening at floor: " + currentFloor);
                            currentState = ElevatorState.DOOR_OPENING;
                        } else {
                            handleIncrementingFloor();
                            System.out.println("Moving to floor test: " + currentFloor);
                            currentState = ElevatorState.MOVING;

                        }
                    }
                    break;
                case DOOR_OPENING:
                    // If the doors were opened, they have to close
                    // Change states to door closing
                    // Simulates the elevator door closing
                    System.out.println("Door closing at floor: " + currentFloor);
                    currentState = ElevatorState.DOOR_CLOSING;
                    break;
                case DOOR_CLOSING:
                    // If the current floor == targetFloor then we know the elevator reached its destination, so it notifys
                    // the scheduler, here we would also have a method to notify the scheduler but it's not implemented yet
                    if(currentFloor == targetFloor && hasReachedInitialFloor){
                        // Simulating the elevator being idle
                        System.out.println("Elevator is now idle, notifying the scheduler\n");
                        //The below method will end our threads since the elevator is sending back the data
                        sendDataToScheduler("Elevator is now idle");
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        currentState = ElevatorState.IDLE;
                    }else{

                        hasReachedInitialFloor = true;
                        //This handles if the direction is up or down
                        handleIncrementingFloor();
                        //If the elevator has more floors to climb or drop, it moves to the floor, and changes states to MOVING
                        System.out.println("Moving to floor: " + currentFloor);
                        // Notify the scheduler about the completion of the task
                        currentState = ElevatorState.MOVING;
                    }
                    break;
                case FAULT:
                    // New FAULT state handling
                    System.out.println("Elevator " + id + " is in a FAULT state, stopped at floor " + currentFloor);
                    sendDataToScheduler("Elevator " + id + " FAULT at floor " + currentFloor);
                    // Here, we could implement logic to attempt recovery or wait for maintenance
                    // For now, it will remain in this state until externally reset or fault is cleared
                    break;
            }
        }
    }

    /**
     * Handles elevator faults based on the type of fault encountered.
     * @param faultType The type of fault to handle.
     */
    private void handleFault(String faultType) {
        switch (faultType) {
            case "FT": // Floor Timer Fault (Hard Fault)
                System.out.println("Critical Fault Detected: Elevator " + id + " encountered a floor timer fault. Elevator shutting down.\n");
                currentState = ElevatorState.FAULT; // Shut down the elevator
                // Notify the scheduler about the fault and shutdown
                sendDataToScheduler("Elevator " + id + " FAULT: Floor timer fault. Elevator shutdown.");
                break;
            case "DOF": // Door Open Fault (Transient Fault)
                String output = "Transient Fault Detected: Elevator " + id + " door is stuck open. Attempting to fix...";
                System.out.println(output);
                sendDataToScheduler("Transient Fault Detected");
                // Simulate fixing the fault
                try {
                    Thread.sleep(1000); // Wait for 1 second to simulate fixing the fault
                    output = "Fault Fixed: Elevator " + id + " door issue resolved. Resuming operations.";
                    System.out.println(output);
                    sendDataToScheduler("Fault Fixed");
                    // No state change needed, just resume operations
                } catch (InterruptedException e) {
                    System.out.println("Error while handling transient fault for Elevator " + id);
                }
                break;
            default:
                System.out.println("Unknown fault type received for Elevator " + id);
                break;
        }
    }

    /**
     * The main method to start elevator simulation.
     * @param args Command line arguments (not used).
     */
    public static void main (String[] args) {
        Elevator elevator0 =  new Elevator();
        elevator0.setId(0);

        Elevator elevator1 =  new Elevator();
        elevator1.setId(1);

        Elevator elevator2 =  new Elevator();
        elevator2.setId(2);

        Elevator elevator3 =  new Elevator();
        elevator3.setId(3);

        elevator0.start();
        elevator1.start();
        elevator2.start();
        elevator3.start();

    }
}
