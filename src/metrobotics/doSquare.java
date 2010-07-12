package metrobotics;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;


/**
 * @author Pablo Munoz - Metrobotics 
 * TODO: This needs to be fixed. Pablo
 *
 */
public class doSquare implements MouseListener {
	
	ArrayList<Robot> robots;
	//AibosSquareThread [] aibosSq;
	doSquare(ArrayList<Robot> robots){
		this.robots = robots;
	}
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println("In Behavior Square");
		//aibosSq = new AibosSquareThread[robots.size()];
		try {
			for(int i=0; i<robots.size(); i++){
				if(robots.get(i).getHasMoveAibo()){
					System.out.println("Here");
					new AibosSquareThread(robots, i).start();
				}
			}
			for(int j=0; j<4; j++){
				for(int i=0; i<robots.size(); i++){
					if(robots.get(i).getUsesPlayer()){
						double speed = 0.5;
						robots.get(i).p2d.setSpeed(speed, 0);
						// Mark's style to ckeck if the robot has received the command:
						//while(robots.get(i).p2d.getData().getVel() < (speed-0.2)){ // WRONG!!!! FIX THIS
						//}
					}
				}
				Thread.sleep(500); 
				for(int i=0; i<robots.size(); i++){
					if(robots.get(i).getUsesPlayer()){
						robots.get(i).p2d.setSpeed(0.0, 1.7);
					}
				}
				Thread.sleep(200);  
				for(int i=0; i<robots.size(); i++){
					if(robots.get(i).getUsesPlayer()){
						robots.get(i).p2d.setSpeed(0.0, 0.0);
					}
				}
			}
		} catch (InterruptedException exc) {
			exc.printStackTrace();
		}
		return;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
}

class AibosSquareThread extends Thread{
	ArrayList<Robot> robots;
	int index;
	AibosSquareThread(ArrayList<Robot> robots, int i){
		this.robots = robots;
		this.index = i;
	}
	public void run(){
		// NOT THE BEST WAY TO DO IT!! FIX IT!!
		System.out.println("Controlling directly the Aibo in Thread");
		for(int i=0; i<4; i++){
			for(int j = 0; j<100; j++){
				robots.get(index).aiboDirect.sendCommandLegs("f", 0.5);
			}
			for(int j = 0; j<100; j++){
				robots.get(index).aiboDirect.sendCommandLegs("r", 0.2);
			}
		}
		return;
	}
}