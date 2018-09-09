import java.util.Timer;

public class ReaderApp implements UpdateTimeListener, TimerListener, ReaderExceptionHandler {
	
	int seconds = 0;
	private Window window;
	private ReaderThread reader;
	
	private RTimerTask timerTask;
	private Timer timer;

	public void startTimer(int seconds) {
		this.timerTask = new RTimerTask();
		this.timerTask.addListener(this);
		this.timerTask.init(seconds);
		this.timer = new Timer();
		this.timer.schedule(timerTask, 1000, 1000);
	}

	public void pauseTimer() {
		this.timer.cancel();
		this.timer.purge();
	}

	public void resetTimer() {
		this.seconds = 0;
		this.updateTime(0);
	}
	
	public ReaderApp() {
		this.window = new Window();
		this.window.addListener(this);
		
		this.reader = new ReaderThread();
		this.reader.addListener(this);
		
		Thread t = new Thread(this.reader);
		t.start();
	}
	
	public void updateTime(int t) {
		this.window.setTime(t);
		this.reader.setTime(t);
	}

	public void onReaderException(Exception e) {
		this.window.showMessage(e.getMessage());
	}
}
