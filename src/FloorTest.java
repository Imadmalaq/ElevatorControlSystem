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

@ExtendWith(MockitoExtension.class)
class FloorTest {

    @Mock
    private DatagramSocket mockSocket;

    @InjectMocks
    private Floor floor;

    @BeforeEach
    void setUp() {
        floor = new Floor(); // Floor's constructor might need adjusting depending on your setup.
    }

    @Test
    void processInputData_ValidInput() {
        String inputData = "10:00 5 UP 3";
        DataPacket expected = new DataPacket("10:00", "5", "UP", "3");

        DataPacket result = Floor.processStringIntoDataPacket(inputData);

        assertNotNull(result);
        assertEquals(expected.getTime(), result.getTime());
        assertEquals(expected.getFloor(), result.getFloor());
        assertEquals(expected.getDirection(), result.getDirection());
        assertEquals(expected.getCarButton(), result.getCarButton());
    }

    @Test
    void processInputData_InvalidInput() {
        String inputData = "Invalid data";
        DataPacket result = Floor.processStringIntoDataPacket(inputData);
        assertNull(result);
    }

    @Test
    void handleDataPacket_ValidData() {
        String inputData = "10:00 5 UP 3";
        DataPacket expected = new DataPacket("10:00", "5", "UP", "3");
        DataPacket result = Floor.processStringIntoDataPacket(inputData);
        assertEquals(expected,result);
    }

}
