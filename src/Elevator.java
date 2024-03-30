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

    public enum ElevatorState {
        IDLE, MOVING, NOTIFY_SCHEDULER, DOOR_OPENING, DOOR_CLOSING
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
            byte[] sendBuffer = new byte[MainSystem.buffer_size]; // Adjust size as necessary
            byte[] returnBuffer = new byte[MainSystem.buffer_size]; // Adjust size as necessary

            // Create a DatagramPacket for receiving data

            sendBuffer = (id + "Get Request Elevator").getBytes();

            DatagramPacket request = new DatagramPacket(sendBuffer, sendBuffer.length, MainSystem.address, MainSystem.Scheduler_Elevator_Port_Number);
            DatagramPacket response = new DatagramPacket(returnBuffer, returnBuffer.length, MainSystem.address, MainSystem.Elevator_Port_Number + id);
            ;
//                Thread.sleep(300);
            MainSystem.rpc_send(request, response, id);
            MainSystem.printReceivePacketData(response);

            MainSystem.sendAcknowledgment(response);


            DataPacket receivedData;
            // Deserialize the data from the received packet into a DataPacket object
            if (Floor.isValidDataPacket(new String(response.getData(),0, response.getLength()))) {
                receivedData = Floor.processStringIntoDataPacket(new String(response.getData(),0, response.getLength()));
            } else {
                return;
            }

            // Process the received DataPacket
            if (receivedData != null) {
                this.currentDataPacket = receivedData;
                System.out.println("Elevator received data: " + receivedData.getFloor() + " " + receivedData.getDirection() + " " + receivedData.getCarButton());
                // Additional processing can be done here
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
                Long.toString(System.currentTimeMillis()), // Use current time as timestamp
                Integer.toString(currentFloor),
                direction,
                Integer.toString(targetFloor)); // Use currentFloor for both floor and carButton as a simple notification

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
                    //Get the request from the scheduler
                    getDataFromScheduler();
                    if (currentDataPacket != null) {
                        //If there is a request, move to the floor. We change to the moving state
                        System.out.println("Moving to floor: " + currentDataPacket.getFloor());
                        targetFloor = Integer.parseInt(currentDataPacket.getCarButton());
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
                        if(direction.equals("Up")){
                            currentFloor++;
                        }else{
                            currentFloor--;
                        }
                        //So the state goes back to moving, as the elevator still has floors to travel
                        System.out.println("Moving to floor: " + currentFloor);
                        currentState = ElevatorState.MOVING;
                    }else{
                        // When the elevator reaches a floor that it must actually stop at, the doors will open
                        // Then it changes to the door opening state
                        System.out.println("Door opening at floor: " + currentFloor);
                        currentState = ElevatorState.DOOR_OPENING;
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
                    if(currentFloor == targetFloor){
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
                        //This handles if the direction is up or down
                        if(direction.equals("Up")){
                            currentFloor++;
                        }else{
                            currentFloor--;
                        }
                        //If the elevator has more floors to climb or drop, it moves to the floor, and changes states to MOVING
                        System.out.println("Moving to floor: " + currentFloor);
                        // Notify the scheduler about the completion of the task
                        currentState = ElevatorState.MOVING;
                    }
                    break;
            }
        }
    }

    public static void main (String[] args) {
        Elevator elevator0 =  new Elevator();
        elevator0.setId(0);

        Elevator elevator1 =  new Elevator();
        elevator1.setId(1);

//        Elevator elevator2 =  new Elevator();
//        elevator0.setId(2);

        elevator0.start();
        elevator1.start();
//        elevator2.start();

    }
}
