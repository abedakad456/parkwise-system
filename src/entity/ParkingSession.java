
package entity;
import java.time.LocalDateTime;
//Entity: Tracks an active or past parking event
public class ParkingSession {
	private int sessionID;
    private LocalDateTime startTime;
    private String assignedConveyorID;

    public ParkingSession(int sessionID, LocalDateTime startTime, String conveyorID) {
        this.sessionID = sessionID;
        this.startTime = startTime;
        this.assignedConveyorID = conveyorID;
    }
}
