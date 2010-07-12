package metrobotics;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Pablo Munoz - Metrobotics
 * This class is used when directly controlling an Aibo Robot.
 * This code is based on the code on Tekkotsu for sending commands.
 * TODO: There is the need to fix the sequence for sending commands. I tested with the
 * legs but the last command is stored, resulting in an not accurate movement after a while
 * Pablo
 */
public class AiboDirectMove {
	Socket walkSock;
	Socket headSock;
	OutputStream outWalk;
	OutputStream outHead;
	double tilt=0.0;
	double pan=0.0;
	double roll=0.0;
	double fwd = 0.0;
	double rot = 0.0;
	double str = 0.0;
	
	AiboDirectMove(String aiboHost, int walkPort, int headPort){
		try {
			walkSock = new Socket(aiboHost, walkPort);
			headSock = new Socket(aiboHost, headPort);
			walkSock.setTcpNoDelay(true);
			walkSock.setTrafficClass(0x10);
			headSock.setTcpNoDelay(true);
			headSock.setTrafficClass(0x10);
			outWalk = walkSock.getOutputStream();
			outHead = headSock.getOutputStream();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.sendCommandHead("p", 0.0);
		this.sendCommandHead("r", 0.0);
		this.sendCommandHead("t", 0.0);
		
	}
	// Send command from Tekkotsu
	public void sendCommandLegs(String command, double param) {
		
		if (outWalk == null) {
			return;
		}
		// Extract command byte
		byte cmdbytes[] = command.getBytes();
		if(cmdbytes[0]=='f')
			this.fwd=param;
		else if(cmdbytes[0]=='r')
			this.rot=param;
		else if(cmdbytes[0]=='s')
			this.str=param;

		// Construct the command sequence
		byte sequence[] = new byte[5];
		sequence[0] = cmdbytes[0];
		int pbits = Float.floatToIntBits((float) param);
		Integer i;
		i = new Integer((pbits >> 24) & 0xff); sequence[4] = i.byteValue();
		i = new Integer((pbits >> 16) & 0xff); sequence[3] = i.byteValue();
		i = new Integer((pbits >>  8) & 0xff); sequence[2] = i.byteValue();
		i = new Integer(pbits & 0xff);	   sequence[1] = i.byteValue();
		// Now write the whole command.
		try {
			this.outWalk.write(sequence, 0, 5);
		} catch(Exception e) {return; }
	
	}
	
	// Same as before but for controlling the head.
	public void sendCommandHead(String command, double param) {
		
		if (outHead == null) {
			return;
		}
		// Extract command byte
		byte cmdbytes[] = command.getBytes();
		if(cmdbytes[0]=='t')
			this.tilt=param;
		else if(cmdbytes[0]=='p')
			this.pan=param;
		else if(cmdbytes[0]=='r')
			this.roll=param;

		// Construct the command sequence
		byte sequence[] = new byte[5];
		sequence[0] = cmdbytes[0];
		int pbits = Float.floatToIntBits((float) param);
		Integer i;
		i = new Integer((pbits >> 24) & 0xff); sequence[4] = i.byteValue();
		i = new Integer((pbits >> 16) & 0xff); sequence[3] = i.byteValue();
		i = new Integer((pbits >>  8) & 0xff); sequence[2] = i.byteValue();
		i = new Integer(pbits & 0xff);	   sequence[1] = i.byteValue();
		// Now write the whole command.
		try {
			this.outHead.write(sequence, 0, 5);
		} catch(Exception e) {return; }
	}
}