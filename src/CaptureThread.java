import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class CaptureThread implements Runnable {

	private volatile boolean running = true;

	private Webcam webcam = null;
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

	public void run() {
		while (running) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				this.readerExceptionHandler.onReaderException(e);
			}

			this.capture();
		}
	}

	private void capture() {
		if (this.webcam != null && this.capturing) {
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
		return String.format("capture_%s_%s.png", LocalDate.now(), LocalTime.now().toString().replace(":", "_"));
	}
}
