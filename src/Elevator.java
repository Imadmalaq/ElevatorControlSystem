import java.net.DatagramPacket;
import java.net.DatagramSocket;


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

    public enum ElevatorState {
        IDLE, MOVING, NOTIFY_SCHEDULER, DOOR_OPENING, DOOR_CLOSING, FAULT
    }

    public Elevator() {
        this.currentState = ElevatorState.IDLE;
    }

    public void enableTestMode() {
        this.testModeEnabled = true;
    }

    public ElevatorState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(ElevatorState state) {
        this.currentState = state;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * NEW ADDED CODE FOR ITERATION 3 BELOW
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
                Long.toString(System.currentTimeMillis()),
                Integer.toString(currentFloor),
                direction,
                Integer.toString(targetFloor),
                "NF" // Indicate No Fault in this notification
        );

        sendDataToScheduler(notificationPacket.toString());
    }

    // Method to set the current data packet for testing purposes
    public void setCurrentDataPacket(DataPacket packet) {
        this.currentDataPacket = packet;
    }

    // Method to get the current data packet for assertions in tests
    public DataPacket getCurrentDataPacket() {
        return this.currentDataPacket;
    }

    // Method to directly set the elevator's current floor for testing
    public void setCurrentFloor(int floor) {
        this.currentFloor = floor;
    }

    // Method to get the current floor for assertions in tests
    public int getCurrentFloor() {
        return this.currentFloor;
    }

    // Method to set the target floor directly for testing purposes
    public void setTargetFloor(int floor) {
        this.targetFloor = floor;
    }

    // Method to get the target floor for assertions in tests
    public int getTargetFloor() {
        return this.targetFloor;
    }

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

    private void handleFault(String faultType) {
        switch (faultType) {
            case "FT": // Floor Timer Fault (Hard Fault)
                System.out.println("Critical Fault Detected: Elevator " + id + " encountered a floor timer fault. Elevator shutting down.");
                currentState = ElevatorState.FAULT; // Shut down the elevator
                // Notify the scheduler about the fault and shutdown
                sendDataToScheduler("Elevator " + id + " FAULT: Floor timer fault. Elevator shutdown.");
                break;
            case "DOF": // Door Open Fault (Transient Fault)
                System.out.println("Transient Fault Detected: Elevator " + id + " door is stuck open. Attempting to fix...");
                // Simulate fixing the fault
                try {
                    Thread.sleep(1000); // Wait for 1 second to simulate fixing the fault
                    System.out.println("Fault Fixed: Elevator " + id + " door issue resolved. Resuming operations.");
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



    //Below is the state machine inside the run method
    @Override
    public void run() {
        if (testModeEnabled) {
            System.out.println("Test mode is enabled. The run method will not start an infinite loop.");
            return; // Exit to prevent infinite loop when in test mode
        }
        while(true){
            switch (currentState) {
                case IDLE:
                    // Existing IDLE state handling
                    getDataFromScheduler();
                    if (currentDataPacket != null) {
                        System.out.println("Moving to floor: " + currentDataPacket.getFloor());
                        targetFloor = Integer.parseInt(currentDataPacket.getCarButton());
                        hasReachedInitialFloor = false;
                        initialFloor = Integer.parseInt(currentDataPacket.getFloor());
                        direction = currentDataPacket.getDirection();
                        currentState = ElevatorState.MOVING;
                    }
                    break;
                case MOVING:
                    // Existing MOVING state handling
                    notifySchedulerFloorReached();
                    currentState = ElevatorState.NOTIFY_SCHEDULER;
                    break;
                case NOTIFY_SCHEDULER:
                    // Existing NOTIFY_SCHEDULER state handling
                    if(currentFloor != initialFloor && currentFloor != targetFloor){
                        handleIncrementingFloor();
                        System.out.println("Moving to floor: " + currentFloor);
                        currentState = ElevatorState.MOVING;
                    } else if (hasReachedInitialFloor){
                        System.out.println("Door opening at floor: " + currentFloor);
                        currentState = ElevatorState.DOOR_OPENING;
                    } else {
                        if (currentFloor == initialFloor){
                            hasReachedInitialFloor = true;
                        }
                        handleIncrementingFloor();
                        currentState = ElevatorState.MOVING;
                    }
                    break;
                case DOOR_OPENING:
                    // Existing DOOR_OPENING state handling
                    System.out.println("Door closing at floor: " + currentFloor);
                    currentState = ElevatorState.DOOR_CLOSING;
                    break;
                case DOOR_CLOSING:
                    // Existing DOOR_CLOSING state handling
                    if(currentFloor == targetFloor && hasReachedInitialFloor){
                        System.out.println("Elevator is now idle, notifying the scheduler\n");
                        sendDataToScheduler("Elevator is now idle");
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        currentState = ElevatorState.IDLE;
                    } else {
                        handleIncrementingFloor();
                        System.out.println("Moving to floor: " + currentFloor);
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


    public static void main (String[] args) {
        Elevator elevator0 =  new Elevator();
        elevator0.setId(0);

        Elevator elevator1 =  new Elevator();
        elevator1.setId(1);

        Elevator elevator2 =  new Elevator();
        elevator2.setId(2);

        elevator0.start();
        elevator1.start();
        elevator2.start();

    }
}
