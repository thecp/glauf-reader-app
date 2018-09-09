import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class Window implements ActionListener {

	public JFrame frame;
	public JTextField timeText;
	public JButton startTimerButton;
	public JButton pauseTimerButton;
	public JButton resetTimerButton;

	public TimerListener timerListener;

	private int seconds = 0;

	public void addListener(TimerListener listener) {
		this.timerListener = listener;
	}

	public Window() {
		this.frame = new JFrame();

		this.startTimerButton = new JButton("Start Timer");
		this.startTimerButton.setBounds(130, 100, 120, 40);
		this.startTimerButton.addActionListener(this);
		this.frame.add(this.startTimerButton);

		this.pauseTimerButton = new JButton("Pause Timer");
		this.pauseTimerButton.setBounds(260, 100, 120, 40);
		this.pauseTimerButton.addActionListener(this);
		this.frame.add(this.pauseTimerButton);

		this.resetTimerButton = new JButton("Reset Timer");
		this.resetTimerButton.setBounds(390, 100, 120, 40);
		this.resetTimerButton.addActionListener(this);
		this.frame.add(this.resetTimerButton);

		this.timeText = new JTextField(this.seconds + "");
		this.timeText.setBounds(130, 200, 100, 40);
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
	}

	public void setTime(int seconds) {
		this.seconds = seconds;
		this.timeText.setText(seconds + "");
	}

	public void showMessage(String msg) {
		JOptionPane.showMessageDialog(null, msg);
	}
}
