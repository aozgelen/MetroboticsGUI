package metrobotics;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;

import javaclient3.*;
import javaclient3.structures.blobfinder.PlayerBlobfinderBlob;
import javaclient3.structures.blobfinder.PlayerBlobfinderData;


/**
 * @author Pablo Munoz
 * I used this class to test the connection to Player. It is NOT used anywhere else in the GUI.
 * 
 *
 */
public class PlayerCliGUI {

	public static void main(String[] args) throws InterruptedException {
		// JP: I am using this to test the client with an Aibo
		PlayerClient robot = new PlayerClient("192.168.2.4", 6665);
		Position2DInterface ppd = robot.requestInterfacePosition2D(0, robot.PLAYER_OPEN_MODE);
		BlobfinderInterface blobs = robot.requestInterfaceBlobfinder(0, robot.PLAYER_OPEN_MODE);
		PlayerBlobfinderData blodata;
		int blobcounts;
		PlayerBlobfinderBlob [] blobArr;
		BlobJPanel show = new BlobJPanel();
		
		while(true){
			robot.readAll();
			//ppd.setSpeed(0.5, 0);
			if(blobs.isDataReady()){
				blodata = blobs.getData();
				blobcounts = blodata.getBlobs_count();
				System.out.println(blobcounts);
				blobArr = new PlayerBlobfinderBlob[blobcounts];
				blobArr = blodata.getBlobs();
				Rectangle [] rect = new Rectangle[blobcounts];
				int i =0;
				for(PlayerBlobfinderBlob x : blobArr){
					rect[i] = new Rectangle(x.getLeft(), x.getTop(), x.getRight() - x.getLeft(), x.getBottom() - x.getTop());
					     i++;
				}
			}
		}
	}
}
class BlobJPanel extends JPanel{
	
	BlobJPanel(){
		Dimension d = new Dimension(80, 64);
		
		
	}
	
	public void paintBlobs(Rectangle [] rects){
		
	}
}