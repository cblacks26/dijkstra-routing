package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;

public class TCPRecieverThread implements Runnable{

	private TCPConnection conn;
	private DataInputStream din;
	private volatile boolean isDone;
	
	public TCPRecieverThread(TCPConnection conn, Socket socket) throws IOException {
		this.conn = conn;
		this.din = new DataInputStream(socket.getInputStream());;
	}
	
	@Override
	public void run() {
		int dataLength;
		while(!isDone) {
			try {
				dataLength = din.readInt();
				byte[] data = new byte[dataLength];
				din.readFully(data,0,dataLength);
				Event event = EventFactory.create(dataLength,data);
				conn.getParentNode().onEvent(event,conn);
			} catch(SocketException se) {
				System.out.println("SE: "+se.getMessage());
				break;
			} catch(IOException e) {
				System.out.println("IOE: "+e.getMessage());
				break;
			} catch (Exception e) {
				System.out.println("E: "+e.getMessage());
			}
		}
	}

	public void finish() throws IOException {
		this.isDone = true;
		din.close();
	}
}
