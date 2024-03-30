import com.sun.tools.javac.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

public class Floor implements Runnable{


    public Floor() {
    }

    /**
     * Reads the input File and sets the currentDataPacket
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
     * Processes the input data and creates a DataPacket object
     * @param data - One line from the input file
     * @return DataPacket object
     */
    public static DataPacket processStringIntoDataPacket(String data) {
        String[] parts = data.split(" ");
        if (!isValidDataPacket(data)) {
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

    public static boolean isValidDataPacket(String data) {
        String[] parts = data.split(" ");
        try {
            int test = Integer.parseInt(parts[1]);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return parts.length == 5;
    }

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
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted: " + e.getMessage());
        }
        getDataFromScheduler();
    }

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



    @Override
    public void run() {
        readInputFile();
        System.exit(0);
    }

    public static void main(String[] args) {
        Floor floor = new Floor();
        Thread t = new Thread(floor);
        t.start();
    }
}
