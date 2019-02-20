package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public abstract class Event {

	private byte[] data;
	protected ByteArrayInputStream bais;
	protected DataInputStream dis;
	protected int type;
	
	public Event(byte[] data) throws IOException {
		this.data = data;
		if(data!=null) {
			this.bais = new ByteArrayInputStream(data);
			this.dis = new DataInputStream(new BufferedInputStream(bais));
			this.type = dis.readInt();
		}
	}
	
	public byte[] getBytes() {
		return data;
	}
	
	public int getType() {
		return type;
	}
	
	public void close() throws IOException {
		dis.close();
		bais.close();
	}
	
}
