import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataPacketTest {
    DataPacket testPacket = new DataPacket("Fifteen","Fifteen","up","testButton");
    @org.junit.jupiter.api.Test
    void getTime() {
        assertEquals(testPacket.getTime(),"Fifteen");
    }

    @org.junit.jupiter.api.Test
    void getFloor() {
        assertEquals(testPacket.getFloor(),"Fifteen");
    }

    @org.junit.jupiter.api.Test
    void getDirection() {
        assertEquals(testPacket.getDirection(),"up");
    }

    @org.junit.jupiter.api.Test
    void getCarButton() {
        assertEquals(testPacket.getCarButton(),"testButton");
    }
}