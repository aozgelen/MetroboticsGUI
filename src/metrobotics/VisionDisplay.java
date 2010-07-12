package metrobotics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * @author Pablo Munoz
 * This class displays the images received from the Robots. 
 * It is also used to display the result from the Image Recognition using 
 * Neural Networks.
 */
public class VisionDisplay extends JPanel {
	ArrayList<Robot> robots;
	int i = 0;
	BufferedImage img;
	Robot bot;
	String message = "";
	
	public VisionDisplay(ArrayList<Robot> robots) {
		super();
		setBackground(Color.gray);
		// Toolkit tk = Toolkit.getDefaultToolkit();
		int Width = 450;//(int)(tk.getScreenSize().getWidth() * 0.15);//0.33);
		int Height = 350;//(int)(tk.getScreenSize().getHeight() * 0.15); //0.33);
	    Dimension d = new Dimension(Width, Height);
	    setPreferredSize(d);	
	    setBorder(BorderFactory.createRaisedBevelBorder());

		this.robots = robots;
		
		// This needs to be in a Thread
		VisionDisplayThread thread = new VisionDisplayThread(this);
		thread.start();
		//CameraThread cameraThread = new CameraThread(this);
		//cameraThread.start();
		//System.out.println("Vision started");
	}
	protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.yellow);
    	i++;
		if(img == null){
			g2.setFont(new Font("SansSerif", Font.BOLD, 36));
			g2.drawString("NO IMAGE" + i, 50, 100);
		}
		else{
			g2.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
			if(message != ""){
				g2.drawString(message, this.getWidth()/2 - 50, this.getHeight()/2);
			}
		}
	}
//	public void setImage(BufferedImage img) {
//		this.img = img;
//		repaint();
//	}
}

class VisionDisplayThread extends Thread{
	Robot bot;
	VisionDisplay vi;
	VisionDisplayThread(VisionDisplay vi){
		this.vi = vi;
	}
	
	public void run(){
		while(true){
			if(Robot.getRobotInUse()!=-1){
				bot = vi.robots.get(Robot.getRobotInUse());
				vi.img = bot.cameraImage;
				vi.repaint();
			}
		}
	}
	
}