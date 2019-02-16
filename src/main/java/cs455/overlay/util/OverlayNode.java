package cs455.overlay.util;

import cs455.overlay.transport.TCPConnection;

public class OverlayNode {

	private TCPConnection connection;
	private String ipAddress;
	private int listeningPort;
	
	public OverlayNode(String ip, int port, TCPConnection connection) {
		this.ipAddress = ip;
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
	
}
