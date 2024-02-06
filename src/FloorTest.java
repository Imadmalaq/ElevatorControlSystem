import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FloorTest {

    @Mock
    private MainSystem mainSystem; // Mock the MainSystem

    private Floor floor;

    @BeforeEach
    void setUp() {
        floor = new Floor(mainSystem);
    }

    @Test
    void processInputData_ValidInput() {
        String inputData = "10:00 5 UP 3";
        DataPacket expected = new DataPacket("10:00", "5", "UP", "3");

        DataPacket result = floor.processInputData(inputData);

        assertNotNull(result);
        assertEquals(expected.getTime(), result.getTime());
        assertEquals(expected.getFloor(), result.getFloor());
        assertEquals(expected.getDirection(), result.getDirection());
        assertEquals(expected.getCarButton(), result.getCarButton());
    }

    @Test
    void processInputData_InvalidInput() {
        String inputData = "Invalid data";
        DataPacket result = floor.processInputData(inputData);

        assertNull(result);
    }

    @Test
    void sendDataToScheduler() {
        DataPacket packet = new DataPacket("10:00", "5", "UP", "3");

        floor.sendDataToScheduler(packet);

        verify(mainSystem).updateSchedulerAndFloorData(packet);
    }

    @Test
    void receiveDataFromScheduler() {
        DataPacket expectedPacket = new DataPacket("10:00", "5", "UP", "3");
        when(mainSystem.getSchedulerAndFloorData()).thenReturn(expectedPacket);

        floor.receiveDataFromScheduler();

        verify(mainSystem).getSchedulerAndFloorData();
    }
}