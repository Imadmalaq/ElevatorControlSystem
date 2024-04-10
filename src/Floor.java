import com.sun.tools.javac.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * The Floor class simulates the floor operations in an elevator system.
 * It reads floor requests from an input file, converts each line of input into a data packet,
 * and communicates with the scheduler to handle these requests.
 *
 * @version 1.0
 * @since 2024-04-10
 * @author Humam Khalil
 * @author Imad Mohamed
 * @author Michael Rochefort
 * @author Kieran Rourke
 * @author Kyle Taticek
 */
public class Floor implements Runnable{

    /**
     * Constructs a new instance of the Floor class.
     */
    public Floor() {
    }

    /**
     * Reads floor request data from an input file and processes each line into a DataPacket.
     * Each DataPacket is then immediately handled by sending it to the scheduler.
     */
    public void readInputFile() {
        try {
            File myObj = new File("input.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                // Process the data immediately upon reading
                DataPacket dataPacket = processStringIntoDataPacket(data);
                if (dataPacket != null) {
                    // Send and process the data packet immediately
                    handleDataPacket(dataPacket);
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Processes a line of input from the file into a DataPacket object if the input is valid.
     *
     * @param data A line of input from the input file representing a floor request.
     * @return A DataPacket object representing the request; null if the input is invalid.
     */
    public static DataPacket processStringIntoDataPacket(String data){
        String[] parts = data.split(" ");
        if(!isValidDataPacket(data)){
            System.out.println("Invalid input");
            System.out.println("Input: " + data);
            return null;
        }

        String time = parts[0];
        String floor = parts[1];
        String direction = parts[2];
        String carButton = parts[3];
        String faultType = parts[4]; // Parse the fault type from the input

        return new DataPacket(time, floor, direction, carButton, faultType);
    }

    /**
     * Validates the input string to ensure it can be correctly parsed into a DataPacket.
     *
     * @param data The input string to validate.
     * @return true if the input can be parsed into a DataPacket; false otherwise.
     */
    public static boolean isValidDataPacket(String data) {
        String[] parts = data.split(" ");
        try {
            int test = Integer.parseInt(parts[1]);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return parts.length == 5;
    }

    /**
     * Handles a valid DataPacket by sending it to the scheduler and awaiting a response.
     *
     * @param dataPacket The DataPacket to handle.
     */
    public void handleDataPacket(DataPacket dataPacket) {
        System.out.println("Handling Data Packet with Fault Type: " + dataPacket.getFaultType());
        DatagramSocket tempSendReceiveSocket = null;
        try {
            tempSendReceiveSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        sendDataToScheduler(dataPacket, tempSendReceiveSocket);
        MainSystem.waitForAck(tempSendReceiveSocket);
        try {
            Thread.sleep(3200);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted: " + e.getMessage());
        }
        getDataFromScheduler();
    }

    /**
     * Sends a DataPacket to the scheduler using a DatagramSocket.
     *
     * @param packet         The DataPacket to send.
     * @param tempSendSocket The DatagramSocket to use for sending the packet.
     */
    public void sendDataToScheduler(DataPacket packet, DatagramSocket tempSendSocket) {
        byte[] message = new byte[MainSystem.buffer_size];
        message = packet.toString().getBytes();
        DatagramPacket dataPacket = new DatagramPacket(message, message.length, MainSystem.address, MainSystem.Scheduler_Floor_Port_Number);

        MainSystem.printSendPacketData(dataPacket);
        try {
            tempSendSocket.send(dataPacket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Requests and receives data from the scheduler, typically to confirm that a request has been processed.
     */
    public void getDataFromScheduler () {
        try {
            // Prepare a buffer to store incoming data
            byte[] sendBuffer = new byte[MainSystem.buffer_size]; // Adjust size as necessary
            byte[] returnBuffer = new byte[MainSystem.buffer_size]; // Adjust size as necessary

            // Create a DatagramPacket for receiving data

            sendBuffer = "Get Request Floor".getBytes();

            DatagramPacket request = new DatagramPacket(sendBuffer, sendBuffer.length, MainSystem.address, MainSystem.Scheduler_Floor_Port_Number);
            DatagramPacket response = new DatagramPacket(returnBuffer, returnBuffer.length, MainSystem.address, MainSystem.Floor_Port_Number);
            MainSystem.rpc_send(request, response, -1);
            MainSystem.printReceivePacketData(response);

            MainSystem.sendAcknowledgment(response);



        } catch (Exception e) {
            System.out.println("Exception in getDataFromScheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * The main run method of the Floor class, starting the process of reading and handling input from the input file.
     */
    @Override
    public void run() {
        readInputFile();
        System.exit(0);
    }

    /**
     * The main entry point for the Floor class, creating a new instance and starting it in a new thread.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        Floor floor = new Floor();
        Thread t = new Thread(floor);
        t.start();
    }
}
