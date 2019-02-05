package cs455.overlay.wireformats;

public class Deregister extends Event{

	public Deregister(byte[] data) {
		super(data);
	}
	
	@Override
	public String getType() {
		return "DEREGISTER";
	}

}
