import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.caen.RFIDLibrary.CAENRFIDException;
import com.caen.RFIDLibrary.CAENRFIDLogicalSource;
import com.caen.RFIDLibrary.CAENRFIDLogicalSourceConstants;
import com.caen.RFIDLibrary.CAENRFIDNotify;
import com.caen.RFIDLibrary.CAENRFIDPort;
import com.caen.RFIDLibrary.CAENRFIDProtocol;
import com.caen.RFIDLibrary.CAENRFIDReadPointStatus;
import com.caen.RFIDLibrary.CAENRFIDReader;

public class ReaderThread implements Runnable, ReaderEventHandler {

	private volatile boolean running = true;
	private int seconds;
	private List<Integer> knownIds = new ArrayList<Integer>();

	private CAENRFIDReader reader = new CAENRFIDReader();
	private CAENRFIDLogicalSource source;
	private ReaderEventListener readerEventListener;
	private String[] readPoints;

	private SimpleDateFormat sdf;
	private AppListener app;
	private Connection conn;

	ReaderThread() {
		this.readerEventListener = new ReaderEventListener();
		this.readerEventListener.addReaderEventHandler(this);
		this.reader.addCAENRFIDEventListener(this.readerEventListener);

		this.sdf = new SimpleDateFormat("HH:mm:ss");
		this.sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	public void setTime(int seconds) {
		this.seconds = seconds;
	}

	public void addListener(AppListener listener) {
		this.app = listener;
	}

	private void writeIntoDB(int chipID) {
		if (this.seconds == 0 || this.conn == null) {
			return;
		}
		try {
			PreparedStatement stmt = this.conn
					.prepareStatement("INSERT INTO starters (seconds, time, rfid) VALUES (?, SEC_TO_TIME(?), ?) "
							+ "ON DUPLICATE KEY UPDATE seconds=?, time=SEC_TO_TIME(?);");
			stmt.setInt(1, this.seconds);
			stmt.setInt(2, this.seconds);
			stmt.setInt(3, chipID);
			stmt.setInt(4, this.seconds);
			stmt.setInt(5, this.seconds);
			stmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			this.app.onReaderException(e);
		}
	}

	private void openDBConnection() {
		String ssl = ""; // "&verifyServerCertificate=false&useSSL=true";
		String url = "jdbc:mysql://localhost:3306/zmt?serverTimezone=Europe/Berlin" + ssl;
		String user = "admin";
		String password = "7911640";
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			this.conn = DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			e.printStackTrace();
			this.app.onReaderException(e);
		}
	}

	private void writeIntoFile(int chipId) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("backup.tmp", true));
			bw.write(chipId + ";" + this.seconds + ";");
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			this.app.onReaderException(e);
		}
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

			this.source.SetReadCycle(0); // endless loop o reading
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
		if (this.seconds == 0) {
			return;
		}

		// TODO this should usually be filtered out by the reader!
		if (notifyEvent.getTagType() != CAENRFIDProtocol.CAENRFID_EPC_C1G2) {
			// we are only using this type!
			return;
		}

		// TODO this should usually be filtered out by the reader!
		byte[] id = notifyEvent.getTagID();
		if (id[0] > 0) {
			return;
		}

		int chipID = Helper.getIdFromByteArray(id);
		if (this.knownIds.contains(chipID)) {
			return;
		}
		this.knownIds.add(chipID);

		// TODO these both could be slightly blocking, move them into own thread!
		this.writeIntoFile(chipID);
		this.writeIntoDB(chipID);

		// TODO date calculation should be done in window...
		Date date = new Date((long) (this.seconds * 1000));
		this.app.addResult(chipID, this.sdf.format(date));
	}

	public void run() {
		this.openDBConnection();
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
}
