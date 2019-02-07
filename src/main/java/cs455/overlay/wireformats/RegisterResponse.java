package cs455.overlay.wireformats;

import java.io.IOException;

public class RegisterResponse extends Event{

	private int result;
	private String info;
	
	public RegisterResponse(byte[] data) throws IOException {
		super(data);
		
		result = dis.readInt();
		info = dis.readNBytes(data.length-8).toString();
	}

	public int getResult() {
		return result;
	}
	
	public String getExtraInfo() {
		return info;
	}
}
