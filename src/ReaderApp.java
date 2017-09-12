import java.util.Timer;
import java.util.TimerTask;

public class ReaderApp implements UpdateTimeListener, TimerListener {
	
	int seconds = 0;
	private Window window;
	private ReaderThread reader;
	
	public void startTimer(int seconds) {

		class MyTimerTask extends TimerTask {
			UpdateTimeListener listener;
			private int time = 0;
			
			public void init(int seconds) {
				if (this.time == 0) {
					this.time = seconds;
				}
			}
			
			@Override
			public void run() {
				this.time++;
				if (this.listener != null) {
					this.listener.updateTime(this.time);
				}
			}
			
			public void addListener(UpdateTimeListener listener) {
				this.listener = listener;
			}
		};
		
		Timer timer = new Timer();
		MyTimerTask timerTask = new  MyTimerTask();
		timerTask.init(seconds);
		timerTask.addListener(this);
		timer.schedule(timerTask, 1000, 1000);
	}
	
	public ReaderApp() {
		
		this.window = new Window();
		this.window.addListener(this);
		
		this.reader = new ReaderThread();
		
		Thread t = new Thread(this.reader);
		t.start();
	}
	
	public void updateTime(int t) {
		this.window.setTime(t);
		this.reader.setTime(t);
	}
}
