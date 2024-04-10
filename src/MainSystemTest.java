import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MainSystem class.
 * These tests focus on ensuring that the MainSystem class correctly handles the storage and retrieval
 * of scheduler and elevator data packets.
 *
 * @version 1.0
 * @since 2024-04-10
 * @author Humam Khalil
 * @author Imad Mohamed
 * @author Michael Rochefort
 * @author Kieran Rourke
 * @author Kyle Taticek
 */
class MainSystemTest {

    private MainSystem mainSystem;

    @BeforeEach
    void setUp() {
        mainSystem = new MainSystem();
    }

    /**
     * Test to verify the correctness of setting and getting scheduler and elevator data packets.
     * This test ensures that the MainSystem correctly stores a DataPacket and retrieves it back
     * when requested.
     */
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
