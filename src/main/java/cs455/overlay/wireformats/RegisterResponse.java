package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterResponse extends Event{

	private int result;
	private String info;
	
	public RegisterResponse(byte[] data) throws IOException {
		super(data);
		
		result = dis.readInt();
		byte[] buff = new byte[data.length];
		dis.readFully(buff,0,data.length-8);
		info = new String(buff);
		close();
	}

	public int getResult() {
		return result;
	}
	
	public String getExtraInfo() {
		return info;
	}
	
	public static byte[] createMessage(String info, int result) throws IOException {
		byte[] infoBytes = info.getBytes();
		byte[] marshallBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));
		dos.writeInt(2);
		dos.writeInt(result);
		dos.write(infoBytes);
		dos.flush();
		marshallBytes = baos.toByteArray();
		baos.close();
		dos.close();
		return marshallBytes;
	}
}
