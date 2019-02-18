package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PullTaskSummary extends Event{

	public PullTaskSummary(byte[] data) throws IOException {
		super(data);
	}

	public static byte[] createMessage() throws IOException {
		byte[] marshallBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));
		dos.writeInt(8);
		dos.flush();
		marshallBytes = baos.toByteArray();
		baos.close();
		dos.close();
		return marshallBytes;
	}
	
}
