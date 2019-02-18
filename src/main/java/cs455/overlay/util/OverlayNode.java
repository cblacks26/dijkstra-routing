package cs455.overlay.util;

import cs455.overlay.transport.TCPConnection;

public class OverlayNode {

	private TCPConnection connection;
	private String ipAddress;
	private int listeningPort;
	
	public OverlayNode(String ip, int port, TCPConnection connection) {
		this.ipAddress = ip.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
		this.listeningPort = port;
		this.connection = connection;
	}
	
	public TCPConnection getConnection() {
		return connection;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	public int getListeningPort() {
		return listeningPort;
	}
	
	public String toString() {
		return ipAddress+":"+listeningPort;
	}
	
	public boolean equals(Object o) {
		if(o instanceof OverlayNode) return equals((OverlayNode)o);
		return false;
	}
	
	public boolean equals(OverlayNode on) {
		// System.out.println(on.ipAddress+":"+on.listeningPort+" - "+ipAddress+":"+listeningPort);
		// System.out.println(on.ipAddress.length()+" : "+ipAddress.length());
		if(on.getIpAddress().equalsIgnoreCase(ipAddress)) {
			// System.out.println("IPAddress is equal");
			if(on.getListeningPort()==listeningPort) return true;
		}
		return false;
	}
	
	public boolean equals(OverlayNode on, OverlayNode on2) {
		return on.equals(on2);
	}
	
}
