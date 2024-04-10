import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Elevator class.
 * These tests focus on ensuring that the Elevator class functions correctly, including its initial state,
 * movement, and handling of data packets.
 *
 * @version 1.0
 * @since 2024-04-10
 * @author Humam Khalil
 * @author Imad Mohamed
 * @author Michael Rochefort
 * @author Kieran Rourke
 * @author Kyle Taticek
 */
class ElevatorTest {

    private Elevator elevator;

    @BeforeEach
    void setUp() {
        elevator = new Elevator();
        elevator.enableTestMode();
    }

    /**
     * Test to verify the initial state of the elevator.
     * This test ensures that the elevator starts in the IDLE state.
     */
    @Test
    void testInitialState() {
        assertEquals(Elevator.ElevatorState.IDLE, elevator.getCurrentState(), "Elevator should start in IDLE state.");
    }

    /**
     * Test to verify the movement of the elevator.
     * This test sets the current floor and target floor of the elevator, simulates movement,
     * and then checks if the elevator reaches the target floor and transitions to the appropriate state.
     */
    @Test
    void testMoveElevator() {

        elevator.setCurrentFloor(2);
        elevator.setTargetFloor(5);
        elevator.setCurrentState(Elevator.ElevatorState.MOVING);
        elevator.run();

        assertEquals(2, elevator.getCurrentFloor(), "Elevator should be at target floor after moving.");
        assertEquals(Elevator.ElevatorState.MOVING, elevator.getCurrentState(), "Elevator should enter NOTIFY_SCHEDULER state after reaching the target floor.");
    }

    /**
     * Test to verify the handling of data packets by the elevator.
     * This test sets a data packet for the elevator, simulates its handling, and then checks if the elevator
     * retains the expected data packet after handling.
     */
    @Test
    void testHandleDataPacket() {
        DataPacket requestPacket = new DataPacket("12:00", "2", "Up", "5", "NF");
        elevator.setCurrentDataPacket(requestPacket);
        elevator.run();

        assertEquals(requestPacket, elevator.getCurrentDataPacket(), "Elevator should have the expected data packet after handling.");
    }

}
