import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.github.sarxos.webcam.Webcam;

public class Window implements ActionListener {

	public JFrame frame;
	public JTextField timeText;
	public JButton startTimerButton;
	public JButton pauseTimerButton;
	public JButton resetTimerButton;

	public JComboBox webcams;
	public JButton startCaptureButton;
	public JButton stopCaptureButton;

	public TimerListener timerListener;

	public CaptureListener captureListener;

	private int seconds = 0;

	public void addListener(TimerListener listener, CaptureListener captureListener) {
		this.timerListener = listener;
		this.captureListener = captureListener;
	}

	public Window() {
		this.frame = new JFrame();

		this.startTimerButton = new JButton("Start Timer");
		this.startTimerButton.setBounds(80, 100, 120, 40);
		this.startTimerButton.addActionListener(this);
		this.frame.add(this.startTimerButton);

		this.pauseTimerButton = new JButton("Pause Timer");
		this.pauseTimerButton.setBounds(210, 100, 120, 40);
		this.pauseTimerButton.addActionListener(this);
		this.frame.add(this.pauseTimerButton);

		this.resetTimerButton = new JButton("Reset Timer");
		this.resetTimerButton.setBounds(340, 100, 120, 40);
		this.resetTimerButton.addActionListener(this);
		this.frame.add(this.resetTimerButton);

		this.webcams = new JComboBox();
		this.webcams.addActionListener(this);
		this.webcams.setBounds(550, 100, 120, 40);
		this.frame.add(this.webcams);

		this.startCaptureButton = new JButton("Start Capture");
		this.startCaptureButton.setBounds(680, 100, 120, 40);
		this.startCaptureButton.addActionListener(this);
		this.frame.add(this.startCaptureButton);

		this.stopCaptureButton = new JButton("Stop Capture");
		this.stopCaptureButton.setBounds(810, 100, 120, 40);
		this.stopCaptureButton.addActionListener(this);
		this.frame.add(this.stopCaptureButton);

		this.timeText = new JTextField(this.seconds + "");
		this.timeText.setBounds(80, 200, 100, 40);
		this.frame.add(this.timeText);

		this.frame.setSize(1024, 800);
		this.frame.setLayout(null);
		this.frame.setVisible(true);
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == this.startTimerButton) {
			if (this.seconds == 0) {
				try {
					this.seconds = Integer.parseInt(this.timeText.getText());
				} catch (Exception e) {
					this.seconds = 0;
				}
				this.timerListener.startTimer(this.seconds);
			}
		}
		if (ae.getSource() == this.pauseTimerButton) {
			this.timerListener.pauseTimer();
		}
		if (ae.getSource() == this.resetTimerButton) {
			this.timerListener.resetTimer();
		}
		if (ae.getSource() == this.startCaptureButton) {
			this.captureListener.startCapture((Webcam) this.webcams.getSelectedItem());
		}
		if (ae.getSource() == this.stopCaptureButton) {
			this.captureListener.stopCapture();
		}
	}

	public void setTime(int seconds) {
		this.seconds = seconds;
		this.timeText.setText(seconds + "");
	}

	public void showMessage(String msg) {
		JOptionPane.showMessageDialog(null, msg);
	}

	public void setCams(List<Webcam> webcams) {
		for (int i = 0; i < webcams.size(); i++) {
			this.webcams.addItem(webcams.get(i));
		}
	}
}
