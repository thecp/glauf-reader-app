import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class Window implements ActionListener {
	
	public JFrame frame;
	public JTextField timeText;
	public JButton timerButton;
	
	public TimerListener timerListener;
	
	private int seconds = 0;
	
	public void addListener(TimerListener listener) {
		this.timerListener = listener;
	}

	public Window() {
		this.frame = new JFrame();  
		          
		this.timerButton = new JButton("Start Timer"); //creating instance of JButton  
		this.timerButton.setBounds(130,100,100, 40);//x axis, y axis, width, height  
		this.timerButton.addActionListener(this);
		this.frame.add(this.timerButton);//adding button in JFrame  
		
		this.timeText = new JTextField(this.seconds + "");
		this.timeText.setBounds(130,200,100, 40);//x axis, y axis, width, height  
		this.frame.add(this.timeText);//adding button in JFrame  
		       
		this.frame.setSize(400,500);//400 width and 500 height  
		this.frame.setLayout(null);//using no layout managers  
		this.frame.setVisible(true);//making the frame visible  
	}
	
	public void actionPerformed (ActionEvent ae) {
		if (ae.getSource() == this.timerButton) {
			if (this.seconds == 0) {
				try {
					this.seconds = Integer.parseInt(this.timeText.getText());
				} catch (Exception e) {
					this.seconds = 0;
				}
				this.timerListener.startTimer(this.seconds);
			}
		}
    }
	
	public void setTime(int seconds) {
		this.seconds = seconds;
		this.timeText.setText(seconds + "");
	}
}  