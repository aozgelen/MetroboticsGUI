package metrobotics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;

public class RobotSelector extends JPanel implements Scrollable{
	JLabel robotInUse;
	ArrayList<Robot> robots;
	JButton [] robotButtons;
	ImageIcon [] robotIcons;
	JButton [] grabButtons;
	JButton [] ungrabButtons;
	JButton [] imageButtons;
	JButton [] videoButtons;
	JPanel buttons;
	VisionDisplay vi;
	//boolean threadVision = false;
	static CameraThread cameraThread;
	static CameraAiboTekThread cameraAiboThread;
	int aiboPortInUse;
	int Width;
	int Height;
	//boolean visionThreadStarted = false;
	
	RobotSelector(JLabel robotInUse, ArrayList<Robot> robots, VisionDisplay vision){
		super();
		this.robotInUse = robotInUse;
		this.robots = robots;
		this.vi = vision;
		setBackground(Color.gray);
		// Toolkit tk = Toolkit.getDefaultToolkit();
		Width = 130; // 120//110 (int)(tk.getScreenSize().getWidth() * 0.05);
		Height = 800; //(int)(tk.getScreenSize().getHeight() * 1);
		//Width -= 10;
	    Dimension d = new Dimension(Width, Height);
	    setPreferredSize(d);		
	    setLayout(new BorderLayout());
	    setBorder(BorderFactory.createRaisedBevelBorder());
	    
	    System.out.println(this.getPreferredSize());
	    
	    robotButtons = new JButton[robots.size()];
	    robotIcons = new ImageIcon[robots.size()];
	    grabButtons = new JButton[robots.size()];
	    ungrabButtons = new JButton[robots.size()];
		imageButtons = new JButton[robots.size()];
		videoButtons = new JButton[robots.size()];
	    int i=0;
	    buttons = new JPanel();
	    for (final Robot x : robots) {
    		//final Robot x = (Robot)e.nextElement();
    		String name = x.getName();
    		System.out.println(name);
    		robotIcons[i] = GuiUtils.resizeJP(x.getRobotIcon().getImage(), Width, Width);
    		final JLabel jl = robotInUse;
    		if(name==null){
    			name = "No Name";
    		}
    		robotButtons[i] = new JButton(name, robotIcons[i]);
    		robotButtons[i].setVerticalTextPosition(AbstractButton.TOP);
    		robotButtons[i].setHorizontalTextPosition(AbstractButton.CENTER);
    		if(!Gui.useCentralServer){
	    		robotButtons[i].addActionListener(new ActionListener() {
					private boolean threadVision;
					private boolean threadVisionAibo;
	
					public void actionPerformed(ActionEvent e) {
						// TODO Think about how to use the key in robots hashtable
	    	        	System.out.println(x.getName() + " " + Robot.getRobotInUse());
	    	        	Robot.setRobotInUse(x.getRobotKey());
	    	        	System.out.println(x.getName() + " " + Robot.getRobotInUse());
	    	        	jl.setText(x.getName());
	    	        	
	    	        	// Start Thread for Vision for this Robot. Kill all the other Threads for Camera.
	    	        	// TODO: It is missing the implementation of switching between the Segmented and the Raw Camera.
	    	        	if(x.getHasCamera()){
	    	        		System.out.println("In Robot Selects gethasCamera");
	    	        		Robot.setAiboCameraThread(false); // End all AiboCameraThreads
	    	        		if(threadVisionAibo){
	    	        			while(cameraAiboThread.isAlive()){
	        	        			//System.out.println("Old Thread is still alive");
	        	        		}
	    	        		}
	    	        		Robot.setCameraThread(false); // End all other CameraThreads;
	    	        		if(threadVision){
	    	        			while(cameraThread.isAlive()){
	    	        				System.out.println("Waiting for old thread to end");
	    	        				System.out.println(Robot.getCameraThread());
	        	        			//System.out.println("Old Thread is still alive");
	        	        		}
	    	        		}
	    	        		Robot.setCameraThread(true);
	    	        		cameraThread = new CameraThread(x);
	    	        		cameraThread.start();
	    	        		System.out.println("Vision Thread started");
	    	        		threadVision = true;
	    	        	}
	    	        	else if(x.getHasCameraAibo()){
	    	        		if(Gui.debug){
	    	        			System.out.println("In Switch");
	    	        		}
	    	        		// If we have only one port, we shouldn't enter this statement. 
	    	        		Robot.setAiboCameraThread(false); // End Previous thread; // This inside while below????
	    	        		// This should be done in a different way. For instance checking that the thread has ended.
	    	        		if(threadVisionAibo){
	    	        			while(cameraAiboThread.isAlive()){
	        	        			//System.out.println("Old Thread is still alive");
	        	        		}
	    	        		}
	    	        		Robot.setCameraThread(false); // End all other CameraThreads;
	    	        		if(threadVision){
		    	        		while(cameraThread.isAlive()){
		    	        			//System.out.println("Old Thread is still alive");
		    	        		}
	    	        		}
	    	        		aiboPortInUse = x.getRawCameraPort();
	    	        		// This was for switching from Seg to Raw, or Raw to Seg. 
	    	        		//aiboPortInUse = (aiboPortInUse == x.getRawCameraPort())? x.getSegCameraPort() : x.getRawCameraPort();
	    	        		//if(aiboPortInUse == 0)aiboPortInUse = x.getRawCameraPort();
	                        Robot.setAiboCameraThread(true); // So the new Thread can run
	                        cameraAiboThread = new CameraAiboTekThread(x, aiboPortInUse);
	    	        		System.out.println("Aibo Vision Thread started " + aiboPortInUse);
	    	        		cameraAiboThread.start();
	    	        		threadVisionAibo = true;
	    	        	}
	    	        	
					}
	    	    });
	    		buttons.add(robotButtons[i]);
    		}

    		else {
    		grabButtons[i] = new JButton("Lock");
    		grabButtons[i].setFont(new Font("SansSerif", Font.PLAIN, 9));
    		grabButtons[i].setPreferredSize(new Dimension(Width/3 + 15, 25));  // 3 15
    		grabButtons[i].setVerticalTextPosition(AbstractButton.TOP);
    		grabButtons[i].setHorizontalTextPosition(AbstractButton.CENTER);
    		grabButtons[i].addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				Gui.serverComm.writeStream("LOCK " + x.getUniqueId());
    				Robot.setRobotInUse(x.getRobotKey());
    				jl.setText(x.getName());
//    				if(Gui.serverComm.sendReqGrab()){
//    					System.out.println(x.getName() + " " + Robot.getRobotInUse());
//	    	        	Robot.setRobotInUse(x.getRobotKey());
//	    	        	System.out.println(x.getName() + " " + Robot.getRobotInUse());
//	    	        	jl.setText(x.getName());
//	    	        	grabButtons[x.getRobotKey()].setBackground(Color.green);
//	    	        	ungrabButtons[x.getRobotKey()].setBackground(Color.red);
//    				}
//    				else{
//    					jl.setText("Robot is Locked");
//    				}
    			}
    		});
    		
