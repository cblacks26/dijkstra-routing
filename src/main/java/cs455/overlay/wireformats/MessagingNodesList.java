package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MessagingNodesList extends Event{

	private int numberOfNodes;
	private String[] nodes;
	
	public MessagingNodesList(byte[] data) throws IOException {
		super(data);
		this.numberOfNodes = dis.readInt();
		this.nodes = new String[numberOfNodes];
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
	
	public static byte[] createMessage(String list, int numberOfNodes) throws IOException {
		byte[] marshallBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));
		dos.writeInt(4);
		dos.writeInt(numberOfNodes);
		dos.write(list.getBytes());
		dos.flush();
		marshallBytes = baos.toByteArray();
		baos.close();
		dos.close();
		return marshallBytes;
	}
}
