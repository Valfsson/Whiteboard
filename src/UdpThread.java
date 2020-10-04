package drawApp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
 * Client-server communication using UDP
 */
public class UdpThread extends Thread {

	private DatagramSocket inSocket, outSocket; // UDP sockets
	private InetAddress remoteAddr; // IP adress of the remote host
	private int remotePort; //UDP port of the remote host
	private volatile boolean running; //value of variable can be modified b different threads at the same time
	private Paper paper; //owns this UDP. Incoming data sends here
	
	public UdpThread(int localPort, String remoteHost, int remotePort,Paper owner) {
	this.paper=owner;
	this.inSocket=null;
	this.outSocket=null;
	this.remoteAddr=null;
	this.remotePort=remotePort;
	this.running=true;
	
	/* 
	 * creates inbound datagram socket
	 */
	try 
	{
		inSocket=new DatagramSocket(localPort);
	}
	
	catch(SocketException e){
	
		/* If can't bind to local port, swap local and remote ports and try again.
		 * In case of failure next time-quit.
		 */
		if(e instanceof java.net.BindException && localPort==2000 && remotePort==2001) {
			{
			this.remotePort=localPort;
			localPort=remotePort;
			remotePort=this.remotePort;
			
			try {
				inSocket=new DatagramSocket(localPort);
			} catch(SocketException e2) {
				System.exit(0); //exit
				running=false; 
			}	
			}	
		}
	}	
		//Creates outbound Datagram socket
		if(running)
			try {
		outSocket= new DatagramSocket();
		remoteAddr=InetAddress.getByName(remoteHost);
			}
		catch (SocketException e) {
			running=false;
		}
		catch(UnknownHostException e) {
			running=false;
		}
	}
	
	/**
	 * Gets local UDP port
	 */
	public int getLocalPort() {
		return inSocket.getLocalPort();
	}

	/**
	 * Gets remote host
	 */
	public String getRemoteHost() {
		return remoteAddr.getHostAddress();
	}

	/**
	 * Gets remote port
	 */
	public int getRemotePort() {
		return remotePort;
	}
	
	/**
	 * Send point (x,y) to remote application
	 */
	public void sendPoint(int x, int y) {
		if(outSocket !=null) 
			try {
				String message=" "+x+" "+y+" ";
				byte[]buffered =message.getBytes();
				
				outSocket.send(new DatagramPacket(buffered, buffered.length,remoteAddr,remotePort));			
		}catch(IOException e) 
		{	
		}
	}

	/**
	 *  Receive packets
	 */
	@Override  
	public void run() {
		byte[] inBuffer= new byte[1204];
		DatagramPacket inPacket= new DatagramPacket(inBuffer,inBuffer.length);
		String inMessage=null;
		
		while(running) {
			try {
				inSocket.receive(inPacket);
				byte[] resizedBuffer= new byte[inPacket.getLength()];
				System.arraycopy(inBuffer, 0, resizedBuffer, 0, inPacket.getLength());
				
				inMessage=new String(resizedBuffer);
				String [] word=inMessage.trim().split("\\s{1,}");
				
				int x=Integer.parseInt(word[0]);
				int y=Integer.parseInt(word[1]);
				
				//draws the point
				paper.drawPoint(x,y, true /* = remote*/);		
			}
			catch(NumberFormatException e) {
				System.err.println("Invalid packet: ["+ inMessage+"]");
			}
			catch(IOException ex)
			{
				running= false;}
			}
	
	}
	

}
