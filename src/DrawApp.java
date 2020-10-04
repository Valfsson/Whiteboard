package drawApp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class DrawApp extends JFrame {
	
	private Paper p = new Paper();
	
	// Communication with UDP
	int localPort=2000;
	String remoteHost="localhost";
	int remotePort=2001;
	
	UdpThread udp=p.connect(localPort,remoteHost,remotePort);
	
	/**
	 * Constructor
	 */
	public DrawApp() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().add(p, BorderLayout.CENTER);

		setSize(640, 480);
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new DrawApp();
	}
}

class Paper extends JPanel {

	/*    Hash Set: can't contain duplicates; NOT thread-safe. You must explicitly synchronize concurrent access to a HashSet in a multi-threaded environment.  */
	Set<Point> s = Collections.synchronizedSet(new HashSet<Point>());
	
	private UdpThread udp=null;

	public Paper() {
		setBackground(Color.white);
		addMouseListener(new L1());
		addMouseMotionListener(new L2());
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.black);
   
		// Iterating Set, to make Hash-set Thread safe
		synchronized(s) {
		    Iterator<Point> i = s.iterator(); // Must be in the synchronized block
		    while (i.hasNext()) {
		    	Point p = (Point) i.next();
			g.fillOval(p.x, p.y, 10, 10);
		}
		    }
	}

	/**
	 * Adds point to Paper
	 * @param x coordinate
	 * @param y coordinate
	 */
	private void addPoint(int x,int y) {
		Point p= new Point(x,y);
		s.add(p);
		repaint();		
	}
	
	/**
	 * Establishes UDP connection
	 * @param localPort
	 * @param remoteHost
	 * @param remotePort
	 * @return
	 */
	public UdpThread connect (int localPort, String remoteHost, int remotePort) {
		udp=new UdpThread(localPort,remoteHost,remotePort,this);
		udp.start();
		return udp;
	}

	/**
	 * Adds both local and remote points to paper
	 * @param x
	 * @param y
	 * @param isRemote
	 */
	public void drawPoint (int x, int y, boolean isRemote) {				
		addPoint(x,y);
		
		if(!isRemote && udp!=null) {
			udp.sendPoint(x, y);
		}
	}
	
	class L1 extends MouseAdapter {
		public void mousePressed(MouseEvent me) {
			drawPoint(me.getPoint().x,me.getPoint().y,false); 
		}
	}

	class L2 extends MouseMotionAdapter {
		public void mouseDragged(MouseEvent me) {
			drawPoint(me.getPoint().x,me.getPoint().y,false); 
		}
	}
}
