public class Scheduler implements Runnable {

    private final MainSystem mainSystem;
    private DataPacket currentDataPacket; // this is the data packet that the scheduler is currently working on

    public Scheduler(MainSystem mainSystem) {
        this.mainSystem = mainSystem;
    }

    /**
     * Gets the data from the floor and sets the currentDataPacket
     */
    public void getDataFromFloor () {
        currentDataPacket = mainSystem.getSchedulerAndFloorData();
        System.out.println("Scheduler received: " + currentDataPacket.getTime() + " " + currentDataPacket.getFloor() + " " + currentDataPacket.getDirection() + " " + currentDataPacket.getCarButton());
    }

    /**
     * Sends the data to the elevator
     */
    public void sendDataToElevator (){
        System.out.println("Sending Data to elevator from scheduler: " + currentDataPacket.getTime() + " " + currentDataPacket.getFloor() + " " + currentDataPacket.getDirection() + " " + currentDataPacket.getCarButton());
        mainSystem.updateSchedulerAndElevatorData(currentDataPacket);
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted: " + e.getMessage());
        }
    }

    /**
     * Gets the data from the elevator and sets the currentDataPacket
     */
    public void getDataFromElevator(){
        currentDataPacket = mainSystem.getSchedulerAndElevatorData();
        System.out.println("Scheduler received: " + currentDataPacket.getTime() + " " + currentDataPacket.getFloor() + " " + currentDataPacket.getDirection() + " " + currentDataPacket.getCarButton());
    }

    /**
     * Sends the data to the floor
     */
    public void sendDataToFloor(){
        System.out.println("Sending Data to floor from scheduler: " + currentDataPacket.getTime() + " " + currentDataPacket.getFloor() + " " + currentDataPacket.getDirection() + " " + currentDataPacket.getCarButton());
        mainSystem.updateSchedulerAndFloorData(currentDataPacket);
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while(true){
            getDataFromFloor();
            sendDataToElevator();
            getDataFromElevator();
            sendDataToFloor();
        }
    }
}
