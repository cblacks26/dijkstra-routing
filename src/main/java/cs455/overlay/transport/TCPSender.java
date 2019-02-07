package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPSender {
	
	private DataOutputStream dout;
	
	public TCPSender(Socket socket) throws IOException {
		this.dout = new DataOutputStream(socket.getOutputStream());
	}

	public void sendData(byte[] dataToSend){
		try {
			int dataLength = dataToSend.length;
			dout.writeInt(dataLength);
			dout.write(dataToSend,0,dataLength);
			dout.flush();
		}catch(IOException ie) {
			System.out.println("Error sending data: "+ ie.getMessage());
		}
	}
	
	public void close() throws IOException {
		dout.close();
	}
}
