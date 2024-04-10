import java.io.IOException;
import java.net.*;

/**
 * The MainSystem class facilitates communication between the elevator system's components
 * via network sockets. It provides utilities for sending and receiving datagram packets,
 * along with utilities for logging packet data.
 *
 * @version 1.0
 * @since 2024-04-10
 * @author Humam Khalil
 * @author Imad Mohamed
 * @author Michael Rochefort
 * @author Kieran Rourke
 * @author Kyle Taticek
 */
public class MainSystem {

	 private DataPacket schedulerAndFloorData = null;
	 private DataPacket schedulerAndElevatorData = null;

	 public static int Scheduler_Floor_Port_Number = 100;
	 public static int Scheduler_Elevator_Port_Number = 68;
	 public static int Elevator_Port_Number = 96;
	 public static int Floor_Port_Number = 420;
	 public static InetAddress address;
	 public static String RESET = "\u001B[0m";
	 public static String RED_TEXT = "\u001B[31m";
	 public static String GREEN_TEXT = "\u001B[32m";
	 public static String YELLOW_TEXT = "\u001B[33m";

	 static {
		  try {
				address = InetAddress.getLocalHost();
		  } catch (UnknownHostException e) {
				throw new RuntimeException(e);
		  }
	 }

	 public static int buffer_size = 100;

	/**
	 * Waits for and returns the next DataPacket intended for the elevator from the scheduler.
	 * If no packet is available, it waits until one is set.
	 *
	 * @return The next DataPacket from the scheduler intended for the elevator.
	 */
	 public synchronized DataPacket getSchedulerAndElevatorData() {
		  while (schedulerAndElevatorData == null) {
				try {
					 wait();
				} catch (InterruptedException e) {
					 System.out.print(e);
				}
		  }
		  DataPacket finalPacket = schedulerAndElevatorData;
		  schedulerAndElevatorData = null;
		  notifyAll();
		  return finalPacket;
	 }

	/**
	 * Sends a request from one system component to another and waits for a response.
	 * This method encapsulates the process of sending a DatagramPacket and receiving
	 * the response in another DatagramPacket.
	 *
	 * @param request The DatagramPacket containing the request.
	 * @param response The DatagramPacket to store the response.
	 * @param elevatorId The ID of the elevator sending the request, or -1 for requests not from elevators.
	 */
	 public static void rpc_send(DatagramPacket request, DatagramPacket response, int elevatorId) {
		  // Send the datagram packet to the client via the send socket.
		  try {
				DatagramSocket tempSendSocket = new DatagramSocket();
				if (elevatorId != -1) { // Synchronize request to elevator to stop double sending
					 sendDataToSchedulerFromElevator(request, tempSendSocket, elevatorId);
				} else {
					 System.out.println("Sending get request to Scheduler on port " + request.getPort());
					 tempSendSocket.send(request);
				}


				System.out.println("Receiving from host port " + response.getPort() + "\n");
				DatagramSocket tempReceiveSocket = new DatagramSocket(response.getPort());
				tempReceiveSocket.receive(response);

				tempSendSocket.close();
				tempReceiveSocket.close();

				//Get Response
		  } catch (SocketException e) {
				throw new RuntimeException(e);
		  } catch (IOException e) {
				throw new RuntimeException(e);
		  }
	 }

	/**
	 * Sends a DatagramPacket from an elevator to the scheduler.
	 *
	 * @param request The DatagramPacket to send.
	 * @param socket The DatagramSocket to use for sending the packet.
	 * @param elevatorId The ID of the elevator sending the packet.
	 */
	 public synchronized static void sendDataToSchedulerFromElevator(DatagramPacket request, DatagramSocket socket, int elevatorId) {
		  try {
				System.out.println("Elevator " + elevatorId + " sending get request to Scheduler on port " + request.getPort() +
						  "\n");
				socket.send(request);
				try {
					 Thread.sleep(100);
				} catch (InterruptedException e) {
					 throw new RuntimeException(e);
				}
		  } catch (IOException e) {
				throw new RuntimeException(e);
		  }
	 }

