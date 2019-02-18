package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Deregister extends Event{

	private String ipAddress;
	private int port;
	
	public Deregister(byte[] data) throws IOException {
		super(data);
		
		// read bytes minus an integer for the port
		byte[] buff = new byte[data.length];
		dis.readFully(buff,0,data.length-8);
		ipAddress = new String(buff).replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
		port = dis.readInt();
		close();
	}
	
	public int getNodePort() {
		return port;
	}
	
	public String getIPAddress() {
		return ipAddress;
	}
	
	public static byte[] createMessage(String ip, int port) throws IOException {
		byte[] marshalBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baos));
		dataOut.writeInt(3);
		dataOut.write(ip.getBytes());
		dataOut.writeInt(port);
		dataOut.flush();
		marshalBytes = baos.toByteArray();
		dataOut.close();
		baos.close();
		return marshalBytes;
	}
}
