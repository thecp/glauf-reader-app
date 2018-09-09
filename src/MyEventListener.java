import com.caen.RFIDLibrary.CAENRFIDEvent;
import com.caen.RFIDLibrary.CAENRFIDEventListener;

public class MyEventListener implements CAENRFIDEventListener {
	public void CAENRFIDTagNotify(CAENRFIDEvent evt) {
		// System.out.println(evt.getData());
		System.out.println("just got here");
	}
}
