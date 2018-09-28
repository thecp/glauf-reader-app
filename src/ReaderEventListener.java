import java.util.ArrayList;

import com.caen.RFIDLibrary.CAENRFIDEvent;
import com.caen.RFIDLibrary.CAENRFIDEventListener;
import com.caen.RFIDLibrary.CAENRFIDNotify;

public class ReaderEventListener implements CAENRFIDEventListener {
	private ReaderEventHandler eventHandler;

	public void addReaderEventHandler(ReaderEventHandler r) {
		this.eventHandler = r;
	}

	@Override
	public void CAENRFIDTagNotify(CAENRFIDEvent arg0) {
		@SuppressWarnings("unchecked")
		ArrayList<CAENRFIDNotify> dataList = arg0.getData();

		for (int i = 0; i < dataList.size(); i++) {
			this.eventHandler.handleReaderEvent(dataList.get(i));
		}
	}
}
