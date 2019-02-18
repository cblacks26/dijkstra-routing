package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskInitiate extends Event{

	private int numberRounds;
	
	public TaskInitiate(byte[] data) throws IOException {
		super(data);
		
		this.numberRounds = dis.readInt();
		System.out.println("Number of Rounds "+numberRounds);
		close();
	}

	public int getNumberOfRounds() {
		return numberRounds;
	}
	
	
	public static byte[] createMessage(int numRounds) throws IOException {
		byte[] marshallBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));
		dos.writeInt(6);
		dos.writeInt(numRounds);
		dos.flush();
		marshallBytes = baos.toByteArray();
		baos.close();
		dos.close();
		return marshallBytes;
	}
}
