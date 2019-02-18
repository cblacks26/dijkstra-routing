package cs455.overlay.wireformats;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskSummary extends Event{

	private String ip;
	private int nodePort;
	private int numberSent;
	private long summationSent;
	private int numberRec;
	private long summationRec;
	private int numberRelay;
	
	public TaskSummary(byte[] data) throws IOException {
		super(data);
		
		int sizeString = data.length - ((4*5) + (8*2));
		byte[] buff = new byte[data.length];
		dis.readFully(buff,0,sizeString);
		this.ip = new String(buff);
		
		this.nodePort = dis.readInt();
		this.numberSent = dis.readInt();
		this.summationSent = dis.readLong();
		this.numberRec = dis.readInt();
		this.summationRec = dis.readLong();
		this.numberRelay = dis.readInt();
		close();
	}
	
	public String getIpAddress() {
		return ip;
	}
	
	public int getPort() {
		return nodePort;
	}
	
	public int getNumberSentMessages() {
		return numberSent;
	}
	
	public long getSummationSentMessages() {
		return summationSent;
	}
	
	public int getNumberRecievedMessages() {
		return numberRec;
	}

	public long getSummationRecievedMessages() {
		return summationRec;
	}
	
	public int getNumberRelayedMessages() {
		return numberRelay;
	}
	
	public static byte[] createMessage(String ipAddress, int port, int numSent, long sumSent, int numRec, long sumRec, int numRelay) throws IOException {
		byte[] marshallBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));
		dos.writeInt(9);
		dos.write(ipAddress.getBytes());
		dos.writeInt(port);
		dos.writeInt(numSent);
		dos.writeLong(sumSent);
		dos.writeInt(numRec);
		dos.writeLong(sumRec);
		dos.writeInt(numRelay);
		dos.flush();
		marshallBytes = baos.toByteArray();
		baos.close();
		dos.close();
		return marshallBytes;
	}
	
}
