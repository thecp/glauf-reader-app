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
import com.caen.RFIDLibrary.CAENRFIDPort;
import com.caen.RFIDLibrary.CAENRFIDProtocol;
import com.caen.RFIDLibrary.CAENRFIDReader;
import com.caen.RFIDLibrary.CAENRFIDTag;

public class ReaderThread implements Runnable {

	private volatile boolean running = true;

	private CAENRFIDReader reader;
	private CAENRFIDLogicalSource source = null;

	private int seconds;

	private AppListener app;

	private Connection conn;

	private List<Integer> knownIds = new ArrayList<Integer>();

	public void setTime(int seconds) {
		this.seconds = seconds;
	}

	public void addListener(AppListener listener) {
		this.app = listener;
	}

	private int getIdValue(byte[] id) {
		int i = 0;
		try {
			for (int j = 0; j < id.length; ++j) {
				String hex = Integer.toHexString(id[j]);
				if (hex.length() > 2) {
					hex = hex.substring(hex.length() - 2);
				}
				i += (Math.pow(100, id.length - j - 1)) * Integer.valueOf(hex);
			}
		} catch (Exception e) {
			e.printStackTrace();
			i = 0;
		}
		return i;
	}

	private void writeIntoDB(int chipID) {
		if (this.seconds == 0) {
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
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
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

//			myReader.addCAENRFIDEventListener(new EventListener());
//			
//			CAENRFIDLogicalSource[] sources = myReader.GetSources();

//			byte[] mask = new byte[] {};
//			short maskLength = 0;
//			short position = 0;
//			short flag = 0x0E;

			this.source = reader.GetSource("Source_0");
			source.SetQ_EPC_C1G2(2);

//			String[] readPoints = myReader.GetReadPoints();
//			for (int i = 0; i < readPoints.length; ++i) {
//				CAENRFIDReadPointStatus r = myReader.GetReadPointStatus(readPoints[i]);
//				System.out.println();
//			}

//			source.addCAENRFIDEventListener(new MyEventListener());
//			source.EventInventoryTag(mask, maskLength, position, flag);

		} catch (Exception e) {
			e.printStackTrace();
			this.app.onReaderException(e);
		}
	}

	public void shutdown() {
		this.running = false;
		this.stopReader();
	}

	private void readTags() {
		CAENRFIDTag[] rfidTags;
		try {
			rfidTags = this.source.InventoryTag();
			if (rfidTags != null && this.seconds != 0) {
				for (int i = 0; i < rfidTags.length; ++i) {
					byte[] id = rfidTags[i].GetId();
					if (rfidTags[i].GetType() != CAENRFIDProtocol.CAENRFID_EPC_C1G2) {
						// we are only using this type!
						continue;
					}
					if (id[0] > 0) {
						continue;
					}
					int chipID = this.getIdValue(id);
					if (this.knownIds.contains(chipID)) {
						continue;
					}
					this.knownIds.add(chipID);
					this.writeIntoFile(chipID);
					this.writeIntoDB(chipID);

					Date date = new Date((long) (this.seconds * 1000));
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
					sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
					this.app.addResult(chipID, sdf.format(date));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.app.onReaderException(e);
		}
	}

	public void run() {
		this.reader = new CAENRFIDReader();
		this.openDBConnection();
		this.startReader();

		while (this.running) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				this.app.onReaderException(e);
			}
			this.readTags();
		}
	}
}
