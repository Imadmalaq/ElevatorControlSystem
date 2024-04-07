import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

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
    private HashMap<Integer, String> elevatorFaults = new HashMap<>(); // Track faults reported by elevators

    private int numElevators;
    boolean hasReachedInitialFloor = false;
    boolean isCriticalFault = false;
    private boolean verbose = false;

    public Interface elevatorSystemInterface = Interface.getInstance();

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }


    public enum SchedulerState {
        WAITING_FOR_REQUEST, FLOOR_REQUEST_RECEIVED, SENDING_REQUEST_TO_ELEVATOR, WAITING_FOR_ELEVATOR_RESPONSE, PROCESSING_ELEVATOR_RESPONSE, HANDLE_CRITICAL_FAULT
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
        if (verbose){
            System.out.println("Getting Data from Elevator\n");
        }
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

        if (verbose){

            MainSystem.printReceivePacketData(packet);
        }
        // Check if elevator is sending a data packet
        String packetData = new String(packet.getData(),0, packet.getLength());
        int id = Integer.parseInt(packetData.substring(0,1));
        packetData = packetData.substring(1);


        ElevatorDataPacket eData = new ElevatorDataPacket(0, MainSystem.Elevator_Port_Number + id, id);

        if (Floor.isValidDataPacket(packetData)){
            currentDataPacket = Floor.processStringIntoDataPacket(packetData);
            eData.setCurrentFloor(Integer.parseInt(currentDataPacket.getFloor()));
            elevatorSystemInterface.addText("Received data that elevator " + id + " is at current floor " + eData.getCurrentFloor());
            if (initialFloor == eData.getCurrentFloor()){
                elevatorSystemInterface.addText("Elevator " + id + " Reached initial floor Opening Door");
                elevatorSystemInterface.addText("Elevator " + id + " Closing Door");

            }
            if (verbose){
                System.out.println("Sending ACK to elevator\n");
            }
            MainSystem.sendAcknowledgment(packet);

            if (!"NF".equals(currentDataPacket.getFaultType())) {
                handleElevatorFault(id, currentDataPacket.getFaultType());
                return false; // Returning false since the elevator might not be considered 'idle' in case of a fault
            }
        } else if (packetData.equals("Elevator is now idle")) {
            if (verbose) {
                System.out.println("Sending ACK to elevator\n");
            }
            elevatorSystemInterface.addText("Elevator " + id + " Reached target floor Opening Door");
            elevatorSystemInterface.addText("Elevator " + id + " Closing Door");
            elevatorSystemInterface.addText("<font color='green'>Elevator " + id + " has reached its destination of floor "
                    + targetFloor + " and is now idle </font> <br>");
            eData.setCurrentFloor(elevatorData.get(id).getCurrentFloor());
            MainSystem.sendAcknowledgment(packet);
            isIdle = true;
        } else {
            if (elevatorData.get(id) != null){
                eData.setCurrentFloor(elevatorData.get(id).getCurrentFloor());
            }
            if (!"NF".equals(currentDataPacket.getFaultType()) && !packetData.equals(" Get Request Elevator")) {
                handleElevatorFault(id, currentDataPacket.getFaultType());
            }
            if ("Transient Fault Detected".equals(packetData)){
                MainSystem.sendAcknowledgment(packet);
            } else if ("Fault Fixed".equals(packetData)){
                elevatorSystemInterface.addText("<font color='green'>Elevator " + id + " has fixed the fault and is now operational </font> <br>");
                MainSystem.sendAcknowledgment(packet);
            }


        }

        if(!isCriticalFault){
            elevatorData.put(id, eData);
        }
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
            if (verbose){
                MainSystem.printSendPacketData(elevatorPacket);
            }

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

        if (verbose){
            MainSystem.printReceivePacketData(packet);
        }
        floorSocket.close();
        if (!Floor.isValidDataPacket(new String(packet.getData(),0, packet.getLength()))){ //Get Request
            return;
        }
        parseFloorData(new String(packet.getData(),0, packet.getLength()));

        elevatorSystemInterface.addText("Received request from floor " + initialFloor + " to floor " + targetFloor + " in direction " + direction + "\n");


        if (verbose){
            System.out.println("Sending ACK to floor\n");
        }
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
        if (verbose){
            MainSystem.printSendPacketData(floorPacket);
        }
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

        if (elevatorData.isEmpty()) {
            System.out.println("No elevators available to pick.");
            return -1; // Indicative value for no elevator available
        }

         ElevatorDataPacket closestElevator = values.get(0);
         int closestDistance = Math.abs(startingFloor - closestElevator.getCurrentFloor());

         for (ElevatorDataPacket e : values) {
            int distance = Math.abs(startingFloor - e.getCurrentFloor());
            if (distance < closestDistance){
              closestElevator = e;
              closestDistance = distance;
           }
         }
         String outputData = "Picking Elevator ID: " + closestElevator.getId() + " currently on floor " +
                 closestElevator.getCurrentFloor() + " to initial floor " + startingFloor + " with target floor " + targetFloor + "\n";
         System.out.println(outputData);
         elevatorSystemInterface.addText(outputData);
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
                    int elevatorId = pickElevator(initialFloor);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if (elevatorId == -1) {
                        System.out.println("Currently, no elevators are working Shutting down...\n");
                        System.exit(0);
                        // Here, you might want to implement logic to queue the request or retry after some time
                    } else {
                        // If an elevator is successfully picked, send the request to that elevator
                        sendDataToElevator(currentDataPacket.toString(), elevatorId);
                        hasReachedInitialFloor = false; // Resetting the state for the next operation
                        currentState = SchedulerState.SENDING_REQUEST_TO_ELEVATOR;
                    }
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

                    if (isCriticalFault) {
                        isCriticalFault = false;
                        currentState = SchedulerState.HANDLE_CRITICAL_FAULT;
                    }
                }
                case PROCESSING_ELEVATOR_RESPONSE -> {
                    //Elevator is now idle, the scheduler is now idle as well, it sends the data to the floor
                    // Changes states to WAITING_FOR_REQUEST
//                    sendDataToFloor();
                    getDataFromFloor();
                    sendDataToFloor(currentDataPacket.toString());
                    currentState = SchedulerState.WAITING_FOR_REQUEST;
                }

                case HANDLE_CRITICAL_FAULT -> {

                    int elevatorId = pickElevator(initialFloor);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if (elevatorId == -1) {
                        String outputText = "Currently, all elevators are under maintenance no one to handle the request.";
                        System.out.println(outputText);
                        elevatorSystemInterface.addText(outputText);
                        // Here, you might want to implement logic to queue the request or retry after some time
                    } else {
                        // If an elevator is successfully picked, send the request to that elevator
                        currentDataPacket = new DataPacket(currentDataPacket.getTime(), currentDataPacket.getFloor(), currentDataPacket.getDirection(), currentDataPacket.getCarButton(), "NF");
                        sendDataToElevator(currentDataPacket.toString(), elevatorId);
                        hasReachedInitialFloor = false; // Resetting the state for the next operation
                        currentState = SchedulerState.SENDING_REQUEST_TO_ELEVATOR;
                    }

                }
            }
            if (stopAfterOneCycle) {
                break; // Exit the loop after one cycle for testing purposes
            }
        }
    }

    private void handleElevatorFault(int elevatorId, String faultType) {
        // This only Log the fault for now
        String outputData = "Elevator " + elevatorId + " reported a fault: " + faultType + "\n";
        System.out.println(outputData);
        elevatorSystemInterface.addText("<font color='red'>" + outputData +" </font>");


        switch (faultType) {
            case "FT": // Floor Timer fault
                outputData = "Critical: Elevator " + elevatorId + " is stuck or experiencing significant delays.\n";
                System.out.println(outputData);
                elevatorSystemInterface.addText("<font color='red'>" + outputData + "</font><br>");
                isCriticalFault = true;
                elevatorData.remove(elevatorId);
                // Here we can deactivate the elevator in the system until maintenance has resolved the issue
                break;
            case "DOF": // Door Open Fault
                outputData = "Warning: Elevator " + elevatorId + "'s door is stuck open.";
                System.out.println(outputData);
                elevatorSystemInterface.addText("<font color='#ffcc00'>" + outputData + "</font>");
                currentDataPacket = new DataPacket(currentDataPacket.getTime(), currentDataPacket.getFloor(), currentDataPacket.getDirection(),
                    currentDataPacket.getCarButton(), "NF");
                // Same here. We can deactivate the elevator in the system
                break;
            default:
                System.out.println("Elevator " + elevatorId + " reported an unknown fault type.");
                break;
        }

        // For simplicity, we decided to just mark the elevator as having a fault.
        elevatorFaults.put(elevatorId, faultType);
    }

    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler(4);
        scheduler.setVerbose(true);
        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.start();
    }
}
