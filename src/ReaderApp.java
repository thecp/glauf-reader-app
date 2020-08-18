import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.caen.RFIDLibrary.CAENRFIDReadPointStatus;
import com.github.sarxos.webcam.Webcam;

public class ReaderApp implements AppListener, UpdateTimeListener, ReaderExceptionHandler {

	private Window window;
	private ReaderThread readerThread;
	private CaptureThread captureThread;

	private RTimer[] rTimers;

	private ServerSocket socket;

	private List<Webcam> webcams;

	private SimpleDateFormat sdf;

	public void startCapture(Webcam w) {
		this.captureThread.setCam(w);
		this.captureThread.start();
	}

	public void stopCapture() {
		this.captureThread.stop();
	}

	public ReaderApp() {

		try {
			this.socket = new ServerSocket(11333);
		} catch (IOException e) {
			System.out.println("port in use");
			System.exit(1);
		}

		this.window = new Window();
		this.window.addListener(this);

		this.sdf = new SimpleDateFormat("HH:mm:ss");
		this.sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		this.webcams = Webcam.getWebcams();
		this.window.setCams(this.webcams);

		this.readerThread = new ReaderThread();
		this.readerThread.addListener(this);

		this.rTimers = new RTimer[3];
		this.rTimers[0] = new RTimer(0, this, "HM", 501, 800);
		this.rTimers[1] = new RTimer(1, this, "11.5km", 251, 500);
		this.rTimers[2] = new RTimer(2, this, "6.5km", 1, 250);
		this.window.setTimers(this.rTimers);
		this.readerThread.setTimers(this.rTimers);

		Thread rt = new Thread(this.readerThread);
		rt.start();

		this.captureThread = new CaptureThread();
		Thread ct = new Thread(this.captureThread);
		ct.start();
	}

	public void updateTime(int t, int i) {
		this.window.setTime(t, i);
		this.rTimers[i].seconds = t;
	}

	public void onReaderException(Exception e) {
		this.window.showMessage(e.getMessage());
	}

	public void addResult(int rfid, int seconds) {
		Date date = new Date((long) (seconds * 1000));
		this.window.addResult(rfid, this.sdf.format(date));
	}

	public void updateStatus(CAENRFIDReadPointStatus[] s) {
		this.window.setStatus(s);
	}

	public void exit() {
		try {
			this.socket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("shutdown");
		this.readerThread.shutdown();
		this.captureThread.shutdown();
	}
}
