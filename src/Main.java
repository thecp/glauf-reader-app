interface TimerListener {
	void startTimer(int t);

	void pauseTimer();

	void resetTimer();
}

interface UpdateTimeListener {
	void updateTime(int t);
}

interface ReaderExceptionHandler {
	void onReaderException(Exception e);
}

public class Main {

	public static void main(String[] args) {
		new ReaderApp();
	}
}
