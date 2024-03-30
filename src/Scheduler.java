import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class Scheduler implements Runnable {

    private DataPacket currentDataPacket; // This is the data packet that the scheduler is currently working on

    private SchedulerState currentState;

    private int initialFloor;
    private int currentFloor;
    private int targetFloor;

    private String direction;
    private boolean testModeEnabled = false; // Flag to indicate if test mode is enabled
    private boolean stopAfterOneCycle = false; // For testing: Stop the scheduler after one cycle
    public HashMap<Integer, ElevatorDataPacket> elevatorData;
    private int numElevators;
    boolean hasReachedInitialFloor = false;


    public enum SchedulerState {
        WAITING_FOR_REQUEST, FLOOR_REQUEST_RECEIVED, SENDING_REQUEST_TO_ELEVATOR, WAITING_FOR_ELEVATOR_RESPONSE, PROCESSING_ELEVATOR_RESPONSE
    }

    public Scheduler(int numElevators) {
        this.currentState = SchedulerState.WAITING_FOR_REQUEST;
        elevatorData = new HashMap<>();
        this.numElevators = numElevators;
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
     * Gets the data from the elevator and sets the currentDataPacket
     * @return boolean - true if elevator is idle
     */
    public boolean getDataFromElevator(){
        boolean isIdle = false;
        System.out.println("Getting Data from Elevator\n");
        DatagramSocket elevatorSocket = null; // Use the port number the Elevator will listen on
        try {
            elevatorSocket = new DatagramSocket(MainSystem.Scheduler_Elevator_Port_Number);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        byte[] receiveData = new byte[MainSystem.buffer_size];
        DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);

        try {
            elevatorSocket.receive(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MainSystem.printReceivePacketData(packet);
        // Check if elevator is sending a data packet
        String packetData = new String(packet.getData(),0, packet.getLength());
        int id = Integer.parseInt(packetData.substring(0,1));
        packetData = packetData.substring(1);


        ElevatorDataPacket eData = new ElevatorDataPacket(0, MainSystem.Elevator_Port_Number + id, id);

        if (Floor.isValidDataPacket(packetData)){
            currentDataPacket = Floor.processStringIntoDataPacket(packetData);
            eData.setCurrentFloor(Integer.parseInt(currentDataPacket.getFloor()));
            System.out.println("Sending ACK to elevator\n");
            MainSystem.sendAcknowledgment(packet);
        } else if (packetData.equals("Elevator is now idle")) {
            System.out.println("Sending ACK to elevator\n");
            eData.setCurrentFloor(elevatorData.get(id).getCurrentFloor());
            MainSystem.sendAcknowledgment(packet);
            isIdle = true;
        } else {
            if (elevatorData.get(id) != null){
                eData.setCurrentFloor(elevatorData.get(id).getCurrentFloor());
            }
        }

        elevatorData.put(id, eData);
        elevatorSocket.close();
        return isIdle;

    }

    public void sendDataToElevator(String data, int elevatorID) {
        try {
            // Serialize the DataPacket object into a byte array
            byte[] packetData = new byte[MainSystem.buffer_size];
            DatagramSocket socket = new DatagramSocket();
            packetData = data.getBytes();
            DatagramPacket elevatorPacket = new DatagramPacket(packetData, packetData.length, MainSystem.address, MainSystem.Elevator_Port_Number + elevatorID);
            MainSystem.printSendPacketData(elevatorPacket);

            // Create a DatagramSocket for sending the packet
            socket.send(elevatorPacket);
            socket.close();
        } catch (Exception e) {
            System.out.println("Exception in sendDataToElevator: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void getDataFromFloor(){
        DatagramSocket floorSocket = null;
        try {
            floorSocket = new DatagramSocket(MainSystem.Scheduler_Floor_Port_Number);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        byte[] receiveData = new byte[MainSystem.buffer_size];
        DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);

        try {
            floorSocket.receive(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MainSystem.printReceivePacketData(packet);
        floorSocket.close();
        if (!Floor.isValidDataPacket(new String(packet.getData(),0, packet.getLength()))){ //Get Request
            return;
        }
        parseFloorData(new String(packet.getData(),0, packet.getLength()));


        System.out.println("Sending ACK to floor\n");
        MainSystem.sendAcknowledgment(packet);
    }

    public void sendDataToFloor(String data) {
        // Serialize the DataPacket object into a byte array
        byte[] packetData = new byte[MainSystem.buffer_size];
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        packetData = data.getBytes();
        DatagramPacket floorPacket = new DatagramPacket(packetData, packetData.length, MainSystem.address, MainSystem.Floor_Port_Number);
        MainSystem.printSendPacketData(floorPacket);
        try {
            socket.send(floorPacket);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
    public void parseFloorData (String data) {
        currentDataPacket = Floor.processStringIntoDataPacket(data);
        this.initialFloor = Integer.parseInt(currentDataPacket.getFloor());
        this.currentFloor = initialFloor;
        this.targetFloor = Integer.parseInt(currentDataPacket.getCarButton());
        this.direction = currentDataPacket.getDirection();
    }

    public int pickElevator (int startingFloor){
         ArrayList<ElevatorDataPacket> values = new ArrayList<>(elevatorData.values());

         ElevatorDataPacket closestElevator = values.get(0);
         int closestDistance = Math.abs(startingFloor - closestElevator.getCurrentFloor());

         for (ElevatorDataPacket e : values) {
            int distance = Math.abs(startingFloor - e.getCurrentFloor());
            if (distance < closestDistance){
              closestElevator = e;
              closestDistance = distance;
           }
         }
         System.out.println("Picking Elevator ID: " + closestElevator.getId() + " on floor " +
                closestElevator.getCurrentFloor() + " for floor " + startingFloor + "\n");
         return closestElevator.getId();

    }

    @Override
    public void run() {
        if (testModeEnabled) {
            System.out.println("Test mode is enabled. Scheduler will not enter the main loop.");
            return; // Prevent entering the loop when in test mode
        }
        while(true) {
            switch (currentState) {
                //This state handles waiting for a floor request
                case WAITING_FOR_REQUEST -> {
                    getDataFromFloor();
                    if (currentDataPacket != null) {
                        //If there is a request, we receive it and change states to FLOOR_REQUEST_RECEIVED
                        currentState = SchedulerState.FLOOR_REQUEST_RECEIVED;
                        //System.out.println("Received from floor");
                    }
                }
                case FLOOR_REQUEST_RECEIVED -> {

                    // Send the floor request to the elevator
                    if(elevatorData.isEmpty()){
                        for (int i = 0 ; i < numElevators; i++){
                            getDataFromElevator();
                        }
                    } else {
                        getDataFromElevator();
                    }
                    int id = pickElevator(initialFloor);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sendDataToElevator(currentDataPacket.toString(), id );
                    hasReachedInitialFloor = false;
                    currentState = SchedulerState.SENDING_REQUEST_TO_ELEVATOR;
                    // System.out.println("Sending floor request to elevator, initial floor: " + initalFloor + "target floor: " + targetFloor);
                }

                case SENDING_REQUEST_TO_ELEVATOR -> {

                    //Here the scheduler waits for a response from the elevator that it reached a floor
                    //System.out.println("Waiting for elevator response");
                    currentState = SchedulerState.WAITING_FOR_ELEVATOR_RESPONSE;
                }
                case WAITING_FOR_ELEVATOR_RESPONSE -> {
                    //Here the scheduler receives the response from the elevator that it reached a floor
                    boolean isIdle = getDataFromElevator();
                    //System.out.println("Response received elevator reached floor " + currentFloor);
                    if (isIdle){
                        currentState = SchedulerState.PROCESSING_ELEVATOR_RESPONSE;
                    } else {
                        currentState = SchedulerState.SENDING_REQUEST_TO_ELEVATOR;
                    }
//                    if (currentFloor == initialFloor){
//                        hasReachedInitialFloor = true;
//                    }
//                    if (!Objects.equals(currentDataPacket.getFloor(), currentDataPacket.getCarButton())) {
//                        //Handles the direction
//                        if (direction.equals("Up")) {
//                            currentFloor++;
//                        } else {
//                            currentFloor--;
//                        }
//                        //If there is more floors to move to, it changes states to SEND_REQUEST_TO_ELEVATOR
//
//                    } else if (hasReachedInitialFloor) {
//                        //Else we have reached the target floor
//                        getDataFromElevator();
//                        currentState = SchedulerState.PROCESSING_ELEVATOR_RESPONSE;
//                    }
                }
                case PROCESSING_ELEVATOR_RESPONSE -> {
                    //Elevator is now idle, the scheduler is now idle as well, it sends the data to the floor
                    // Changes states to WAITING_FOR_REQUEST
//                    sendDataToFloor();
                    getDataFromFloor();
                    sendDataToFloor(currentDataPacket.toString());
                    currentState = SchedulerState.WAITING_FOR_REQUEST;
                }
            }
            if (stopAfterOneCycle) {
                break; // Exit the loop after one cycle for testing purposes
            }
        }
    }

    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler(1);
        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.start();
    }
}
