package cs455.overlay.wireformats;

public class Message extends Event{

	public Message(byte[] data) {
		super(data);
	}

	@Override
	public String getType() {
		return "MESSAGE";
	}

}
