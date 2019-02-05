package cs455.overlay.wireformats;

public class Register extends Event{

	public Register(byte[] data) {
		super(data);
	}

	@Override
	public String getType() {
		return "REGISTER";
	}

}
