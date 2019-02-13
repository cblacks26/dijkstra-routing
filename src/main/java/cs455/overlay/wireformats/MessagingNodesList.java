package cs455.overlay.wireformats;

import java.io.IOException;

public class MessagingNodesList extends Event{

	private String[] nodes;
	
	public MessagingNodesList(byte[] data) throws IOException {
		super(data);
		this.nodes = new String[dis.readInt()];
		byte[] buff = new byte[data.length];
		dis.readFully(buff,0,data.length-8);
		close();
		
		String list = buff.toString();
		nodes = list.split(" ");
	}
	
	public int getNumberOfNodes() {
		return nodes.length;
	}

	public String[] getNodes() {
		return nodes;
	}
}
