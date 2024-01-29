public class Elevator implements Runnable {

    private final MainSystem mainSystem;
    private DataPacket currentDataPacket;

    public Elevator(MainSystem mainSystem) {
        this.mainSystem = mainSystem;
    }

    /**
     * Gets the data from the scheduler and sets the currentDataPacket
     */
    public void getDataFromScheduler(){
        currentDataPacket = mainSystem.getSchedulerAndElevatorData();
        System.out.println("Elevator received: " + currentDataPacket.getTime() + " " + currentDataPacket.getFloor() + " " + currentDataPacket.getDirection() + " " + currentDataPacket.getCarButton());
    }

    /**
     * Sends the data to the scheduler
     * @param packet - DataPacket object
     */
    public void sendDataToScheduler(DataPacket packet){
        mainSystem.updateSchedulerAndElevatorData(packet);
        System.out.println("Sending Data to scheduler from elevator: " + packet.getTime() + " " + packet.getFloor() + " " + packet.getDirection() + " " + packet.getCarButton());
    }

    @Override
    public void run() {
        getDataFromScheduler();
        sendDataToScheduler(currentDataPacket);

    }
}
