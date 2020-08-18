import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DataThread implements Runnable {
	private volatile boolean running = true;

	private AppListener app;
	private Connection conn;

	public void addListener(AppListener listener) {
		this.app = listener;
	}

	public void write(int chipId, int seconds) {
		CompletableFuture.runAsync(() -> this.writeIntoDB(chipId, seconds));
		CompletableFuture.runAsync(() -> this.writeIntoFile(chipId, seconds));
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

	private void writeIntoFile(int chipId, int seconds) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("backup.tmp", true));
			bw.write(chipId + ";" + seconds + ";");
			bw.newLine();
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
			this.app.onReaderException(e);
		}
	}

	private void writeIntoDB(int chipID, int seconds) {
		if (seconds == 0 || this.conn == null) {
			return;
		}
		try {
			PreparedStatement stmt = this.getConnection()
					.prepareStatement("INSERT INTO starters (seconds, time, rfid) VALUES (?, SEC_TO_TIME(?), ?) "
							+ "ON DUPLICATE KEY UPDATE seconds=?, time=SEC_TO_TIME(?);");
			stmt.setInt(1, seconds);
			stmt.setInt(2, seconds);
			stmt.setInt(3, chipID);
			stmt.setInt(4, seconds);
			stmt.setInt(5, seconds);
			stmt.executeUpdate();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			this.app.onReaderException(e);
		}
	}

	private Connection getConnection() {
		if (this.conn == null) {
			this.openDBConnection();
		}
		return this.conn;
	}

	public Map<String, Integer> getStnoMappings() {
		Map<String, Integer> map = new HashMap<String, Integer>();

		try {
			Statement stmt = this.getConnection().createStatement();
			ResultSet res = stmt.executeQuery("SELECT id, stno FROM rfid_mappings");
			while (res.next()) {
				String id = res.getString(1);
				int stno = res.getInt(2);
				map.put(id, stno);
			}
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			this.app.onReaderException(e);
		}

		return map;
	}

	public void stop() {
		if (this.conn != null) {
			try {
				this.conn.close();
			} catch (Exception e) {
				e.printStackTrace();
				this.app.onReaderException(e);
			}
		}
		this.running = false;
	}

	public void run() {
		while (this.running) {
			// do something
		}
	}
}
