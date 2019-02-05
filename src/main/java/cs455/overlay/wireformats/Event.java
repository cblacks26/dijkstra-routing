package cs455.overlay.wireformats;

public abstract class Event {

	private byte[] data;
	
	public Event(byte[] data) {
		this.data = data;
	}
	
	public byte[] getBytes() {
		return data;
	}
	
	public String getType() {
		return "UNKNOWN";
	}
	
}
