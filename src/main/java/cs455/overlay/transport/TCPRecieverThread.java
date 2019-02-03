package cs455.overlay.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class TCPRecieverThread implements Runnable{

	private Socket socket;
	private DataInputStream din;
	
	public TCPRecieverThread(Socket socket) throws IOException {
		this.socket = socket;
		this.din = new DataInputStream(socket.getInputStream());
	}
	
	@Override
	public void run() {
		int dataLength;
		while(socket != null) {
			try {
				dataLength = din.readInt();
				byte[] data = new byte[dataLength];
				din.readFully(data,0,dataLength);
			} catch(SocketException se) {
				System.out.println(se.getMessage());
				break;
			} catch(IOException e) {
				System.out.println(e.getMessage());
				break;
			}
		}
		
	}

}
