import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SchedulerTest {

    @Mock
    private MainSystem mainSystem; // Mock the MainSystem

    @InjectMocks
    private Scheduler scheduler; // Injects mocked MainSystem into Scheduler

    @BeforeEach
    void setUp() {
    }

    @Test
    void getDataFromElevator_simplified() {
        DataPacket expectedPacket = new DataPacket("10:30", "2", "UP", "4");
        scheduler.setDataPacket(expectedPacket);
        DataPacket resultPacket = scheduler.getCurrentDataPacket();
        assertEquals(expectedPacket, resultPacket, "The data packet should match the expected packet.");
    }


    @Test
    void sendDataToElevator() {
        DataPacket packetToSend = new DataPacket("11:00", "1", "UP", "3");
        scheduler.setDataPacket(packetToSend);
        scheduler.sendDataToElevator(packetToSend.toString());
    }

    @Test
    void getDataFromFloor_simplified() {
        DataPacket expectedPacket = new DataPacket("09:00", "1", "DOWN", "0");
        scheduler.setDataPacket(expectedPacket);
        DataPacket resultPacket = scheduler.getCurrentDataPacket();
        assertEquals(expectedPacket, resultPacket, "The data packet should match the expected packet.");
    }


    @Test
    void sendDataToFloor() {
        DataPacket packetToSend = new DataPacket("12:00", "5", "DOWN", "1");
        scheduler.sendDataToFloor(packetToSend.toString());
    }
}
