import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Timer;

import com.github.sarxos.webcam.Webcam;

public class ReaderApp implements AppListener, UpdateTimeListener, ReaderExceptionHandler {

	int seconds = 0;
	private Window window;
	private ReaderThread readerThread;
	private CaptureThread captureThread;

	private RTimerTask timerTask;
	private Timer timer;

	private ServerSocket socket;

	private List<Webcam> webcams;

	public void startTimer(int seconds) {
		this.timerTask = new RTimerTask();
		this.timerTask.addListener(this);
		this.timerTask.init(seconds);
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(timerTask, 1000, 1000);
	}

	public void pauseTimer() {
		this.timer.cancel();
		this.timer.purge();
	}

	public void resetTimer() {
		this.seconds = 0;
		this.updateTime(0);
	}

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
			// port already used: exit application
			this.exit();
		}

		this.window = new Window();
		this.window.addListener(this);

		this.webcams = Webcam.getWebcams();
		this.window.setCams(this.webcams);

		this.readerThread = new ReaderThread();
		this.readerThread.addListener(this);

		Thread rt = new Thread(this.readerThread);
		rt.start();

		this.captureThread = new CaptureThread();
		Thread ct = new Thread(this.captureThread);
		ct.start();
	}

	public void updateTime(int t) {
		this.window.setTime(t);
		this.readerThread.setTime(t);
		this.captureThread.updateTime(t);
	}

	public void onReaderException(Exception e) {
		this.window.showMessage(e.getMessage());
	}

	public void addResult(int rfid, String result) {
		this.window.addResult(rfid, result);
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
		System.exit(0);
	}
}
