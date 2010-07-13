package metrobotics;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

/**
 * @author Pablo Munoz - Metrobotics
 * This class is in charge of the main Frame.
 * TODO: Improve the Layout and Scroll Panes 
 * Pablo. 
 *
 */
public class MainFrame extends JFrame{
	RobotSelector robotSel;
	Grid grid;
	volatile ArrayList<Robot> robots;
	PlayerJoy playerJoy;
	JLabel RobotInUseLabel;
	VisionDisplay vision;
	Title metroTitle;
	Behaviors behaviors;
	GridBagConstraints c;
	Container mainContent;
	JTextField userMsg;
	JLabel guiId;
	JLabel messageFromServer;
	JScrollPane scrollPane;
	JScrollBar bar;
	
	MainFrame(String title, ArrayList<Robot> robots){
		super(title);
		//Toolkit tk = Toolkit.getDefaultToolkit();
	    //Dimension d = tk.getScreenSize();
		Dimension d = new Dimension(1050, 750);
	    //d.setSize(d.getWidth(), d.getHeight()-50);
	    setMinimumSize(d);
	    //setLayout(new FlowLayout()); // For testing purposes
	    this.robots = robots;
	 
	    mainContent = this.getContentPane();
	    mainContent.setLayout(new GridBagLayout());
	    c = new GridBagConstraints();
	    
	    c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 0;
	    metroTitle = new Title();
	    mainContent.add(metroTitle, c);
	    
	    c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 1;
	    RobotInUseLabel = new JLabel();
	    Dimension dim  = new Dimension(200, 40);
	    RobotInUseLabel.setPreferredSize(dim);
	    RobotInUseLabel.setHorizontalAlignment(SwingConstants.CENTER);
		RobotInUseLabel.setText("No Robot In Use");
		
		mainContent.add(RobotInUseLabel, c);
	    
		vision = new VisionDisplay(robots);
		
		robotSel = new RobotSelector(RobotInUseLabel, robots, vision);
		
		scrollPane = new JScrollPane(robotSel);
		scrollPane.setPreferredSize(new Dimension(120, 600));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getViewport().setPreferredSize(new Dimension(130, 1000));
//		bar = scrollPane.getVerticalScrollBar();
//		bar.addAdjustmentListener(new AdjustmentListener(){
//			public void adjustmentValueChanged(AdjustmentEvent e) {
//				scrollPane.repaint();
//			}
//			
//		});
//		scrollPane.validate();
		
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		c.gridheight = 5;
		c.gridx = 2;
		c.gridy = 1;
		mainContent.add(scrollPane, c); // scrollPane, c); // robotSel, c); // robotSel, c);
		
		
		behaviors = new Behaviors(robots);
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 2;
		c.gridheight = 4;
		c.gridx = 0;
		c.gridy = 2;
		mainContent.add(behaviors, c);
		
		
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 4;
		c.gridheight = 4;
		c.gridx = 3;
		c.gridy = 0;
		grid = new Grid(RobotInUseLabel, robots);
		mainContent.add(grid, c);
	
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		c.gridheight = 2;
		c.gridx = 3;
		c.gridy = 4;
	    mainContent.add(vision, c);
	    
	    c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		c.gridheight = 2;
		c.gridx = 5;
		c.gridy = 4;
		playerJoy = new PlayerJoy(RobotInUseLabel, robots, grid);
		mainContent.add(playerJoy, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 7;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 7;
		JPanel message = new JPanel();
		Dimension dimen = new Dimension(1100, 40);
		message.setPreferredSize(dimen);
		message.setBackground(Color.white);
		if(Gui.useCentralServer){
			userMsg = new JTextField();
			userMsg.setText("Message to Central Server");
			userMsg.setPreferredSize(new Dimension(100, 25));
			message.add(userMsg);
			JButton sendServer = new JButton("Send to Server");
			sendServer.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					sendString(); 
				}
				
			});
	
			JButton reqGlobalState = new JButton("Request Global State");
			reqGlobalState.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String ident = "IDENT";
					Gui.serverComm.writeStream(ident);
					
					// This is working
					//updateRobots();
					//mainContent.remove(robotSel);
					//mainContent.remove(grid);
					//mainContent.validate();
					//newRobotSelAndGrid();
				}
			});
			guiId = new JLabel("GUI ID :" + Gui.getGUIId());
			messageFromServer = new JLabel("Last Message from Server: EMPTY");
			
			message.add(sendServer);
			message.add(reqGlobalState);
			message.add(guiId);
			message.add(messageFromServer);
		}
		mainContent.add(message, c);
		
		mainContent.validate();
		
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // JP: Before closing you should close all the connections and databases your are using
	    //setResizable(false);
	    setVisible(true);
		pack();
	}
	private void sendString(){
		Gui.serverComm.writeStream(userMsg.getText());
	}
//	private void updateRobots(){
//		robots = Gui.serverComm.requestGlobalState();
//		System.out.println("Success");
//	}
	public void newRobotSelAndGrid(){
		this.remove(scrollPane);
		scrollPane.removeAll();
		
		robotSel = new RobotSelector(RobotInUseLabel, robots, vision);
		scrollPane = new JScrollPane(robotSel);
		scrollPane.setPreferredSize(new Dimension(120, 600));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setViewportView(robotSel);
		scrollPane.getViewport().setPreferredSize(new Dimension(130, 900));
		scrollPane.validate();
//		robotSel.revalidate();
//		scrollPane = new JScrollPane(robotSel);
//		scrollPane.setPreferredSize(new Dimension(150, 600));
//		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
//		
//		scrollPane.getViewport().setPreferredSize(new Dimension(130, 900));
//		bar = scrollPane.getVerticalScrollBar();
//		bar.addAdjustmentListener(new AdjustmentListener(){
//			public void adjustmentValueChanged(AdjustmentEvent e) {
//				scrollPane.repaint();
//			}
//			
//		});
//		scrollPane.validate();
		
		
//		System.out.println(robots.size());
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		c.gridheight = 5;
		c.gridx = 2;
		c.gridy = 1;
		mainContent.add(scrollPane, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 4;
		c.gridheight = 4;
		c.gridx = 3;
		c.gridy = 0;
		grid = new Grid(RobotInUseLabel, robots);
		mainContent.add(grid, c);
		
		mainContent.validate();
	}
}
