package cs455.overlay.wireformats;

import java.io.IOException;

public class LinkWeights extends Event{

	private String[] links;
	
	public LinkWeights(byte[] data) throws IOException {
		super(data);
		
		this.links = new String[dis.readInt()];
		byte[] buff = new byte[data.length];
		dis.readFully(buff,0,data.length-8);
		close();
		
		String list = buff.toString();
		
		links = list.split(" ");
	}
	
	public int getNumberOfLinks() {
		return links.length;
	}
	
	public String[] getLinks() {
		return links;
	}

}
