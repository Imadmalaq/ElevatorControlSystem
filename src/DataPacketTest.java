import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the DataPacket class.
 * These tests focus on ensuring that the getter methods of the DataPacket class return the expected values.
 *
 * @version 1.0
 * @since 2024-04-10
 * @author Humam Khalil
 * @author Imad Mohamed
 * @author Michael Rochefort
 * @author Kieran Rourke
 * @author Kyle Taticek
 */
public class DataPacketTest {
    DataPacket testPacket = new DataPacket("Fifteen","Fifteen","up","testButton", "NF");

    /**
     * Test the getTime() method of the DataPacket class.
     * This test verifies that the getTime() method returns the expected time value.
     */
    @org.junit.jupiter.api.Test
    void getTime() {
        assertEquals(testPacket.getTime(),"Fifteen");
    }

    /**
     * Test the getFloor() method of the DataPacket class.
     * This test verifies that the getFloor() method returns the expected floor value.
     */
    @org.junit.jupiter.api.Test
    void getFloor() {
        assertEquals(testPacket.getFloor(),"Fifteen");
    }

    /**
     * Test the getDirection() method of the DataPacket class.
     * This test verifies that the getDirection() method returns the expected direction value.
     */
    @org.junit.jupiter.api.Test
    void getDirection() {
        assertEquals(testPacket.getDirection(),"up");
    }

    /**
     * Test the getCarButton() method of the DataPacket class.
     * This test verifies that the getCarButton() method returns the expected car button value.
     */
    @org.junit.jupiter.api.Test
    void getCarButton() {
        assertEquals(testPacket.getCarButton(),"testButton");
    }
}