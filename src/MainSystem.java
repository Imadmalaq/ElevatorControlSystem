public class MainSystem {

    private DataPacket schedulerAndFloorData = null;
    private DataPacket schedulerAndElevatorData = null;

    /**
     * Method to allow floor and scheduler to update their data packet
     * @param packet - DataPacket object
     */
    public synchronized void updateSchedulerAndFloorData(DataPacket packet){
        while(schedulerAndFloorData != null) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.print(e);
            }
        }

        schedulerAndFloorData = packet;
        notifyAll();
    }

    /**
     * Method to allow floor and scheduler to get their data packet
     * @return DataPacket object
     */
    public synchronized DataPacket getSchedulerAndFloorData(){
        while(schedulerAndFloorData == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.print(e);
            }
        }
        DataPacket finalPacket = schedulerAndFloorData;
        schedulerAndFloorData = null;
        notifyAll();
        return finalPacket;
    }


    /**
     * Method to allow scheduler and elevator to update their data packet
     * @param packet - DataPacket object
     */
    public synchronized void updateSchedulerAndElevatorData(DataPacket packet) {
        while (schedulerAndElevatorData != null) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.print(e);
            }
        }
        schedulerAndElevatorData = packet;
        notifyAll();
    }

    /**
     * Method to allow scheduler and elevator to get their data packet
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


    public static void main(String[] args) {
        MainSystem mainSystem = new MainSystem();
        Thread floor = new Thread(new Floor(mainSystem), "Floor");
        Thread elevator = new Thread(new Elevator(mainSystem), "Elevator");
        Thread scheduler = new Thread(new Scheduler(mainSystem), "Scheduler");


        floor.start();
        scheduler.start();
        elevator.start();
    }
}
