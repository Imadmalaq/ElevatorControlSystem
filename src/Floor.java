import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Floor implements Runnable{

    private final MainSystem mainSystem;

    public Floor(MainSystem mainSystem) {
        this.mainSystem = mainSystem;
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
                DataPacket dataPacket = processInputData(data);
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
    public DataPacket processInputData (String data){
        String[] parts = data.split(" ");
        if(parts.length != 4){
            System.out.println("Invalid input");
            return null;
        }

        String time = parts[0];
        String floor = parts[1];
        String direction = parts[2];
        String carButton = parts[3];

       return new DataPacket(time, floor, direction, carButton);
    }

    /**
     * Sends the data to the scheduler
     * @param packet - DataPacket object
     */
    public void sendDataToScheduler (DataPacket packet){
        System.out.println("Sending Data to scheduler from floor: " + packet.getTime() + " " + packet.getFloor() + " " + packet.getDirection() + " " + packet.getCarButton());
        mainSystem.updateSchedulerAndFloorData(packet);
    }

    /**
     * Receives the data from the scheduler and sets the currentDataPacket
     */
    public void receiveDataFromScheduler(){
        DataPacket data = mainSystem.getSchedulerAndFloorData();
        System.out.println("Floor received: " + data.getTime() + " " + data.getFloor() + " " + data.getDirection() + " " + data.getCarButton()+"\n\n");
    }

    public void handleDataPacket(DataPacket dataPacket) {
        sendDataToScheduler(dataPacket);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted: " + e.getMessage());
        }
        receiveDataFromScheduler();
    }

    @Override
    public void run() {
        readInputFile();
        System.exit(0);
    }
}
