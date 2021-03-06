package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Message extends Event{

	private String path;
	private int number;
	
	public Message(byte[] data) throws IOException {
		super(data);
		// read bytes minus an integer for the port
		byte[] buff = new byte[data.length];
		dis.readFully(buff,0,data.length-8);
		path = new String(buff);
		number = dis.readInt();
		close();
	}
	
	public int getNumber() {
		return number;
	}
	
	public String getPath() {
		return path;
	}
	
	public static byte[] createMessage(String path, int number) throws IOException {
		byte[] marshalBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baos));
		dataOut.writeInt(10);
		dataOut.write(path.getBytes());
		dataOut.writeInt(number);
		dataOut.flush();
		marshalBytes = baos.toByteArray();
		dataOut.close();
		baos.close();
		return marshalBytes;
	}
	
}
