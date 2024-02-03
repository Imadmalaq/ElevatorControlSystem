import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainSystemTest {

    @Test
    void testUpdateAndGetSchedulerAndFloorData() throws InterruptedException {
        MainSystem mainSystem = new MainSystem();
        DataPacket packet = new DataPacket("10:00", "5", "UP", "3");

        // Update data in a separate thread to simulate concurrency
        new Thread(() -> mainSystem.updateSchedulerAndFloorData(packet)).start();

        // Ensure the packet can be retrieved
        DataPacket retrievedPacket = mainSystem.getSchedulerAndFloorData();
        assertEquals(packet, retrievedPacket);
    }

    @Test
    void testUpdateAndGetSchedulerAndElevatorData() throws InterruptedException {
        MainSystem mainSystem = new MainSystem();
        DataPacket packet = new DataPacket("10:15", "3", "DOWN", "1");

        // Update data in a separate thread to simulate concurrency
        new Thread(() -> mainSystem.updateSchedulerAndElevatorData(packet)).start();

        // Ensure the packet can be retrieved
        DataPacket retrievedPacket = mainSystem.getSchedulerAndElevatorData();
        assertEquals(packet, retrievedPacket);
    }
}