    		ungrabButtons[i] = new JButton("Unlock");
    		ungrabButtons[i].setFont(new Font("SansSerif", Font.PLAIN, 9));
    		ungrabButtons[i].setPreferredSize(new Dimension(Width/3 + 15, 25)); // 3 15
    		ungrabButtons[i].setVerticalTextPosition(AbstractButton.TOP);
    		ungrabButtons[i].setHorizontalTextPosition(AbstractButton.CENTER);
    		ungrabButtons[i].addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				Gui.serverComm.writeStream("UNLOCK " + x.getUniqueId());
    				Robot.setRobotInUse(-1);
    				jl.setText("NO Robot in use");
//    				if(Gui.serverComm.sendReqUnGrab()){
//    					System.out.println(x.getName() + " " + Robot.getRobotInUse());
//	    	        	Robot.setRobotInUse(x.getRobotKey());
//	    	        	System.out.println(x.getName() + " " + Robot.getRobotInUse());
//	    	        	jl.setText(x.getName());
//    				}
//    				else{
//    					jl.setText("Robot is Locked");
//    				}
    			}
    		});
    		
    		imageButtons[i] = new JButton("Image");
    		imageButtons[i].setFont(new Font("SansSerif", Font.PLAIN, 9)); // 8
    		imageButtons[i].setPreferredSize(new Dimension(Width/3 + 15, 25)); // 3 15
    		imageButtons[i].setVerticalTextPosition(AbstractButton.TOP);
    		imageButtons[i].setHorizontalTextPosition(AbstractButton.CENTER);
    		
    		videoButtons[i] = new JButton("Video");
    		videoButtons[i].setFont(new Font("SansSerif", Font.PLAIN, 9));  // 8
    		videoButtons[i].setPreferredSize(new Dimension(Width/3 + 15, 25));  // 3 15
    		videoButtons[i].setVerticalTextPosition(AbstractButton.TOP);
    		videoButtons[i].setHorizontalTextPosition(AbstractButton.CENTER);
    		
    		// Wire the button with action listener that contains Robot key
    		
    		buttons.add(robotButtons[i]);
    		buttons.add(grabButtons[i]);
    		buttons.add(ungrabButtons[i]);
    		buttons.add(imageButtons[i]);
    		buttons.add(videoButtons[i]);
    		}
    		
    		i++;
    		
    		
        }
	    add(buttons);
	    validate();
	    
	}

	public Dimension getPreferredScrollableViewportSize() {
		return null;
	}

	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		return 0;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
		return 0;
	}

}
