import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainSystemTest {

    private MainSystem mainSystem;

    @BeforeEach
    void setUp() {
        mainSystem = new MainSystem();
    }

    @Test
    void testSetAndGetSchedulerAndElevatorData() {
        // Test both setting and getting scheduler and elevator data to ensure they work as expected.

        // Arrange
        DataPacket expectedPacket = new DataPacket("10:00", "3", "UP", "5", "NF");

        // Act

        mainSystem.setSchedulerAndElevatorData(expectedPacket);
        DataPacket actualPacket = mainSystem.getSchedulerAndElevatorData();

        // Assert
        assertEquals(expectedPacket, actualPacket, "The retrieved data packet should match the one that was set.");
    }

}
