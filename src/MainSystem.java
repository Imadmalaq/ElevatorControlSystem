import java.io.IOException;
import java.net.*;

public class MainSystem {

	 private DataPacket schedulerAndFloorData = null;
	 private DataPacket schedulerAndElevatorData = null;

	 public static int Scheduler_Floor_Port_Number = 100;
	 public static int Scheduler_Elevator_Port_Number = 68;
	 public static int Elevator_Port_Number = 96;
	 public static int Floor_Port_Number = 420;
	 public static InetAddress address;

	 static {
		  try {
				address = InetAddress.getLocalHost();
		  } catch (UnknownHostException e) {
				throw new RuntimeException(e);
		  }
	 }

	 public static int buffer_size = 100;

	 /**
	  * Method to allow scheduler and elevator to get their data packet
	  *
	  * @return DataPacket object
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

//				try {
//					 Thread.sleep(500);
//				} catch (InterruptedException e) {
//					 throw new RuntimeException(e);
//				}


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

	 public synchronized static void sendDataToSchedulerFromElevator(DatagramPacket request, DatagramSocket socket, int eleavtorId) {
		  try {
				System.out.println("Elevator " + eleavtorId + " sending get request to Scheduler on port " + request.getPort() +
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

	 public static void waitForAck(DatagramSocket socket) {
		  DatagramPacket receivePacket = new DatagramPacket(new byte[MainSystem.buffer_size], MainSystem.buffer_size);
		  try {
				socket.receive(receivePacket);
				MainSystem.printReceivePacketData(receivePacket);
		  } catch (IOException e) {
				throw new RuntimeException(e);
		  }

	 }

	 public synchronized void setSchedulerAndElevatorData(DataPacket packet) {
		  this.schedulerAndElevatorData = packet;
		  // Notify any waiting threads that new data is available.
		  notifyAll();
	 }

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

	 public synchronized static void printReceivePacketData(DatagramPacket packet) {
		  // Process the received datagram.
		  System.out.println("Packet received:");
		  System.out.println("From host: " + packet.getAddress());
		  System.out.println("Host port: " + packet.getPort());
		  int len = packet.getLength();
		  System.out.println("Length: " + len);
		  printPacketData(packet);
	 }

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