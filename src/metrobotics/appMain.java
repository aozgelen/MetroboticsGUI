package metrobotics;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Pablo Munoz - Metrobotics
 * This class is the point of entrance of the Application. 
 * Currently, only working with the server is enabled. 
 * I am working on reading a configuration file from Player, so there is no need to hard
 * code the use of Player or directly controlling the robots.
 * TODO: Comment all the methods. Pablo.
 */
public class appMain {
	// In case we want to directly connect to Player 
	static String playerServer;
	static int portAssigned;
	
	public static void main(String[] args) throws InterruptedException {
		ArrayList<Robot> robots = new ArrayList<Robot>();
		Gui.debug = true;
		if(Gui.debug){
			for(int i = 0; i<args.length; i++){
				System.out.println(i + ": " + args[i]);
			}
		}
		if(args.length<1){
			System.out.println("Usage: appMain [option] \nOptions: \n" + 
					"-s Using Central Server [host] [port]\n-p Using Player [host] [config file]\n" + "" +
							"-c Using Hybrid Config File (Player + Direct control of Robot) [file] NOT IMPLEMENTED YET"); 
			return;
		}
		
		// Using Central Server
		else if(args[0].compareTo("-s") == 0){
			Gui.useCentralServer = true;
			Gui.centralServerIP = args[1]; 
			Gui.centralServerInitMsgPort = Integer.parseInt(args[2]); 
			
		}
		else if(args[0].compareTo("-p") == 0){
			System.out.println("Connecting directly through Player");
			playerServer = args[1];
			portAssigned = Integer.parseInt(args[2]);
			// Read configuration file (.cfg)
			robots = readConfigFile(args[3]);
		}
		// Creating the mainFrame;
		MainFrame mf = new MainFrame("Metrobotics", robots);
		if(Gui.useCentralServer){
			Gui.serverComm = new ServerComm(mf, robots);
			try {
				Thread.sleep(1000); 
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
			GuiState guiState = new GuiState();
			guiState.start();
		}
	}

	private static ArrayList<Robot> readConfigFile(String fileName) {
		ArrayList<Robot> robotList = new ArrayList<Robot>();
		System.out.println("Sorry, I am working on creating the code to read the configuration from Player");
		
		// THIS IS HARD CODED AS A PRESENTATION FOR THE CONFERENCE
		
		// File file = new File(fileName);
		// portAssigned = 6665;
		// set of parameters for Aibo1 - Bet
		//Robot aibo = new Robot.Builder(true).icon(GUIConstants.SID_AIBO).name("Aibo One").playerclient(playerServer, portAssigned).pos2D(0).camera(0).build(); //.build(); // cameraAiboTek("192.168.2.155", 10011, 0).pos2DandPTZAiboTek("192.168.2.155", 10050, 10052).
		
		//aibo.setGridX(30);
		//aibo.setGridY(100);
		//aibo.setGridTheta(90);
		
		// Aibo 2 - Rachel (160) - Other? (157)
//		portAssigned++; // = 6666;
		//Robot aibo2 = new Robot.Builder(true).icon(GUIConstants.SID_AIBO).name("Aibo Two").playerclient(playerServer, portAssigned).pos2D(0).camera(0).build(); // cameraAiboTek("192.168.2.160", 10211, 0).pos2DandPTZAiboTek("192.168.2.160", 10150, 10152).build()
		//aibo2.setGridX(160);
		//aibo2.setGridY(160);
		//aibo2.setGridTheta(45);
		
		
		// set of parameters for Surveyor
//		portAssigned = 6667;
		Robot surveyor = new Robot.Builder(true).icon(GUIConstants.SID_SURVEYOR).name("Surveyor 13").playerclient(playerServer, portAssigned).pos2D(0).camera(0).build(); // build(); //(true).playerclient(playerServer, portAssigned).pos2D(0).camera(0).build();
		surveyor.setGridX(260);
		surveyor.setGridY(160);
		surveyor.setGridTheta(45);
				
//		portAssigned = 6668;
//		Robot surveyor2 = new Robot.Builder(false).icon(ICON_SURVEYOR).name("Surveyor 15").build(); // playerclient(playerServer, portAssigned).pos2D(0).camera(0).build();

//		robotList.add(aibo);
		//robots.add(aibo2);
		robotList.add(surveyor);
//		robotList.add(surveyor2);

		return robotList;
	}
}
