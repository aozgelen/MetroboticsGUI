package metrobotics;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;


/**
 * @author Pablo Munoz
 * This class is used only when we are directly getting images from an Aibo Robot.
 * The thread is started when an Aibo Robot is selected and the GUI has the ip and port for
 * the camera.
 *
 */
public class CameraAiboTekThread extends Thread {
	
	private BufferedImage imgJP;
	
	// Variables from Listener
	protected long bytesRead = 0;
	protected long bytesWritten = 0;
	private boolean countersEnabled = true; 
  
	public int _port;
	public String _host;
	public boolean _isConnected;
	public volatile Thread _listenerThread;
	public volatile boolean destroy=false;
	public int _frametimer_numframes=0;
	public long _frametimer_timer=System.currentTimeMillis();
	public static final int PACKET_TEXT=0;
  	public static final int PACKET_VISIONRAW_HALF=1;
  	public static final int PACKET_VISIONRAW_FULL=2;
  	public static final int PACKET_VISIONRAW_YFULL_UVHALF=3;
  	public static final int PACKET_VISIONRAW_Y_ONLY=4;
  	public static final int PACKET_VISIONRAW_Y_LH_ONLY=5;
  	public static final int PACKET_VISIONRAW_Y_HL_ONLY=6;
  	public static final int PACKET_VISIONRAW_Y_HH_ONLY=7;
  	public static final int PACKET_VISIONRAW_U_ONLY=8;
  	public static final int PACKET_VISIONRAW_V_ONLY=9;
  	public static final int PACKET_VISIONRLE_FULL=10;
  	public static final int PACKET_WORLDSTATEJOINTS=11;
  	public static final int PACKET_WORLDSTATEPIDS=12;
  	public static final int PACKET_WORLDSTATEBUTTONS=13;
  	public static final int PACKET_WMCLASS=14;
  	
  	// Variables from UDPListener
  	byte[] incomingbuf = new byte[1<<16];
    DatagramPacket incoming = new DatagramPacket(incomingbuf, incomingbuf.length);

    byte[] buf = (new String("connection request")).getBytes();

  	int _lastPort=-1; // keep track of previously used port number so we can resume connections
    DatagramSocket _socket;
    
    // Variables from VisionListener
    public static final int ENCODE_COLOR=0;
	public static final int ENCODE_SINGLE_CHANNEL=1;
	public static final int COMPRESS_NONE=0;
	public static final int COMPRESS_JPEG=1;
	public static final int CHAN_Y=0;
	public static final int CHAN_U=1;
	public static final int CHAN_V=2;
	public static final int CHAN_Y_DY=3;
	public static final int CHAN_Y_DX=4;
	public static final int CHAN_Y_DXDY=5;
	// ImageReader was static
	ImageReader jpegReader=(ImageReader)ImageIO.getImageReadersByFormatName("jpeg").next();
	
	final static int DEFAULT_WIDTH=176;
	final static int DEFAULT_HEIGHT=144;
	static int defRawPort=10011;
	static int defRLEPort=10012;
    static int defRegionPort=10013;
    
    // Variables from UDPVisionListener
	byte[] colormap = new byte[256*3];
    
    InputStream in;
  	boolean updatedFlag;
  	Date timestamp;
  	long frameNum=0;

  	//protected Vector listeners = new Vector();
  	protected boolean updating;
  	
	int channels=3;
	int width=DEFAULT_WIDTH;
	int height=DEFAULT_HEIGHT;
	int pktSize=width*height*channels;
	int oldformat=PACKET_VISIONRAW_FULL;
	int format;
	int compression;
	int chan_id;

	byte[] _data=new byte[pktSize];
	byte[] _outd=new byte[pktSize];
	byte[] _tmp=new byte[pktSize*2];
	byte[] _jpeg=new byte[pktSize*2];
	byte[] _newjpeg=new byte[pktSize*2];
	String sensors;
	boolean isJPEG=false;
	int jpegLen=0;
	int newjpegLen=0;
	boolean isIndex=false;
	boolean badCompressWarn=false;
	int[] _pixels=new int[width*height];
	BufferedImage img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
	boolean convertRGB=true;
	IndexColorModel cmodel;
	//File file;
	Robot inUse;
	
	int newWidth;
	int newHeight;
	String fmt;
	long timest;
	

//	CameraAiboTekThread(String host, int port){
//		this._host = host;
//		this._port = port;
//		System.out.println(_host + " " + _port);
//	}
	
