package cs455.overlay.wireformats;

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
}
