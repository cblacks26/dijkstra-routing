package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TCPSender implements Runnable{
	
	private volatile boolean isDone;
	private DataOutputStream dout;
	private List<byte[]> payloads;
	
	public TCPSender(Socket socket) throws IOException {
		this.dout = new DataOutputStream(socket.getOutputStream());
		this.isDone = false;
		this.payloads = Collections.synchronizedList(new ArrayList<byte[]>());
	}

	public void sendData(byte[] dataToSend){
		payloads.add(dataToSend);
	}
	
	public void close() throws IOException {
		dout.close();
	}
	
	public void setDone(boolean done) {
		isDone = done;
	}

	@Override
	public void run() {
		while(!isDone) {
			while(payloads.size()>0) {
				byte[] data = payloads.remove(0);
				try {
					int dataLength = data.length;
					dout.writeInt(dataLength);
					dout.write(data,0,dataLength);
					dout.flush();
				}catch(IOException ie) {
					System.out.println("Error sending data: "+ ie.getMessage());
				}
			}
		}
	}
}
