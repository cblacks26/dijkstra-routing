package cs455.overlay.wireformats;

import java.io.IOException;

public class Register extends Event{

	private String ipAddress;
	private int port;
	
	public Register(byte[] data) throws IOException {
		super(data);
		
		// read bytes minus an integer for the port
		ipAddress = dis.readNBytes(data.length-8).toString();
		port = dis.readInt();
		close();
	}
	
	public int getNodePort() {
		return port;
	}
	
	public String getIPAddress() {
		return ipAddress;
	}
}
