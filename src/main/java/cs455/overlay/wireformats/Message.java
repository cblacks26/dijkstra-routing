package cs455.overlay.wireformats;

import java.io.IOException;

public class Message extends Event{

	
	
	public Message(byte[] data) throws IOException {
		super(data);
	}

}
