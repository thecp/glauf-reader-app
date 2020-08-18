import java.util.TimerTask;

class RTimerTask extends TimerTask {
	UpdateTimeListener listener;
	private int time = 0;
	private int i;

	RTimerTask(int i) {
		this.i = i;
	}

	public void init(int seconds) {
		if (this.time == 0) {
			this.time = seconds;
		}
	}

	@Override
	public void run() {
		this.time++;
		if (this.listener != null) {
			this.listener.updateTime(this.time, i);
		}
	}

	public void addListener(UpdateTimeListener listener) {
		this.listener = listener;
	}
};
