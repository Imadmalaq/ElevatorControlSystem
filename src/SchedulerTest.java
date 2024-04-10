import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Scheduler class.
 * Uses Mockito to mock dependencies and ensure that the Scheduler class functions as expected
 * under various scenarios, especially focusing on interactions with the MainSystem and handling
 * of DataPacket objects.
 *
 * @version 1.0
 * @since 2024-04-10
 * @author Humam Khalil
 * @author Imad Mohamed
 * @author Michael Rochefort
 * @author Kieran Rourke
 * @author Kyle Taticek
 */
@ExtendWith(MockitoExtension.class)
class SchedulerTest {

    private Scheduler scheduler; // Changed from @InjectMocks to regular declaration

    @Mock
    private MainSystem mainSystem;

    /**
     * Set up common test objects and configurations.
     * Here, we instantiate a Scheduler with a preset number of elevators for all tests.
     */
    @BeforeEach
    void setUp() {
        // Manually create an instance of Scheduler with a mock or preset number of elevators
        scheduler = new Scheduler(3);

    }

    /**
     * Test to verify that the scheduler correctly receives data from an elevator.
     * This test simulates the reception of a DataPacket from an elevator and checks
     * that the scheduler's current data packet matches the expected packet.
     */
    @Test
    void getDataFromElevator_simplified() {
        DataPacket expectedPacket = new DataPacket("10:30", "2", "UP", "4", "NF");
        scheduler.setDataPacket(expectedPacket);
        DataPacket resultPacket = scheduler.getCurrentDataPacket();
        assertEquals(expectedPacket, resultPacket, "The data packet should match the expected packet.");
    }

    /**
     * Test to ensure that the scheduler can send data correctly to an elevator.
     * This test simulates sending a DataPacket to an elevator, focusing on the method's ability
     * to execute without errors.
     */
    @Test
    void sendDataToElevator() {
        DataPacket packetToSend = new DataPacket("11:00", "1", "UP", "3", "NF");
        scheduler.setDataPacket(packetToSend);
        scheduler.sendDataToElevator(packetToSend.toString(), 0);

    }

    /**
     * Test to verify that the scheduler correctly receives data from the floor.
     * This test simulates the reception of a DataPacket from a floor and checks
     * that the scheduler's current data packet matches the expected packet.
     */
    @Test
    void getDataFromFloor_simplified() {
        DataPacket expectedPacket = new DataPacket("09:00", "1", "DOWN", "0", "NF");
        scheduler.setDataPacket(expectedPacket);
        DataPacket resultPacket = scheduler.getCurrentDataPacket();
        assertEquals(expectedPacket, resultPacket, "The data packet should match the expected packet.");
    }

    /**
     * Test to ensure that the scheduler can send data correctly to the floor.
     * This test simulates sending a DataPacket to the floor, focusing on the method's ability
     * to execute without errors.
     */
    @Test
    void sendDataToFloor() {
        DataPacket packetToSend = new DataPacket("12:00", "5", "DOWN", "1", "NF");
        scheduler.sendDataToFloor(packetToSend.toString());
    }
}
