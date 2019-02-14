package cs455.overlay.wireformats;

import java.io.IOException;

public class Register extends Event{

	private String ipAddress;
	private int port;
	
	public Register(byte[] data) throws IOException {
		super(data);
		
		// read bytes minus an integer for the port
		byte[] buff = new byte[data.length];
		dis.readFully(buff,0,data.length-8);
		ipAddress = new String(buff);
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
