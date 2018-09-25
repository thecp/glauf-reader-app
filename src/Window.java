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
	public JTextField timeText;
	public JButton startTimerButton;
	public JButton pauseTimerButton;
	public JButton resetTimerButton;

	public JComboBox<Webcam> webcams;
	public JButton startCaptureButton;
	public JButton stopCaptureButton;

	public JScrollPane scrollPane;
	public JTable resultTable;
	private TableModel resultTableModel;

	private JLabel[] statusList;

	public AppListener app;

	private int seconds = 0;

	public void addListener(AppListener app) {
		this.app = app;
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

		this.timeText = new JTextField(this.seconds + "");
		this.timeText.setBounds(80, 200, 100, 40);
		this.frame.add(this.timeText);

		this.resultTableModel = new DefaultTableModel(new String[][] {}, new String[] { "rfid", "result" });
		this.resultTable = new JTable(this.resultTableModel);

		this.scrollPane = new JScrollPane(this.resultTable);
		this.scrollPane.setBounds(550, 250, 380, 400);
		this.resultTable.setFillsViewportHeight(true);
		this.frame.add(this.scrollPane);

		this.statusList = new JLabel[] {
				new JLabel("A1 •"),
				new JLabel("A2 •"),
				new JLabel("A3 •"),
				new JLabel("A4 •")
		};
		for (int i = 0; i < this.statusList.length; ++i) {
			this.statusList[i].setBounds(30, 500 + (i * 20), 40, 15);
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
		if (ae.getSource() == this.startTimerButton) {
			if (this.seconds == 0) {
				try {
					this.seconds = Integer.parseInt(this.timeText.getText());
				} catch (Exception e) {

					this.seconds = 0;
				}
				this.app.startTimer(this.seconds);
			}
		}
		if (ae.getSource() == this.pauseTimerButton) {
			this.app.pauseTimer();
		}
		if (ae.getSource() == this.resetTimerButton) {
			this.app.resetTimer();
		}
		if (ae.getSource() == this.startCaptureButton) {
			this.app.startCapture((Webcam) this.webcams.getSelectedItem());
		}
		if (ae.getSource() == this.stopCaptureButton) {
			this.app.stopCapture();
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
