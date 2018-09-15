import com.github.sarxos.webcam.Webcam;

interface AppListener extends ReaderExceptionHandler {
	void startTimer(int t);

	void pauseTimer();

	void resetTimer();

	void startCapture(Webcam w);

	void stopCapture();

	void exit();

	void addResult(int rfid, String result);
}

interface UpdateTimeListener {
	void updateTime(int t);
}

interface ReaderExceptionHandler {
	void onReaderException(Exception e);
}

public class Main {

	public static void main(String[] args) {
		ReaderApp readerApp = new ReaderApp();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				readerApp.exit();
			}
		});
	}
}
