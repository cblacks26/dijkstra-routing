package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LinkWeights extends Event{

	private int numberOfLinks;
	private String[] links;
	
	public LinkWeights(byte[] data) throws IOException {
		super(data);
		this.numberOfLinks = dis.readInt();
		this.links = new String[numberOfLinks];
		byte[] buff = new byte[data.length];
		dis.readFully(buff,0,data.length-8);
		close();
		
		String list = new String(buff).trim();
		
		links = list.split(" ");
	}
	
	public int getNumberOfLinks() {
		return numberOfLinks;
	}
	
	public String[] getLinks() {
		return links;
	}

	public static byte[] createMessage(String list, int numLinks) throws IOException {
		byte[] marshallBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));
		dos.writeInt(5);
		dos.writeInt(numLinks);
		dos.write(list.getBytes());
		dos.flush();
		marshallBytes = baos.toByteArray();
		baos.close();
		dos.close();
		return marshallBytes;
	}
	
}
