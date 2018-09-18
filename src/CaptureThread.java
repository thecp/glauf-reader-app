import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class CaptureThread implements Runnable {

	private volatile boolean running = true;

	private Webcam webcam = null;
	private int time = 0;
	private boolean capturing = false;

	private ReaderExceptionHandler readerExceptionHandler;

	public void addListener(ReaderExceptionHandler listener) {
		this.readerExceptionHandler = listener;
	}

	public void setCam(Webcam w) {
		this.webcam = w;
		this.webcam.open();
	}

	public void start() {
		this.capturing = true;
	}

	public void stop() {
		this.capturing = false;
	}

	public void shutdown() {
		running = false;
	}

	public void updateTime(int t) {
		this.time = t;
		if (this.capturing) {
			this.capture();
		}
	}

	public void run() {
		while (running) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				this.readerExceptionHandler.onReaderException(e);
			}
		}
	}

	private void capture() {
		if (this.webcam != null && this.time != 0) {
			BufferedImage image = this.webcam.getImage();
			try {
				ImageIO.write(image, "PNG", new File(this.getFileName()));
			} catch (Exception e) {
				e.printStackTrace();
				this.readerExceptionHandler.onReaderException(e);
			}
		}
	}

	private String getFileName() {
		return String.format("capture_%d_%s_%s.png", this.time, LocalDate.now(),
				LocalTime.now().toString().replace(":", "_"));
	}
}
