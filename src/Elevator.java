import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

public class Elevator implements Runnable {

    private final MainSystem mainSystem;
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

    public Elevator(MainSystem mainSystem) {
        this.mainSystem = mainSystem;
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

    /**
     * NEW ADDED CODE FOR ITERATION 3 BELOW
     */
    public void getDataFromScheduler() {
        try {
            // Create a DatagramSocket to listen on a specific port
            DatagramSocket socket = new DatagramSocket(Elevator_Port_Number); // Need to choose an actual port number

            // Prepare a buffer to store incoming data
            byte[] buffer = new byte[1024]; // Adjust size as necessary

            // Create a DatagramPacket for receiving data
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

            // Receive the packet
            socket.receive(receivePacket);

            // Close the socket
            socket.close();

            // Deserialize the data from the received packet into a DataPacket object
            DataPacket receivedData = deserializeDataPacket(receivePacket.getData());

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

    public void sendDataToScheduler(DataPacket packet) {
        try {
            // Serialize the DataPacket object into a byte array - Need some adjustments
            byte[] dataToSend = serializeDataPacket(packet);

            // Specify the IP address and port of the Scheduler
            InetAddress schedulerAddress = InetAddress.getByName("Scheduler_IP_Address");
            int schedulerPort = Scheduler_Port_Number; // Need to choose an actual port number

            // Create a DatagramPacket for sending data
            DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, schedulerAddress, schedulerPort);

            // Create a DatagramSocket for sending the packet
            DatagramSocket socket = new DatagramSocket();
            socket.send(sendPacket);
            socket.close();

            System.out.println("Data sent to Scheduler from Elevator.");
        } catch (Exception e) {
            System.out.println("Exception in sendDataToScheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public byte[] serializeDataPacket(DataPacket packet) {
        try {
            // Create a ByteArrayOutputStream
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            // Create an ObjectOutputStream from the ByteArrayOutputStream
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);

            // Write the object to the stream
            objStream.writeObject(packet);

            // Flush and close the stream
            objStream.flush();
            objStream.close();

            // Convert the ByteArrayOutputStream into a byte array
            byte[] serializedData = byteStream.toByteArray();

            return serializedData;
        } catch (Exception e) {
            System.out.println("Exception in serializeDataPacket: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    //Deserializes a byte array back into a DataPacket object.
    public DataPacket deserializeDataPacket(byte[] data) {
        try {
            // Create a ByteArrayInputStream using the data
            ByteArrayInputStream byteStream = new ByteArrayInputStream(data);

            // Create an ObjectInputStream from the ByteArrayInputStream
            ObjectInputStream objStream = new ObjectInputStream(byteStream);

            // Read the object from the ObjectInputStream and cast it to DataPacket
            DataPacket packet = (DataPacket) objStream.readObject();

            // Close the streams
            objStream.close();
            byteStream.close();

            return packet;
        } catch (Exception e) {
            System.out.println("Exception in deserializeDataPacket: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /* OLD CODE FOR GETTING DATA AND SENDING DATA

    public void getDataFromScheduler() {
        currentDataPacket = mainSystem.getSchedulerAndElevatorData();
        if (currentDataPacket != null) {
            this.initialFloor = Integer.parseInt(currentDataPacket.getFloor());
            this.currentFloor = initialFloor;
            this.targetFloor = Integer.parseInt(currentDataPacket.getCarButton());
            this.direction = currentDataPacket.getDirection();
            System.out.println("Elevator received: " + currentDataPacket.getTime() + " " + currentDataPacket.getFloor() + " " + currentDataPacket.getDirection() + " " + currentDataPacket.getCarButton());
            if (!testModeEnabled) {
                // In test mode, do not automatically transition states
                this.currentState = ElevatorState.MOVING;
            }
        }
    }
    public void sendDataToScheduler(DataPacket packet) {
        mainSystem.updateSchedulerAndElevatorData(packet);
        try {
            if (!testModeEnabled) {
                Thread.sleep(1000); // Simulate delay
            }
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted: " + e.getMessage());
        }
        if (testModeEnabled && this.currentFloor == this.targetFloor) {
            // In test mode, simulate transition to IDLE after sending data if target floor is reached
            this.currentState = ElevatorState.IDLE;
        }
    }
     */

    /**
     * Notifies the scheduler that the elevator has reached a specific floor.
     */
    public void notifySchedulerFloorReached() {
        System.out.println("Elevator arrived at floor "+ currentFloor + ", notifying scheduler ");
        DataPacket notificationPacket = new DataPacket(
                Long.toString(System.currentTimeMillis()), // Use current time as timestamp
                Integer.toString(currentFloor),
                direction,
                Integer.toString(currentFloor)); // Use currentFloor for both floor and carButton as a simple notification

        sendDataToScheduler(notificationPacket);
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
                        System.out.println("Elevator is now idle, notifying the scheduler");
                        //The below method will end our threads since the elevator is sending back the data
                        sendDataToScheduler(currentDataPacket);
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
}
