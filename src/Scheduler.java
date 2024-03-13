import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Scheduler implements Runnable {

    private final MainSystem mainSystem;
    private DataPacket currentDataPacket; // This is the data packet that the scheduler is currently working on

    private SchedulerState currentState;

    private int initialFloor;
    private int currentFloor;
    private int targetFloor;

    private String direction;
    private boolean testModeEnabled = false; // Flag to indicate if test mode is enabled
    private boolean stopAfterOneCycle = false; // For testing: Stop the scheduler after one cycle

    public enum SchedulerState {
        WAITING_FOR_REQUEST, FLOOR_REQUEST_RECEIVED, SENDING_REQUEST_TO_ELEVATOR, WAITING_FOR_ELEVATOR_RESPONSE, PROCESSING_ELEVATOR_RESPONSE
    }

    public Scheduler(MainSystem mainSystem) {
        this.mainSystem = mainSystem;
        this.currentState = SchedulerState.WAITING_FOR_REQUEST;
    }

    public void enableTestMode() {
        this.testModeEnabled = true;
    }

    public DataPacket getCurrentDataPacket(){
        return currentDataPacket;
    }

    public void setDataPacket(DataPacket p){
        currentDataPacket = p;
    }

    /**
     * NEW ADDED CODE FOR ITERATION 3 BELOW
     */
    public void listenForRequests() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(Scheduler_Port_Number); // Use the port number the Scheduler will listen on
            byte[] buffer = new byte[1024]; // Adjust size as necessary

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Deserialize the data from the received packet
                DataPacket receivedData = deserializeDataPacket(packet.getData());

                // Process the received DataPacket
                if (receivedData != null) {
                    // Here, we would need to add logic to handle the received data,
                    System.out.println("Received request: " + receivedData.getFloor() + " " + receivedData.getDirection());
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in listenForRequests: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public void sendDataToElevator(DataPacket packet, String elevatorAddress, int elevatorPort) {
        try {
            // Serialize the DataPacket object into a byte array
            byte[] dataToSend = serializeDataPacket(packet);

            // Specify the IP address and port of the target Elevator
            InetAddress address = InetAddress.getByName(elevatorAddress);

            // Create a DatagramPacket for sending data
            DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, address, elevatorPort);

            // Create a DatagramSocket for sending the packet
            DatagramSocket socket = new DatagramSocket();
            socket.send(sendPacket);
            socket.close();

            System.out.println("Data sent to Elevator at " + elevatorAddress + ":" + elevatorPort);
        } catch (Exception e) {
            System.out.println("Exception in sendDataToElevator: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void enableStopAfterOneCycle() {
        this.stopAfterOneCycle = true;
    }

    public SchedulerState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(SchedulerState state) {
        this.currentState = state;
    }

    /**
     * Gets the data from the floor and sets the currentDataPacket
     */
    public void getDataFromFloor () {
        currentDataPacket = mainSystem.getSchedulerAndFloorData();
        this.initialFloor = Integer.parseInt(currentDataPacket.getFloor());
        this.currentFloor = initialFloor;
        this.targetFloor = Integer.parseInt(currentDataPacket.getCarButton());
        this.direction = currentDataPacket.getDirection();
        System.out.println("Scheduler received: " + currentDataPacket.getTime() + " " + currentDataPacket.getFloor() + " " + currentDataPacket.getDirection() + " " + currentDataPacket.getCarButton());
    }

    /* OLD CODE BELOW
    public void sendDataToElevator() {
        System.out.println("Sending Data to elevator from scheduler: " + currentDataPacket.getTime() + " " + currentDataPacket.getFloor() + " " + currentDataPacket.getDirection() + " " + currentDataPacket.getCarButton());
        mainSystem.updateSchedulerAndElevatorData(currentDataPacket);
        if (!testModeEnabled) {
            simulateDelay(750);
        }
    }

    public void getDataFromElevator() {
        currentDataPacket = mainSystem.getSchedulerAndElevatorData();
        System.out.println("Scheduler: received that elevator reached floor " + currentFloor);
    }

    public void sendDataToFloor() {
        System.out.println("Sending Data to floor from scheduler: " + currentDataPacket.getTime() + " " + currentDataPacket.getFloor() + " " + currentDataPacket.getDirection() + " " + currentDataPacket.getCarButton());
        mainSystem.updateSchedulerAndFloorData(currentDataPacket);
        if (!testModeEnabled) {
            simulateDelay(750);
        }
    }

    private void simulateDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted: " + e.getMessage());
        }
    }
     */

    @Override
    public void run() {
        if (testModeEnabled) {
            System.out.println("Test mode is enabled. Scheduler will not enter the main loop.");
            return; // Prevent entering the loop when in test mode
        }
        while(true) {
            switch (currentState) {
                //This state handles waiting for a floor request
                case WAITING_FOR_REQUEST:
                    getDataFromFloor();
                    if(currentDataPacket != null){
                        //If there is a request, we receive it and change states to FLOOR_REQUEST_RECEIVED
                        currentState = SchedulerState.FLOOR_REQUEST_RECEIVED;
                        //System.out.println("Received from floor");
                    }
                    break;
                case FLOOR_REQUEST_RECEIVED:
                    // Send the floor request to the elevator
                    sendDataToElevator();
                    // System.out.println("Sending floor request to elevator, initial floor: " + initalFloor + "target floor: " + targetFloor);
                    currentState = SchedulerState.SENDING_REQUEST_TO_ELEVATOR;
                    break;
                case SENDING_REQUEST_TO_ELEVATOR:
                    //Here the scheduler waits for a response from the elevator that it reached a floor
                    //System.out.println("Waiting for elevator response");
                    currentState = SchedulerState.WAITING_FOR_ELEVATOR_RESPONSE;
                    break;
                case WAITING_FOR_ELEVATOR_RESPONSE:
                    //Here the scheduler receives the response from the elevator that it reached a floor
                    getDataFromElevator();
                    //System.out.println("Response received elevator reached floor " + currentFloor);
                    if(currentFloor == initialFloor || currentFloor != targetFloor){
                        //Handles the direction
                        if(direction.equals("Up")){
                            currentFloor++;
                        }else{
                            currentFloor--;
                        }
                        //If there is more floors to move to, it changes states to SEND_REQUEST_TO_ELEVATOR
                        currentState = SchedulerState.SENDING_REQUEST_TO_ELEVATOR;
                    }else{
                        //Else we have reached the target floor
                        getDataFromElevator();
                        if (currentDataPacket.getFloor().equals(currentDataPacket.getCarButton())) { // Simple check to recognize the notification
                            System.out.println("Scheduler: Elevator has reached floor " + currentDataPacket.getFloor());
                        }
                        currentState = SchedulerState.PROCESSING_ELEVATOR_RESPONSE;
                    }
                    break;
                case PROCESSING_ELEVATOR_RESPONSE:
                    //Elevator is now idle, the scheduler is now idle as well, it sends the data to the floor
                    // Changes states to WAITING_FOR_REQUEST
                    sendDataToFloor();
                    currentState = SchedulerState.WAITING_FOR_REQUEST;
                    break;
            }
            if (stopAfterOneCycle) {
                break; // Exit the loop after one cycle for testing purposes
            }
        }
    }
}
