import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.DatagramSocket;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Floor class.
 * These tests focus on ensuring that the Floor class correctly processes input data and handles data packets.
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
class FloorTest {

    @Mock
    private DatagramSocket mockSocket;

    @InjectMocks
    private Floor floor;

    @BeforeEach
    void setUp() {
        floor = new Floor();
    }

    /**
     * Test to verify that valid input data is processed correctly into a DataPacket object.
     * This test ensures that the processStringIntoDataPacket method in the Floor class correctly parses
     * valid input data and constructs a DataPacket object with the expected attributes.
     */
    @Test
    void processInputData_ValidInput() {
        String inputData = "10:00 5 UP 3 NF";
        DataPacket expected = new DataPacket("10:00", "5", "UP", "3", "NF");

        DataPacket result = Floor.processStringIntoDataPacket(inputData);

        assertNotNull(result);
        assertEquals(expected.getTime(), result.getTime());
        assertEquals(expected.getFloor(), result.getFloor());
        assertEquals(expected.getDirection(), result.getDirection());
        assertEquals(expected.getCarButton(), result.getCarButton());
    }

    /**
     * Test to verify that invalid input data is processed correctly into a null DataPacket object.
     * This test ensures that the processStringIntoDataPacket method in the Floor class returns null
     * when the input data is invalid and cannot be parsed into a DataPacket object.
     */
    @Test
    void processInputData_InvalidInput() {
        String inputData = "Invalid data";
        DataPacket result = Floor.processStringIntoDataPacket(inputData);
        assertNull(result);
    }

    /**
     * Test to verify that a valid data packet is correctly handled by the Floor class.
     * This test ensures that the Floor class processes a valid data packet and correctly
     * constructs it, validating that the actual result matches the expected result.
     */
    @Test
    void handleDataPacket_ValidData() {
        String inputData = "10:00 5 UP 3 NF";
        DataPacket expected = new DataPacket("10:00", "5", "UP", "3", "NF");
        DataPacket result = Floor.processStringIntoDataPacket(inputData);
        assertEquals(expected,result);
    }

}
