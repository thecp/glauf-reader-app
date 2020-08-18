import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.caen.RFIDLibrary.CAENRFIDException;
import com.caen.RFIDLibrary.CAENRFIDLogicalSource;
import com.caen.RFIDLibrary.CAENRFIDLogicalSourceConstants;
import com.caen.RFIDLibrary.CAENRFIDNotify;
import com.caen.RFIDLibrary.CAENRFIDPort;
import com.caen.RFIDLibrary.CAENRFIDReadPointStatus;
import com.caen.RFIDLibrary.CAENRFIDReader;

public class ReaderThread implements Runnable, ReaderEventHandler {

	private volatile boolean running = true;
	private DataThread dataThread;

	private RTimer[] rTimers;

	private List<Integer> knownStnos = new ArrayList<Integer>();
	private Map<String, Integer> idMapping;

	private CAENRFIDReader reader = new CAENRFIDReader();
	private CAENRFIDLogicalSource source;
	private ReaderEventListener readerEventListener;
	private String[] readPoints;

	private AppListener app;

	ReaderThread() {
		this.readerEventListener = new ReaderEventListener();
		this.readerEventListener.addReaderEventHandler(this);
		this.reader.addCAENRFIDEventListener(this.readerEventListener);
	}

	public void addListener(AppListener listener) {
		this.app = listener;
	}

	public void setTimers(RTimer[] rTimers) {
		this.rTimers = rTimers;
	}

	public void stopReader() {
		try {
			this.reader.InventoryAbort();
			this.reader.Disconnect();
		} catch (CAENRFIDException e) {
			e.printStackTrace();
			this.app.onReaderException(e);
		}
	}

	public void startReader() {
		try {
			// TODO make this configurable
			this.reader.Connect(CAENRFIDPort.CAENRFID_TCP, "192.168.0.7:23");

			// just to make sure, cancel all event inventory stuff
			this.reader.InventoryAbort();

			this.source = reader.GetSource("Source_0");

			// more cleanup
			this.source.ClearBuffer();
			this.source.ResetSession_EPC_C1G2();

			// SET Q Level to 3 (7-15 tags)
			// TODO still not sure if Q2 or Q3
			this.source.SetQ_EPC_C1G2(3);

			// enable all antennas
			this.readPoints = this.reader.GetReadPoints();
			if (readPoints != null) {
				for (int i = 0; i < readPoints.length; i++) {
					this.source.AddReadPoint(readPoints[i]);
				}
			}

			// TODO make these variables more strict to only read relevant numbers
			byte[] mask = new byte[4];
			short maskLength = 0x0;
			short position = 0x0;
			short flag = 0x06;

			this.source.SetReadCycle(0); // endless loop of reading
			this.source.SetSelected_EPC_C1G2(CAENRFIDLogicalSourceConstants.EPC_C1G2_All_SELECTED); // no tag filtering
			this.source.EventInventoryTag(mask, maskLength, position, flag); // start the inventory continuous mode

		} catch (Exception e) {
			e.printStackTrace();
			this.app.onReaderException(e);
		}
	}

	public void shutdown() {
		this.running = false;
		this.stopReader();
		this.dataThread.stop();
	}

	private void updateInformation() {
		if (this.readPoints != null) {
			CAENRFIDReadPointStatus[] s = new CAENRFIDReadPointStatus[this.readPoints.length];
			for (int i = 0; i < this.readPoints.length; i++) {
				try {
					CAENRFIDReadPointStatus status = this.reader.GetReadPointStatus(this.readPoints[i]);
					s[i] = status;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			this.app.updateStatus(s);
		}
	}

	public void handleReaderEvent(CAENRFIDNotify notifyEvent) {
//		TODO this should usually be filtered out by the reader!
//		if (notifyEvent.getTagType() != CAENRFIDProtocol.CAENRFID_EPC_C1G2) {
//		we are only using this type!
//		return;
//		}

// 		TODO this should usually be filtered out by the reader!
		byte[] id = notifyEvent.getTagID();
//		if (id[0] > 0) {
//			return;
//		}

		String strID = Helper.getIdFromByteArrayNew(id);
		Integer stno = this.idMapping.get(strID);
		if (stno == null) {
			System.out.println("Warning, unknown id: " + strID);
			return;
		}

		if (this.knownStnos.contains(stno)) {
			return;
		}

		int seconds = this.getSecondsForStno(stno);
		if (seconds == 0) {
			return;
		}

		this.knownStnos.add(stno);
		this.dataThread.write(stno, seconds);
		this.app.addResult(stno, seconds);
	}

	public void run() {
		this.dataThread = new DataThread();
		this.dataThread.addListener(this.app);
		Thread dt = new Thread(this.dataThread);
		dt.start();
		this.idMapping = this.dataThread.getStnoMappings();

		this.startReader();

		while (this.running) {
			// this.updateInformation();
			// FIXME, not possible as long as there is continuous inventory in progress
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				this.app.onReaderException(e);
			}
		}
	}

	public int getSecondsForStno(int stno) {
		if (this.rTimers == null) {
			return 0;
		}
		for (int i = 0; i < 3; i++) {
			if (this.rTimers[i] == null) {
				return 0;
			}
			if (this.rTimers[i].min <= stno && this.rTimers[i].max >= stno) {
				return this.rTimers[i].seconds;
			}
		}
		return 0;
	}
}