	 /**
	  * Sends an acknowledgment packet to the client in response to a received packet.
	  *
	  * @param receivedPacket The DatagramPacket received from the client that triggered the acknowledgment.
	  */
	 public static void sendAcknowledgment(DatagramPacket receivedPacket) {
		  // Extract the content of the received packet
		  String received = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

		  // Construct acknowledgment data including the content of the received packet
		  byte acknowledgmentData[] = ("ACK " + received).getBytes();

		  // Create a DatagramPacket for the acknowledgment
		  DatagramPacket sendPacket = new DatagramPacket(acknowledgmentData, acknowledgmentData.length,
					 receivedPacket.getAddress(), receivedPacket.getPort());

		  // Send the acknowledgment packet
		  DatagramSocket tempSendSocket = null;
		  try {
				tempSendSocket = new DatagramSocket();
		  } catch (SocketException e) {
				throw new RuntimeException(e);
		  }
		  try {
				tempSendSocket.send(sendPacket);
		  } catch (IOException e) {
				e.printStackTrace();
				System.exit(1); // Consider handling this exception more gracefully in a production environment
		  }
		  tempSendSocket.close();

	 }

	/**
	 * Waits for an acknowledgment packet to be received on the provided DatagramSocket.
	 *
	 * @param socket The DatagramSocket to listen on for the acknowledgment packet.
	 */
	 public static void waitForAck(DatagramSocket socket) {
		  DatagramPacket receivePacket = new DatagramPacket(new byte[MainSystem.buffer_size], MainSystem.buffer_size);
		  try {
				socket.receive(receivePacket);
				MainSystem.printReceivePacketData(receivePacket);
		  } catch (IOException e) {
				throw new RuntimeException(e);
		  }

	 }

	/**
	 * Sets the DataPacket intended for the elevator, waking up any threads waiting for this data.
	 *
	 * @param packet The DataPacket to be sent to the elevator.
	 */
	 public synchronized void setSchedulerAndElevatorData(DataPacket packet) {
		  this.schedulerAndElevatorData = packet;
		  // Notify any waiting threads that new data is available.
		  notifyAll();
	 }

	/**
	 * Utility method to print packet data.
	 *
	 * @param packet The DataPacket to be printed.
	 */
	 private synchronized static void printPacketData(DatagramPacket packet) {
		  //Output data
		  System.out.print("Containing... as a string: ");
		  System.out.println(new String(packet.getData(), 0, packet.getLength()));
		  System.out.print("Containing... as bytes: ");
		  for (int i = 0; i < packet.getData().length; i++) {
				System.out.print(packet.getData()[i] + " ");
		  }
		  System.out.println("\n");
	 }

	/**
	 * Utility method to print received packet data.
	 *
	 * @param packet The DataPacket to be printed.
	 */
	 public synchronized static void printReceivePacketData(DatagramPacket packet) {
		  // Process the received datagram.
		  System.out.println("Packet received:");
		  System.out.println("From host: " + packet.getAddress());
		  System.out.println("Host port: " + packet.getPort());
		  int len = packet.getLength();
		  System.out.println("Length: " + len);
		  printPacketData(packet);
	 }

	/**
	 * Utility method to print sent packet data.
	 *
	 * @param packet The DataPacket to be printed.
	 */
	 public synchronized static void printSendPacketData(DatagramPacket packet) {
		  System.out.println("Sending packet:");
		  System.out.println("To host: " + packet.getAddress());
		  System.out.println("Destination host port: " + packet.getPort());
		  int len = packet.getLength();
		  System.out.println("Length: " + len);
		  System.out.println("Current time is " + System.currentTimeMillis() + "\n");
		  printPacketData(packet);
	 }
}