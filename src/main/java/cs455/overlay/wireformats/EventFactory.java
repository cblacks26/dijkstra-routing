package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EventFactory{

	public static Event create(int dataLength, byte[] bytes) throws IOException, Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(new BufferedInputStream(bais));
		int type = dis.readInt();
		Event event = null;
		if(type == 1) {
			event = new Register(bytes);
		}else if (type == 2) {
			event = new RegisterResponse(bytes);
		}else if (type == 3){
			
		}else if (type == 4) {
			
		}else if (type == 5) {
			
		}else {
			throw new Exception("Unrecognized Message Type");
		}
		dis.close();
		bais.close();
		return event;
	}
	
}
