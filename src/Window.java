import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.caen.RFIDLibrary.CAENRFIDReadPointStatus;
import com.github.sarxos.webcam.Webcam;

public class Window implements ActionListener {

	public JFrame frame;

	public JTextField[] timeTexts;
//	public JTextField[] minTexts;
//	public JTextField[] maxTexts;
	public JLabel[] minTexts;
	public JLabel[] maxTexts;
	public JLabel[] trackTexts;
//	public JButton[] setRangeButtons;
	public JButton[] startTimerButtons;
	public JButton[] pauseTimerButtons;
	public JButton[] resetTimerButtons;

	public JComboBox<Webcam> webcams;
	public JButton startCaptureButton;
	public JButton stopCaptureButton;

	public JScrollPane scrollPane;
	public JTable resultTable;
	private TableModel resultTableModel;

	private JLabel[] statusList;

	public AppListener app;

	private RTimer[] rTimers;

	public void addListener(AppListener app) {
		this.app = app;
	}

	public void setTimers(RTimer[] rTimers) {
		this.rTimers = rTimers;
		for (int i = 0; i < 3; i++) {
			this.minTexts[i].setText("" + rTimers[i].min);
			this.maxTexts[i].setText("" + rTimers[i].max);
			this.trackTexts[i].setText(rTimers[i].track);
		}
	}

	public Window() {
		this.frame = new JFrame();

		this.timeTexts = new JTextField[3];
//		this.minTexts = new JTextField[3];
//		this.maxTexts = new JTextField[3];
		this.minTexts = new JLabel[3];
		this.maxTexts = new JLabel[3];
		this.trackTexts = new JLabel[3];
		this.startTimerButtons = new JButton[3];
		this.pauseTimerButtons = new JButton[3];
		this.resetTimerButtons = new JButton[3];
//		this.setRangeButtons = new JButton[3];

		for (int i = 0; i < 3; i++) {
			int yOffset = (i * 140) + 100;

			this.startTimerButtons[i] = new JButton("Start Timer");
			this.startTimerButtons[i].setBounds(80, yOffset, 120, 40);
			this.startTimerButtons[i].addActionListener(this);
			this.frame.add(this.startTimerButtons[i]);

			this.pauseTimerButtons[i] = new JButton("Pause Timer");
			this.pauseTimerButtons[i].setBounds(210, yOffset, 120, 40);
			this.pauseTimerButtons[i].addActionListener(this);
			this.frame.add(this.pauseTimerButtons[i]);

			this.resetTimerButtons[i] = new JButton("Reset Timer");
			this.resetTimerButtons[i].setBounds(340, yOffset, 120, 40);
			this.resetTimerButtons[i].addActionListener(this);
			this.frame.add(this.resetTimerButtons[i]);

			this.timeTexts[i] = new JTextField("0");
			this.timeTexts[i].setBounds(80, yOffset + 60, 120, 40);
			this.frame.add(this.timeTexts[i]);

			this.minTexts[i] = new JLabel("");
			this.minTexts[i].setBounds(220, yOffset + 60, 80, 40);
			this.frame.add(this.minTexts[i]);

			this.maxTexts[i] = new JLabel("");
			this.maxTexts[i].setBounds(310, yOffset + 60, 80, 40);
			this.frame.add(this.maxTexts[i]);

			this.trackTexts[i] = new JLabel("");
			this.trackTexts[i].setBounds(400, yOffset + 60, 80, 40);
			this.frame.add(this.trackTexts[i]);

//			this.setRangeButtons[i] = new JButton("Set");
//			this.setRangeButtons[i].setBounds(400, yOffset + 60, 60, 40);
//			this.setRangeButtons[i].addActionListener(this);
//			this.frame.add(this.setRangeButtons[i]);
		}

		this.webcams = new JComboBox<Webcam>();
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

		this.resultTableModel = new DefaultTableModel(new String[][] {}, new String[] { "rfid", "result" });
		this.resultTable = new JTable(this.resultTableModel);

		this.scrollPane = new JScrollPane(this.resultTable);
		this.scrollPane.setBounds(550, 250, 380, 400);
		this.resultTable.setFillsViewportHeight(true);
		this.frame.add(this.scrollPane);

		this.statusList = new JLabel[] { new JLabel("A1"), new JLabel("A2"), new JLabel("A3"), new JLabel("A4") };
		for (int i = 0; i < this.statusList.length; ++i) {
			this.statusList[i].setBounds(80, 550 + (i * 20), 40, 15);
			this.frame.add(this.statusList[i]);
		}

		this.frame.setSize(1024, 800);
		this.frame.setLayout(null);
		this.frame.setVisible(true);

		this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(frame, "Möchtest du das Programm wirklich beenden?",
						"Programm beenden", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});
		this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent ae) {
		for (int i = 0; i < 3; i++) {
			if (ae.getSource() == this.startTimerButtons[i]) {
				if (this.rTimers[i].seconds == 0) {
					try {
						String text = this.timeTexts[i].getText();
						if (text.matches("^[0-9]{2}:[0-9]{2}:[0-9]{2}$")) {
							this.rTimers[i].seconds = Helper.timeToInt(text);
						} else if (text.matches("^[0-9]+$")) {
							this.rTimers[i].seconds = Integer.parseInt(text);
						} else {
							throw new Exception("Zeitformat nicht valide!!!");
						}
					} catch (Exception e) {
						this.rTimers[i].seconds = 0;
					}
					this.rTimers[i].startTimer();
				}
			}
			if (ae.getSource() == this.pauseTimerButtons[i]) {
				this.rTimers[i].pauseTimer();
			}
			if (ae.getSource() == this.resetTimerButtons[i]) {
				if (this.rTimers[i].resetTimer()) {
					this.timeTexts[i].setText("0");
				}
			}
//			if (ae.getSource() == this.setRangeButtons[i]) {
//				try {
//					this.rTimers[i].min = Integer.parseInt(this.minTexts[i].getText());
//					this.rTimers[i].max = Integer.parseInt(this.maxTexts[i].getText());
//				} catch (Exception e) {
//					this.app.onReaderException(e);
//				}
//			}
		}
		if (ae.getSource() == this.startCaptureButton) {
			this.app.startCapture((Webcam) this.webcams.getSelectedItem());
		}
		if (ae.getSource() == this.stopCaptureButton) {
			this.app.stopCapture();
		}
	}

	public void setTime(int seconds, int i) {
		this.rTimers[i].seconds = seconds;
		this.timeTexts[i].setText(Helper.intToTime(seconds));
	}

	public void showMessage(String msg) {
		JOptionPane.showMessageDialog(null, msg);
	}

	public void setCams(List<Webcam> webcams) {
		for (int i = 0; i < webcams.size(); i++) {
			this.webcams.addItem(webcams.get(i));
		}
	}

	public void addResult(int rfid, String result) {
		try {
			DefaultTableModel model = (DefaultTableModel) this.resultTable.getModel();
			model.addRow(new String[] { Integer.toString(rfid), result });
		} catch (Exception e) {
			this.showMessage(e.getMessage());
		}
	}

	public void setStatus(CAENRFIDReadPointStatus[] status) {
		if (status != null) {
			for (int i = 0; i < Math.min(status.length, this.statusList.length); i++) {
				Color c = Color.red; // STATUS_BAD
				if (status[i] == CAENRFIDReadPointStatus.STATUS_POOR) {
					c = Color.orange;
				} else if (status[i] == CAENRFIDReadPointStatus.STATUS_GOOD) {
					c = Color.green;
				}
				this.statusList[i].setForeground(c);
			}
		}
	}
}
