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
    private Scheduler scheduler; // Injects mocked MainSystem

    @BeforeEach
    void setUp() {
    }

//    @Test
//    void getDataFromFloor() {
//        DataPacket expectedPacket = new DataPacket("10:00", "5", "UP", "3");
//        when(mainSystem.getSchedulerAndFloorData()).thenReturn(expectedPacket);
//
//        scheduler.getDataFromFloor();
//
//        assertNotNull(scheduler.getCurrentDataPacket());
//        assertEquals(expectedPacket, scheduler.getCurrentDataPacket());
//    }

//    @Test
//    void sendDataToElevator() {
//        DataPacket packet = new DataPacket("10:15", "3", "DOWN", "1");
//        // Set the currentDataPacket for the scheduler
//        scheduler.setDataPacket(packet);
//
////        scheduler.sendDataToElevator();
//
//        verify(mainSystem).updateSchedulerAndElevatorData(packet);
//    }

    @Test
    void getDataFromElevator() {
        DataPacket expectedPacket = new DataPacket("10:30", "2", "UP", "4");
        when(mainSystem.getSchedulerAndElevatorData()).thenReturn(expectedPacket);

//        scheduler.getDataFromElevator();

        assertNotNull(scheduler.getCurrentDataPacket());
        assertEquals(expectedPacket, scheduler.getCurrentDataPacket());
    }

//    @Test
//    void sendDataToFloor() {
//        DataPacket packet = new DataPacket("10:45", "1", "DOWN", "G");
//        // Set the currentDataPacket for the scheduler
//        scheduler.setDataPacket(packet);
//
////        scheduler.sendDataToFloor();
//
//        verify(mainSystem).updateSchedulerAndFloorData(packet);
//    }
}