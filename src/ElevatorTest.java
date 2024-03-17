import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ElevatorTest {

    private Elevator elevator;

    @BeforeEach
    void setUp() {
        elevator = new Elevator();
        elevator.enableTestMode();
    }

    @Test
    void testInitialState() {
        assertEquals(Elevator.ElevatorState.IDLE, elevator.getCurrentState(), "Elevator should start in IDLE state.");
    }

    @Test
    void testMoveElevatorToTargetFloor() {

        elevator.setCurrentFloor(2);
        elevator.setTargetFloor(5);
        elevator.setCurrentState(Elevator.ElevatorState.MOVING);
        elevator.run();

        assertEquals(2, elevator.getCurrentFloor(), "Elevator should be at target floor after moving.");
        assertEquals(Elevator.ElevatorState.MOVING, elevator.getCurrentState(), "Elevator should enter NOTIFY_SCHEDULER state after reaching the target floor.");
    }

    @Test
    void testHandleDataPacket() {
        DataPacket requestPacket = new DataPacket("12:00", "2", "Up", "5");
        elevator.setCurrentDataPacket(requestPacket);
        elevator.run();

        assertEquals(requestPacket, elevator.getCurrentDataPacket(), "Elevator should have the expected data packet after handling.");
    }

}
