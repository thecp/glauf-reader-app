import java.util.Timer;

public class RTimer {
	int seconds = 0;
	int i;
	private RTimerTask timerTask;
	private Timer timer;
	private ReaderApp readerApp;

	int min = 0, max = 1000;
	String track;

	RTimer(int i, ReaderApp readerApp, String track, int min, int max) {
		this.i = i;
		this.readerApp = readerApp;
		this.track = track;
		this.min = min;
		this.max = max;
	}

	public void startTimer() {
		this.timerTask = new RTimerTask(this.i);
		this.timerTask.addListener(this.readerApp);
		this.timerTask.init(seconds);
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(timerTask, 1000, 1000);
	}

	public void pauseTimer() {
		if (this.timer != null) {
			this.timer.cancel();
			this.timer.purge();
		}
		this.timer = null;
	}

	public boolean resetTimer() {
		if (this.timer == null) {
			this.seconds = 0;
			this.timerTask.cancel();
			return true;
		}
		return false;
	}

	public void setRange(int min, int max) {
		this.min = min;
		this.max = max;
	}
}
