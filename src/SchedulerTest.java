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

    //A simple test that create a packet and checks that the elevators data is correct with it
    @Test
    void getDataFromElevator_simplified() {
        DataPacket expectedPacket = new DataPacket("10:30", "2", "UP", "4", "NF");
        scheduler.setDataPacket(expectedPacket);
        DataPacket resultPacket = scheduler.getCurrentDataPacket();
        assertEquals(expectedPacket, resultPacket, "The data packet should match the expected packet.");
    }


    //Test to ensure that data can be sent correctly to the elevator
    @Test
    void sendDataToElevator() {
        DataPacket packetToSend = new DataPacket("11:00", "1", "UP", "3", "NF");
        scheduler.setDataPacket(packetToSend);
        scheduler.sendDataToElevator(packetToSend.toString(), 0);
    }

    //test for getting data from the floor class using mock packets behaviour
    @Test
    void getDataFromFloor_simplified() {
        DataPacket expectedPacket = new DataPacket("09:00", "1", "DOWN", "0", "NF");
        scheduler.setDataPacket(expectedPacket);
        DataPacket resultPacket = scheduler.getCurrentDataPacket();
        assertEquals(expectedPacket, resultPacket, "The data packet should match the expected packet.");
    }


    //testing that the data can be sent properly
    @Test
    void sendDataToFloor() {
        DataPacket packetToSend = new DataPacket("12:00", "5", "DOWN", "1", "NF");
        scheduler.sendDataToFloor(packetToSend.toString());
    }
}
