package metrobotics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * @author Pablo Munoz - Metrobotics
 *
 */
public class Grid extends JPanel{
	//Path2D.Double arrow = createArrow();
	JLabel robotInUse;
	ArrayList<Robot> robots;
	int numRobots;
	Path2D.Double [] arrows;
	AffineTransform [] at;
	Shape [] shapes;
	Image [] robotImages;
	private Rectangle[] rectangles;
	Graphics2D g2;
	final int ratioPixelsMeters = 60;
	Grid(final JLabel robotInUse, final ArrayList<Robot> robots){
		super();
		this.robotInUse = robotInUse;
		this.robots = robots;
		this.numRobots = robots.size();
		setBackground(Color.white);		
		//Toolkit tk = Toolkit.getDefaultToolkit();
		int Width = 750; //(int)(tk.getScreenSize().getWidth() * 0.40); //0.80);
		int Height = 325; //(int)(tk.getScreenSize().getHeight() * 0.30); //60);
		double ratioPixelsMeters = 60;
	    Dimension d = new Dimension(Width, Height);
	    setPreferredSize(d);
	    setBorder(BorderFactory.createRaisedBevelBorder());
	    //robotImages = new Image[robots.size()];
	    //	    for(int i = 0; i<numRobots; i++){
//	    	rectangles[i] = new Rectangle(10, 10);
//	    }
		
	}
	
	protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g2 = (Graphics2D)g;
        //g2.translate(this.getWidth()/2, this.getHeight()/2); // New Origin
        //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           // RenderingHints.VALUE_ANTIALIAS_ON);
        
        
    	
        // PAblo is checking the limits for the DEMO
        g2.setStroke(new BasicStroke((float) 4));
        g2.setColor(Color.green);
        g2.drawLine(0, 0, 0, 325);  // 1 because...
        g2.drawLine(0, 325, 500, 325);
        g2.drawLine(500, 325, 500, 0);
        g2.drawLine(500, 0, 0, 0);
        g2.setStroke(new BasicStroke((float) 0.5));
        
        
        g2.setColor(Color.black);
        drawGrid(g2);
        
    	int i=0;
    	if(Gui.debug){
    		System.out.println("In Grid. Num of robots " + robots.size());
    	}
    	rectangles = new Rectangle[robots.size()]; // This rectangles are used to grab the robot from the map.
    	at = new AffineTransform[robots.size()];
    	for (Robot x : robots){
    		// TODO: 
    		// Rotate from center of the image. 
    		 
    		if(Gui.debug){
    			System.out.println("Repainting robots in Grid: i " + i + " x " +
    					x.getGridX() + " y " + x.getGridY() + " Theta " + x.getGridTheta() + 
    					" Confidence " + x.getConfidence());
    		}

    		at[i] = AffineTransform.getTranslateInstance(x.getGridX()*ratioPixelsMeters, (this.getHeight()-x.getGridY()*ratioPixelsMeters)); //*50);
    		at[i].rotate(-x.getGridTheta());    //Math.toRadians((-x.getGridTheta())));
    		rectangles[i] = new Rectangle((int)x.getGridX()*ratioPixelsMeters-40, (int) (this.getHeight()-x.getGridY())*ratioPixelsMeters-40, 80, 80);
    		//g2.draw(rectangles[i]);
			g2.drawImage(x.getRobotGridImage(), at[i], this);
			String name = x.getName() + " x: " + x.getGridX()/ratioPixelsMeters + " y: " + x.getGridY()/ratioPixelsMeters + " theta: " + x.getGridTheta() + " confidence: " + x.getConfidence();
			if(name!=null){
				System.out.println("Printing coordinates");
				g2.drawString(name, (int)x.getGridX()*ratioPixelsMeters + 20, (int)(this.getHeight()-(x.getGridY()*ratioPixelsMeters)) + 30);  //*50;
			}
			i++;
    	}
		//doSomething = false;
    	addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int i =0;
				for(Robot x : robots){
					if(rectangles[i].contains(e.getX(), e.getY())){
						robotInUse.setText(x.getName());
						Robot.setRobotInUse(i);
					}
					else{
						//robotInUse.setText(shapes[i].getBounds().x + " " + shapes[i].getBounds().y + " " + shapes[i].getBounds().width + " " + shapes[i].getBounds().height);
					}
					i++;
				}
			}
	     });
    	
	}
	private void drawGrid(Graphics2D g2) {
		// TODO Auto-generated method stub
		Stroke s = g2.getStroke();
		g2.setStroke(new BasicStroke((float) 0.5));
		for(int i=0; i<this.getWidth(); i++){
			g2.setColor(Color.blue);
			if(i%40==0){
				g2.drawLine(i, 0, i, this.getHeight());
			}
		}
		for(int i=0; i<this.getHeight(); i++){
			g2.setColor(Color.blue);
			if(i%40==0){
				g2.drawLine(0, i, this.getWidth(), i);
			}
		}
		// Back to old stroke
		g2.setStroke(s);
	}

	private Path2D.Double createArrow() {
        int length = 25;
        int barb = 15;
        double angle = Math.toRadians(20);
        Path2D.Double path = new Path2D.Double();
        path.moveTo(-length/2, 0);
        path.lineTo(length/2, 0);
        double x = length/2 - barb*Math.cos(angle);
        double y = barb*Math.sin(angle);
        path.lineTo(x, y);
        x = length/2 - barb*Math.cos(-angle);
        y = barb*Math.sin(-angle);
        path.moveTo(length/2, 0);
        path.lineTo(x, y);
        return path;
    }	
}