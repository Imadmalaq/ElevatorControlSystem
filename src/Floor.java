import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Floor implements Runnable{

    private final MainSystem mainSystem;
    private DataPacket currentDataPacket;

    public Floor(MainSystem mainSystem) {
        this.mainSystem = mainSystem;
    }

    /**
     * Reads the input File and sets the currentDataPacket
     */
    public void readInputFile () {
        try {
            File myObj = new File("input.txt");
            Scanner myReader = new Scanner(myObj);
            DataPacket dataPacket = null;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                currentDataPacket = processInputData(data);
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
        currentDataPacket = mainSystem.getSchedulerAndFloorData();
        System.out.println("Floor received: " + currentDataPacket.getTime() + " " + currentDataPacket.getFloor() + " " + currentDataPacket.getDirection() + " " + currentDataPacket.getCarButton());
    }

    @Override
    public void run() {
        readInputFile();
        if (currentDataPacket != null){
            sendDataToScheduler(currentDataPacket);
            try{
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.print(e);
            }
            receiveDataFromScheduler();
        }
    }
}