	CameraAiboTekThread(Robot inUse, int port){
		this.inUse = inUse;
		this._host = inUse.getAiboHost();
		this._port = port;
		System.out.println(_host + " " + _port + " " + inUse.getName());
		try {
			Thread.sleep(500); // This sleep time should come also from the configuration file. 
		} catch (InterruptedException exc) {
			exc.printStackTrace();
			System.out.println("Thread terminated");
			return;
		}
	}
	
	public static void main(String[] args) {
		// This needs to be fixed.
		//CameraAiboTekThread aiboIm = new CameraAiboTekThread("192.168.2.155", defRawPort);
		//aiboIm.start();

	}
	
	public void run() {
		// From Listener
		_isConnected = false;
		
		// From VisionPanel
		boolean useUDP=true;
		
		try {
			_socket=new DatagramSocket(_port);
			_socket.connect(InetAddress.getByName(_host), _port);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// send a dummy message so that the AIBO can see what
		// address to connect its UDP socket to

		DatagramPacket message = new DatagramPacket(buf, buf.length);
		
		try {
			_socket.setSoTimeout(500);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		while(!destroy && Robot.getAiboCameraThread()) {
			try {
				_socket.send(message);
				_socket.receive(incoming);
			} catch (IOException e) {

				e.printStackTrace();
                return;
			}
			break;
		}
		try {
			_socket.setSoTimeout(0);
		} catch (SocketException e1) {
			e1.printStackTrace();
		} 
		System.out.println("["+_port+"] connected ...");
		
		_isConnected=true;
		
		// Not using fireConnected();
		
		while(!_socket.isClosed() && Robot.getAiboCameraThread()) { // JP: added this variable so we can terminate the thread
			in = new ByteArrayInputStream(incoming.getData());
			String type = null;
			try {
				type = readLoadSaveString(in);
			} catch (IOException e1) {
				e1.printStackTrace();
				// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
				System.out.println("Recovering...");
				_socket.close();
				return;
				// End of Addition.
			}
			//System.out.println("Line 107"); //UDPVisionListener: connected() type is: " + type + " obtained by calling readLoadSaveString(in)");
			//if(!_isConnected) break; //System.out.println("Got type="+type);
			if(!type.equals("TekkotsuImage")) {
				System.out.println("Line 110"); //UDPVisionListener: connected type is not equal to TekkotsuImage");
				if(type.startsWith("#POS\n")) {
					System.out.println("UDPVisionListener: connected(), type starts with #POS");
					sensors=type;
					//System.out.println("got sensors");
					System.out.println("Line 115"); //UDPVisionListener: connected(), sensors = type and calls fireSensorUpdate()");
					// fireSensorUpdate();
					//if(destroy)
						//break;
					try {
						//mysock.receive(incoming);
						_socket.receive(incoming);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
						System.out.println("Recovering...");
						_socket.close();
						return;
						// End of Addition.
					}
					continue;
				} //else if(!type.equals("CloseConnection"))
					//System.err.println("Unrecognized type: "+type);
				//else {
					//System.out.println("Got Close connection packet");
					//_isConnected=false;
				//}
				//break;
			}
			try {
				format=readInt(in);
			//if(!_isConnected) break; //
			//System.out.println("Got format="+format);
				compression=readInt(in);
			//if(!_isConnected) break; //
			//System.out.println("Got compression="+compression);
				newWidth=0;
				newWidth = readInt(in);
			//if(!_isConnected) break; //
			//System.out.println("Got newWidth="+newWidth);
				newHeight=0;
				newHeight = readInt(in);
			//if(!_isConnected) break; //
			//System.out.println("Got newHeight="+newHeight);
				timest = 0;
				timest = readInt(in);
				if(timest<0)
					timest+=(1L<<32);
				//if(!_isConnected) break; //
				//System.out.println("Got timest="+timest);
				frameNum=readInt(in);
				if(frameNum<0)
					frameNum+=(1L<<32);
				//if(!_isConnected) break; //
				//System.out.println("Got frameNum="+frameNum);
			} catch (IOException e7) {
				// TODO Auto-generated catch block
				e7.printStackTrace();
				// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
				System.out.println("Recovering...");
				_socket.close();
				return;
				// End of Addition.
			}
			
			if (format!=oldformat || newWidth!=width || newHeight!=height) {
				//System.out.println("Format is different than old format. newWidth and newHeight set");
				width=newWidth;
				height=newHeight;
				synchronized (_outd) {
					switch (format) {
					case ENCODE_COLOR:
						channels=3;
						pktSize=width*height*channels;
						break;
					case ENCODE_SINGLE_CHANNEL:
						channels=1;
						pktSize=width*height*channels;
						break;
					default:
						System.err.println("VisionRawListener: unknown packet type "+format);
						try {
							throw new java.lang.NoSuchFieldException("fake exception");
						} catch (NoSuchFieldException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
							System.out.println("Recovering...");
							_socket.close();
							return;
							// End of Addition.
						}
					}
					_data=new byte[pktSize];
					_outd=new byte[pktSize];
					_tmp=new byte[pktSize];
					_jpeg=new byte[pktSize*2<2000?2000:pktSize*2];
					_newjpeg=new byte[pktSize*2<2000?2000:pktSize*2];
					_pixels=new int[width*height];;
					img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
					// JP Comment
					//System.out.println("JP: Image acquired");
					//try {
					    // Save as JPEG
					    // file = new File("empty.jpg");
					    //ImageIO.write(img, "jpg", file);
					//} catch (IOException e) {
					//}
					oldformat=format;
				}
			}
			
			// JP: Let see if this works: 
			imgJP=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
			imgJP = getImage();
			
			//System.out.println("JP: Image acquired!!! The super image");
			inUse.cameraImage = imgJP;
			//try {
				//String name = "Frame" + frameNum;
			    // Save as JPEG
			    //file = new File(name +".jpg");
			    //ImageIO.write(imgJP, "jpg", file);
			//} catch (IOException e) {
			//}
			// End of JP test
			
			
			boolean failed=false;
			for(int i=0; i<channels; i++) {
				String creator = null;
				try {
					creator = readLoadSaveString(in);
				} catch (IOException e6) {
					// TODO Auto-generated catch block
					e6.printStackTrace();
					// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
					System.out.println("Recovering...");
					_socket.close();
					return;
					// End of Addition.
				}
				//if(!_isConnected) break; //
				////System.out.println("Got creator="+creator);
				if(!creator.equals("FbkImage")) {
					//System.err.println("Unrecognized creator: "+creator);
					//failed=true; break;
				} else {
					int chanwidth = 0;
					try {
						chanwidth = readInt(in);
					} catch (IOException e5) {
						// TODO Auto-generated catch block
						e5.printStackTrace();
						// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
						System.out.println("Recovering...");
						_socket.close();
						return;
						// End of Addition.
					}
					//if(!_isConnected) break; //
					////System.out.println("Got chanwidth="+chanwidth);
					int chanheight = 0;
					try {
						chanheight = readInt(in);
						//if(!_isConnected) break; //
						//System.out.println("Got chanheight="+chanheight);
					
						if(chanwidth>width || chanheight>height) {
							System.err.println("channel dimensions exceed image dimensions");
							//failed=true; break;
						}
					
						int layer = 0;
						layer = readInt(in);
						//if(!_isConnected) break; //
						//System.out.println("Got layer="+layer);
						chan_id=readInt(in);
						//if(!_isConnected) break; //
						//System.out.println("Got chan_id="+chan_id);
			
						fmt = null;
						fmt = readLoadSaveString(in);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
						System.out.println("Recovering...");
						_socket.close();
						return;
						// End of Addition.
					}
					//if(!_isConnected) break; //
					//System.out.println("Got fmt="+fmt);
					if(fmt.equals("blank")) {
						isJPEG=false;
						isIndex=false;
						int useChan=(channels==1)?i:chan_id;
						int off=useChan;
						for(int y=0; y<height; y++)
							for(int x=0; x<width; x++) {
								_data[off]=(byte)(convertRGB?0x80:0);
								off+=channels;
							}
					} else if(fmt.equals("RawImage")) {
						isJPEG=false;
						isIndex=false;
						int useChan=(channels==1)?i:chan_id;
						try {
							if(!readChannel(in,useChan,chanwidth,chanheight)) { failed=true; //System.out.println("UDPVisionListener channel read failed"); 
							break; }
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
							System.out.println("Recovering...");
							_socket.close();
							return;
							// End of Addition.
						}
					} else if(fmt.equals("JPEGGrayscale")) {
						isIndex=false;
						int useChan=(channels==1)?i:chan_id;
						try {
							if(!readJPEGChannel(in,useChan,chanwidth,chanheight)) { failed=true; //System.out.println("UDPVisionListener JPEGGreyscale channel read failed"); 
							//break; }
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
							System.out.println("Recovering...");
							_socket.close();
							return;
							// End of Addition.
						}
						isJPEG=(channels==1);
					} else if(fmt.equals("JPEGColor")) {
						isIndex=false;
						if(format==ENCODE_SINGLE_CHANNEL)
							System.err.println("WTF? ");
						try {
							if(!readJPEG(in,chanwidth,chanheight)) { failed=true; //System.out.println("UDPVisionListener JPEGColor channel read failed"); 
							break; }
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
							System.out.println("Recovering...");
							_socket.close();
							return;
							// End of Addition.
						}
						i=channels;
						isJPEG=true;
					} else if(fmt.equals("SegColorImage")) {
						isJPEG=false;
						isIndex=true;
						try {
							if(!readIndexedColor(in,chanwidth,chanheight)) { failed=true; //System.out.println("UDPVisionListener SegColor read failed"); 
							break; }
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
							System.out.println("Recovering...");
							_socket.close();
							return;
							// End of Addition.
						}
					} else if(fmt.equals("RLEImage")) {
						isJPEG=false;
						isIndex=true;
						try {
							if(!readRLE(in,chanwidth,chanheight)) { failed=true; //System.out.println("UDPVisionListener RLEImage read failed"); 
							//break; }
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
							System.out.println("Recovering...");
							_socket.close();
							return;
							// End of Addition.
						}
					} else if(fmt.equals("RegionImage")) {
						isJPEG=false;
						isIndex=true;
						try {
							if(!readRegions(in,chanwidth,chanheight)) { 
								failed=true; //System.out.println("UDPVisionListener RegionImage read failed"); 
								//break; }
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
							System.out.println("Recovering...");
							_socket.close();
							return;
							// End of Addition.
						}
					} else {
						isJPEG=false;
						isIndex=false;
						//System.out.println("Unrecognized format: "+fmt);
						failed=true; break;
					}
				}
			}
			if(failed || !_isConnected) {
				//System.out.println("UDPVisionListener connection lost");
				break;
			}
			
			synchronized(_outd) {
				byte[] temp=_data;
				_data=_outd;
				_outd=temp;
				if(isJPEG) {
					temp=_newjpeg;
					_newjpeg=_jpeg;
					_jpeg=temp;
					int tempi=newjpegLen;
					newjpegLen=jpegLen;
					jpegLen=tempi;
				}
				timestamp=new Date(timest);
			}
			updatedFlag=true;
			// fireVisionUpdate();
			if(destroy){ //close requested
				close(); //try to request final sensor frame again (might've dropped packet)
                                return;
                        }
                        try {
				_socket.receive(incoming);
			} catch (IOException e) {
				e.printStackTrace();
				// JP: Addition 06/18/2010. There should also be a message to the user about this problem.
				System.out.println("Recovering...");
				_socket.close();
				return;
				// End of Addition.
			}
		}
		
		System.out.println("Ended Aibo Camera Thread");
		_socket.close();
		return; // JP: Added to end the Thread. 06/17/2010
		
	}
	
	public String readLoadSaveString(InputStream in) throws java.io.IOException {
		////System.out.println("Line 432"); // UDPVisionListener: readLoadSaveString, line 111");
		int creatorLen=readInt(in);
		if(!_isConnected) {
			//System.out.println("Line 435. It is not connected"); //UDPVisionListener: readLoadSaveString, line 111 if !isConnected");
			return ""; ////System.out.println("creatorLen=="+creatorLen);
		}
		String creator=new String(readBytes(in,creatorLen));
		if(!_isConnected) {
			//System.out.println("JP: It is not connected");
			return "";
		}
		if(readChar(in)!='\0') {
			//System.out.println("Line 441"); //UDPVisionListener: readLoadSaveString, line 120 readChar(in)!='0'");
			System.err.println("Misread LoadSave string? len="+creatorLen+" str="+creator);
			Throwable th=new Throwable();
			th.printStackTrace();
		}
		return creator;
	}
  	
  	public int readInt(InputStream in) throws IOException {
		int read=0;
		int last=0;
		byte[] buf=readBytes(in, 4);
		return (b2i(buf[3])<<24) | (b2i(buf[2])<<16) |
					 (b2i(buf[1])<< 8) | b2i(buf[0]);
	}
  	
  	public byte[] readBytes(InputStream in, int bytes) throws IOException {
  	    byte[] ret=new byte[bytes];
  	    readBytes(ret, in, bytes);
  	    return ret;
  	}
  	
  	public void readBytes(byte[] buf, InputStream in, int bytes) throws IOException {
		int read=0;
		int last=0;
		while (read<bytes) {
			last=in.read(buf, read, bytes-read);
			if (last == -1) {
				//System.out.println("Line 500: last == -1 _isConnected set to false");
				_isConnected = false;
				break;
			}
			read+=last;
			if (isReadWriteCountersEnabled()) {
				bytesRead += last;
			}
		}
	}
  	public boolean isReadWriteCountersEnabled() {
		return countersEnabled;
	}
  	public char readChar(InputStream in) throws IOException {
		if (isReadWriteCountersEnabled()) {
			bytesRead++;
		}
		int c=in.read();
		if(c==-1){
			//System.out.println("Line: 518 c==-1 _isConnected set to false");
			_isConnected=false;
		}
		return (char)c;
	}
	public int b2i(byte b) { return (b>=0)?(int)b:((int)b)+256; }
	
	public boolean readChannel(InputStream in, int c, int chanW, int chanH) throws java.io.IOException {
		////System.out.println("Line 493"); //UDPVisionListener: readChannel, line 129");
		readBytes(_tmp,in,chanW*chanH);
		if(!_isConnected) return false;
		return upsampleData(c,chanW,chanH);
	}
	public boolean upsampleData(int c, int chanW, int chanH) {
		////System.out.println("Line 498"); // UDPVisionListener: upsampleData, line 135");
		if(chanW==width && chanH==height) {
			//////System.out.println("Line 501"); //UDPVisionListener: upsampleData, line 137 if statement");
			//special case: straight copy if image and channel are same size
			for(int y=0;y<height;y++) {
				int datarowstart=y*width*channels+c;
				int tmprowstart=y*chanW;
				for(int x=0;x<width;x++)
					_data[datarowstart+x*channels]=_tmp[tmprowstart+x];
			}
			return true;
		}
				float xsc=(chanW)/(float)(width);
		float ysc=(chanH)/(float)(height);
		int xgap=Math.round(1.0f/xsc);
		int ygap=Math.round(1.0f/ysc);
		for(int y=0;y<height-ygap;y++) {
			int datarowstart=y*width*channels+c;
			float ty=y*ysc;
			int ly=(int)ty; //lower pixel index
			float fy=ty-ly; //upper pixel weight
			int tmprowstart=ly*chanW;
			for(int x=0;x<width-xgap;x++) {
				float tx=x*xsc;
				int lx=(int)tx; //lower pixel index
				float fx=tx-lx; //upper pixel weight

				float lv=(_tmp[tmprowstart+lx]&0xFF)*(1-fx)+(_tmp[tmprowstart+lx+1]&0xFF)*fx;
				float uv=(_tmp[tmprowstart+lx+chanW]&0xFF)*(1-fx)+(_tmp[tmprowstart+lx+1+chanW]&0xFF)*fx;
				_data[datarowstart+x*channels]=(byte)(lv*(1-fy)+uv*fy);
			}
			for(int x=width-xgap;x<width;x++) {
				float lv=(_tmp[tmprowstart+chanW-1]&0xFF);
				float uv=(_tmp[tmprowstart+chanW-1+chanW]&0xFF);
				_data[datarowstart+x*channels]=(byte)(lv*(1-fy)+uv*fy);
			}
		}
		for(int y=height-ygap;y<height;y++) {
			int datarowstart=y*width*channels+c;
			int tmprowstart=chanW*(chanH-1);
			for(int x=0;x<width-xgap;x++) {
				float tx=x*xsc;
				int lx=(int)tx; //lower pixel index
				float fx=tx-lx; //upper pixel weight

				float lv=(_tmp[tmprowstart+lx]&0xFF)*(1-fx)+(_tmp[tmprowstart+lx+1]&0xFF)*fx;
				_data[datarowstart+x*channels]=(byte)(lv);
			}
			for(int x=width-xgap;x<width;x++)
				_data[datarowstart+x*channels]=_tmp[tmprowstart+chanW-1];
		}
		
		return true;
	}
	public boolean readJPEGChannel(InputStream in, int c, int chanW, int chanH) throws java.io.IOException {
		////System.out.println("Line 554"); // UDPVisionListener: readJPEGChannel, line 238");
		int len=readInt(in);
		newjpegLen=len;
		//////System.out.println("len="+len);
		if(!_isConnected) return false;
		if(len>=_newjpeg.length) {
			////System.out.println("Not enough tmp room");
			return false;
		}
		readBytes(_newjpeg,in,len);
		if(!_isConnected) return false;
		if(len>chanW*chanH*channels) {
			if(!badCompressWarn) {
				badCompressWarn=true;
				////System.out.println("Compressed image is larger than raw would be... :(");
			}
		} else {
			if(badCompressWarn) {
				badCompressWarn=false;
				////System.out.println("...ok, compressed image is smaller than raw now... :)");
			}
		}

		try {
			////System.out.println("Line 606: In readJPEGChannel");
			ImageInputStream jpegStream=new MemoryCacheImageInputStream(new ByteArrayInputStream(_newjpeg));
			jpegReader.setInput(jpegStream); 
			Raster decoded=jpegReader.readRaster(0,null);
			int off=c;
			for(int y=0; y<chanH; y++)
				for(int x=0; x<chanW; x++) {
					_data[off]=(byte)decoded.getSample(x,y,0);
					off+=channels;
				}
		} catch(Exception ex) { ex.printStackTrace(); }
		return true;
	}
	public boolean readJPEG(InputStream in, int chanW, int chanH) throws java.io.IOException {
		////System.out.println("Line 591 "); //UDPVisionListener: readJPEG, line 276");
		int len=readInt(in);
		newjpegLen=len;
		//////System.out.println("len="+len);
		if(!_isConnected) return false;
		if(len>=_newjpeg.length) {
			////System.out.println("Not enough tmp room");
			return false;
		}
		readBytes(_newjpeg,in,len);
		if(!_isConnected) return false;
		if(len>chanW*chanH*channels) {
			if(!badCompressWarn) {
				badCompressWarn=true;
				////System.out.println("Compressed image is larger than raw would be... :(");
			}
		} else {
			if(badCompressWarn) {
				badCompressWarn=false;
				////System.out.println("...ok, compressed image is smaller than raw now... :)");
			}
		}

		try {
			////System.out.println("Line 644: In readJPEG");
			ImageInputStream jpegStream=new MemoryCacheImageInputStream(new ByteArrayInputStream(_newjpeg));
			jpegReader.setInput(jpegStream); 
			Raster decoded=jpegReader.readRaster(0,null);
			
			// JP: I got the jpeg here! 
			
			
			int off=0;
			for(int y=0; y<chanH; y++)
				for(int x=0; x<chanW; x++) {
					_data[off++]=(byte)decoded.getSample(x,y,0);
					_data[off++]=(byte)decoded.getSample(x,y,1);
					_data[off++]=(byte)decoded.getSample(x,y,2);
				}
		} catch(Exception ex) { ex.printStackTrace(); }
		
		
		
		return true;
	}
	public boolean readIndexedColor(InputStream in, int chanW, int chanH) throws java.io.IOException {
		////System.out.println("Line 629"); // UDPVisionListener: readIndexedColor, line 344");
		readBytes(_data,in,chanW*chanH);
		if(!_isConnected) return false;
		if(!readColorModel(in)) return false;
		if(!_isConnected) return false;
		isIndex=true;
		return true;
	}
	public boolean readRLE(InputStream in, int chanW, int chanH) throws java.io.IOException {
		////System.out.println("Line 638: In readRLE" + _isConnected); // UDPVisionListener: readJPEG, line 354");
		int len=readInt(in);
		if(!_isConnected) return false;
		readBytes(_tmp,in,len*5);
		if(!_isConnected) return false;

		int dpos=0;
		int curx=0, cury=0;
		for (; len>0 && cury<chanH;) {
			byte color=_tmp[dpos++];
			int x=((int)_tmp[dpos++]&0xFF);
			x|=((int)_tmp[dpos++]&0xFF)<<8;
			int rlen=((int)_tmp[dpos++]&0xFF);
			rlen|=((int)_tmp[dpos++]&0xFF)<<8;
			//////System.out.println(color + " "+x + " "+rlen);
			len--;
			if (x < curx)
				return false;
			
			for (; curx < x; curx++)
				_data[cury*width+curx]=0;
			
			if (curx+rlen>width)
				return false;
			
			for (; rlen>0; rlen--, curx++)
				_data[cury*width+curx]=color;
			if (curx==width) {
				cury++;
				curx=0;
			}
		}
		if(!readColorModel(in)) return false;
		if(!_isConnected) return false;
		isIndex=true;
		return true;
	}
	public boolean readRegions(InputStream in, int chanW, int chanH) throws java.io.IOException {
		//clear the _data array
		for(int h=0; h<chanH; h++) {
			for(int w =0; w<chanW; w++) {
				_data[chanW*h+w]=0;
			}
		}
  
		int numColors=readInt(in);
		if(!_isConnected) return false;
		for(int curColor = 0; curColor < numColors; curColor++) {
			int numRegions = readInt(in);
			if(!_isConnected) return false;
            
			readBytes(_tmp,in,36*numRegions);
			if(!_isConnected) return false;
            	
			int dpos=0;
			for (int i = 0; i<numRegions; i++) {
				byte color= _tmp[dpos];
				dpos +=4;
				int x1 = byteToInt(_tmp,dpos);
				dpos +=4;
				int y1 = byteToInt(_tmp,dpos);
				dpos +=4;
				int x2 = byteToInt(_tmp,dpos);
				dpos +=4;
				int y2 = byteToInt(_tmp,dpos);
				//The  data of the centroid, area and run_start are ignored
				dpos +=20; //isn't nessescary, but now it nicely adds up to 36
				if (  x1 > chanW || y1 > chanH || x2 > chanW || y2 > chanH
							|| x1 > x2 || y1 > y2
							|| x1 < 0 || x2 < 0 || y1 < 0 || y2 < 0
							)
					return false;
				
				//Fill the data array with the bounding boxes..
				//..the top and bottom lines
				for (int tb = x1; tb <= x2; tb++) {
					_data[y1*width+tb]=color;
					_data[y2*width+tb]=color;
				}
				//..the left and right lines
				for (int lr = y1; lr <= y2; lr++) {
					_data[lr*width+x1]=color;
					_data[lr*width+x2]=color;
				}
			}
			readBytes(_tmp,in,12); //read out the min_area, total_area and merge_threshhold and ignore them:)
		}
		if(!readColorModel(in)) return false;
		if(!_isConnected) return false;
		isIndex=true;
		return true;
	}
	public void close() {
		////System.out.println("Line 731"); // UDPVisionListener: close, line 756");
		//if(mysock==null || mysock.isClosed())
		if(_socket==null || _socket.isClosed())
			return;
		destroy=true;
		byte[] buf = (new String("refreshSensors\n")).getBytes();
		try {
			//mysock.send(new DatagramPacket(buf,buf.length));
			_socket.send(new DatagramPacket(buf,buf.length));
		} catch(Exception e) { e.printStackTrace(); }
		//wait until the final sensors comes in so if user hits save image we can save the corresponding sensors too
	}
	public boolean readColorModel(InputStream in) throws java.io.IOException {
		////System.out.println("Line 742: In readColorModel"); // UDPVisionListener: readColorModel, line 316");
		int len=readInt(in);
		//////System.out.println("len="+len);
		if(!_isConnected) return false;
		readBytes(colormap,in,len*3);
		if(!_isConnected) return false;
		//we'll do this stupid thing because we can't change an existing color model, and we don't want to make a new one for each frame
		// (btw, java is stupid)
		boolean makeNew=false;
		if(cmodel==null || len!=cmodel.getMapSize()) {
			makeNew=true;
		} else {
			int off=0;
			for(int i=0; i<len; i++) {
				if((byte)cmodel.getRed(i)!=colormap[off++] || (byte)cmodel.getGreen(i)!=colormap[off++] || (byte)cmodel.getBlue(i)!=colormap[off++]) {
					makeNew=true;
					break;
				}
			}
		}
		if(makeNew) {
			//////System.out.println("new color model");
			cmodel=new IndexColorModel(7,len,colormap,0,false);
		}
		return true;
	}
	public int byteToInt(byte[] buf, int offset) {
	    return (b2i(buf[offset+3])<<24) | (b2i(buf[offset+2])<<16) |
	           (b2i(buf[offset+1])<< 8) | b2i(buf[offset]);
	}

	// JP: I might not need this because I can get the image in jpeg. 
	
	public BufferedImage getImage() {
		////System.out.println("UDPVisionListener: getImage, line 730");
		if(!updatedFlag){
			////System.out.println("JP: In Line 847. Problem with updatedFlag");
			return img;
		}
		if(isIndex) {
			////System.out.println("Line 821: In getImage()");
			byte[] data= getData();  
			if(cmodel==null)
				return img;
			if(img.getWidth()!=width || img.getHeight()!=height || img.getType()!=BufferedImage.TYPE_BYTE_INDEXED)
				img=new BufferedImage(width,height,BufferedImage.TYPE_BYTE_INDEXED,cmodel);
			img.getRaster().setDataElements(0,0,width,height,data);
		} 
		
		else {
			////System.out.println("JP: Line 861. Should it work now");
			int[] pix;
			if(img.getWidth()!=width || img.getHeight()!=height || img.getType()!=BufferedImage.TYPE_INT_RGB)
				img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
			synchronized(_outd) {
				if(format==ENCODE_COLOR)
					pix=getConvertRGB()?getRGBPixels():getYUVPixels();
				else
					pix=getIntensityPixels();
				img.setRGB(0,0,width,height,pix,0,width);
			}
		}
		
		return img;
	
	}
	public byte[] getData() {
		synchronized (_outd) {
			updatedFlag=false;
			return _outd;
		}
	}
	public boolean getConvertRGB() { return convertRGB; }

	public int[] getYUVPixels() {
		////System.out.println("UDPVisionListener: getYUVPixels, line 655");
		synchronized(_outd) {
			byte[] data=getData();
			int offset=0;
			for (int i=0; i<width*height; i++) {
				int y=(int)data[offset++]&0xFF;
				int u=(int)data[offset++]&0xFF;
				int v=(int)data[offset++]&0xFF;
				_pixels[i]=(255<<24) | (y<<16) | (u<<8) | v;
			}
		}
		return _pixels;
	}

	//This still uses RGB pixels just in case you want to display the
	//intensity value into hues instead of black/white
	public int[] getIntensityPixels() {
		////System.out.println("UDPVisionListener: getIntensityPixels, line 672");
		synchronized(_outd) {
			byte[] data=getData();
			int offset=0;
			if(!getConvertRGB()) {
				for (int i=0; i<width*height; i++) {
					int z=(int)data[offset++]&0xFF;
					_pixels[i]=(255<<24) | (z<<16) | (z<<8) | z;
				}
			} else if(chan_id==CHAN_Y || chan_id==CHAN_Y_DY || chan_id==CHAN_Y_DX || chan_id==CHAN_Y_DXDY ) {
				for (int i=0; i<width*height; i++) {
					int z=(int)data[offset++]&0xFF;
					_pixels[i]=pixelYUV2RGB(z,0x80,0x80);
				}
			} else if(chan_id==CHAN_U) {
				for (int i=0; i<width*height; i++) {
					int z=(int)data[offset++]&0xFF;
					_pixels[i]=pixelYUV2RGB(0x80,z,0x80);
				}
			} else if(chan_id==CHAN_V) {
				for (int i=0; i<width*height; i++) {
					int z=(int)data[offset++]&0xFF;
					_pixels[i]=pixelYUV2RGB(0x80,0x80,z);
				}
			}
		}		
		return _pixels;
	}

	public int[] getRGBPixels() {
		////System.out.println("UDPVisionListener: getRGBPixels, line 702");
		synchronized(_outd) {
			byte[] data=getData();
			int offset=0;
			for (int i=0; i<width*height; i++) {
				int y=(int)data[offset++]&0xFF;
				int u=(int)data[offset++]&0xFF;
				int v=(int)data[offset++]&0xFF;
				_pixels[i]=pixelYUV2RGB(y, u, v);
			}
		}
		return _pixels;
	}

	static final int pixelYUV2RGB(int y, int u, int v) {
		u=u*2-255;
		v=v-128;
		int b=y+u;
		int r=y+v;
		v=v>>1;
		u=(u>>2)-(u>>4);
		int g=y-u-v;
		if (r<0) r=0; if (g<0) g=0; if (b<0) b=0;
		if (r>255) r=255; if (g>255) g=255; if (b>255) b=255;
		return (255<<24) | (r<<16) | (g<<8) | b;
	}

  	
}

  	