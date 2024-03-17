import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElevatorTest {

    @Mock
    private MainSystem mainSystem; //MainSystem mock to simulate its behavior

    @InjectMocks
    private Elevator elevator; //injects mainSystem mock into the elevator

    @BeforeEach
    void setUp() {
    }

    @Test
    void getDataFromScheduler() {
        DataPacket expectedPacket = new DataPacket("10:00", "5", "UP", "3");
        when(mainSystem.getSchedulerAndElevatorData()).thenReturn(expectedPacket);


        elevator.getDataFromScheduler();

        // Assert
        verify(mainSystem).getSchedulerAndElevatorData(); // Verify interaction with mainSystem

    }

    @Test
    void sendDataToScheduler() {
        DataPacket packetToSend = new DataPacket("10:15", "3", "DOWN", "1");


//        elevator.sendDataToScheduler(packetToSend);

        // Assert
        verify(mainSystem).updateSchedulerAndElevatorData(packetToSend);

    }
}