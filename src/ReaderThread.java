import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import com.caen.RFIDLibrary.*;

public class ReaderThread implements Runnable {

	public CAENRFIDReader myReader;
	
	private int seconds;
	
	private Connection conn;
	
	private List<Integer> knownIds = new ArrayList<Integer>();
	
	public void setTime(int seconds) {
		this.seconds = seconds;
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
		} catch(Exception e) {
			i = 0;
		}
		return i;
	}
	
	private void writeIntoDB(int chipID) {
		if (this.seconds == 0) {
			return;
		}
		try {
			PreparedStatement stmt = this.conn.prepareStatement(
				"UPDATE starters SET insertTime=NOW(), seconds=? WHERE chipId=? AND seconds IS NULL;"
			);
			stmt.setInt(1, this.seconds);
			stmt.setInt(2, chipID);
			stmt.executeUpdate();
		} catch(Exception e) {
			System.out.println("Konnte nicht in Datenbank speichern: " + chipID);
		}
	}
	
	private void openDBConnection() {
		String url = "jdbc:mysql://localhost:3306/zmt";
		String user = "root";
		String password = "7911640";
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			this.conn = DriverManager.getConnection(url, user, password);
		} catch(Exception e) {
			System.out.println("Keine Verbindung zu MySQL möglich");
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
			System.out.println("Konnte Daten nicht in Datei schreiben!");
		}
	}
	
	public void run() {

		this.openDBConnection();
		
		try {
		
			this.myReader = new CAENRFIDReader();
		
			myReader.Connect(CAENRFIDPort.CAENRFID_TCP, "192.168.0.7:23");
			
//			myReader.addCAENRFIDEventListener(new EventListener());
//			
//			CAENRFIDLogicalSource[] sources = myReader.GetSources();
			
//			byte[] mask = new byte[] {};
//			short maskLength = 0;
//			short position = 0;
//			short flag = 0x0E;
			
			CAENRFIDTag[] MyTags;
			CAENRFIDLogicalSource source = myReader.GetSource("Source_0");
			
			source.SetQ_EPC_C1G2(2);
			
//			String[] readPoints = myReader.GetReadPoints();
//			for (int i = 0; i < readPoints.length; ++i) {
//				CAENRFIDReadPointStatus r = myReader.GetReadPointStatus(readPoints[i]);
//				System.out.println();
//			}
			
//			source.addCAENRFIDEventListener(new MyEventListener());
//			source.EventInventoryTag(mask, maskLength, position, flag);
		
			while (true) {
				MyTags = source.InventoryTag();
				if (MyTags != null && this.seconds != 0) {
					for (int i = 0; i < MyTags.length; ++i) {
						byte[] id = MyTags[i].GetId();
						if (id[0] > 0) {
							continue;
						}
						int chipID = this.getIdValue(id);
						if (!this.knownIds.contains(chipID)) {
							this.knownIds.add(chipID);
						} else {
							continue;
						}
						System.out.println(chipID);
						this.writeIntoFile(chipID);
						this.writeIntoDB(chipID);
					}
				}
				Thread.sleep(100);
			}
			
		} catch(Exception e) {
			System.out.println(e);
		}
	}
}
