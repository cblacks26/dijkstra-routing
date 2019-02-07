package cs455.overlay.transport;

import java.io.IOException;
import java.net.Socket;

import cs455.overlay.node.Node;

public class TCPConnection {

	private Node parent;
	private Socket socket;
	private TCPRecieverThread listener;
	private TCPSender sender;
	
	public TCPConnection(Node node, Socket socket) throws IOException {
		this.parent = node;
		this.socket = socket;
		listener = new TCPRecieverThread(this, socket);
		sender = new TCPSender(socket);
		Thread listenerThread = new Thread(listener);
		listenerThread.start();
	}
	
	public Node getParentNode() {
		return parent;
	}
	
	public int getListeningPort() {
		return listener.getPort();
	}
	
	public void sendData(byte[] data) {
		sender.sendData(data);
	}
	
	public String getIPAddress() {
		return socket.getInetAddress().getHostAddress();
	}
	
	public void closeConnection() {
		try {
			sender.close();
			listener.finish();
		}catch(IOException ie) {
			System.out.println("Error closing connection: "+ie.getMessage());
		}
	}
}
