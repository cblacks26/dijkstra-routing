package cs455.overlay.wireformats;

import java.io.IOException;

public class Deregister extends Event{

	public Deregister(byte[] data) throws IOException {
		super(data);
	}

}
