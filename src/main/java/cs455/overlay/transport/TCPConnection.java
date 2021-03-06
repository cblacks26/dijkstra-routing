package cs455.overlay.transport;

import java.io.IOException;
import java.net.Socket;

import cs455.overlay.node.Node;

public class TCPConnection {

	private Node parent;
	private Socket socket;
	private TCPRecieverThread listener;
	private TCPSender sender;
	private String address;
	private Thread listenerThread;
	
	public TCPConnection(Node node, Socket socket) throws IOException {
		this.parent = node;
		this.socket = socket;
		listener = new TCPRecieverThread(this, socket);
		sender = new TCPSender(socket);
		Thread threadSender = new Thread(sender);
		threadSender.start();
		listenerThread = new Thread(listener);
		listenerThread.start();
		this.address = socket.getInetAddress().getHostAddress().replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
	}
	
	public TCPConnection(Node node, String host, int port) {
		this.parent = node;
		try {
			this.socket = new Socket(host,port);
			listener = new TCPRecieverThread(this, socket);
			sender = new TCPSender(socket);
		} catch (IOException e) {
			System.out.println("Error creating TCPConnection Object: "+e.getMessage());
			System.exit(1);
		}
		Thread listenerThread = new Thread(listener);
		listenerThread.start();
	}
	
	public Node getParentNode() {
		return parent;
	}
	
	public int getListeningPort() {
		return socket.getPort();
	}
	
	public TCPSender getSender() {
		return sender;
	}
	
	public String getIPAddress() {
		return address;
	}
	
	public void closeConnection() {
		try {
			sender.close();
			listener.finish();
			System.exit(0);
		}catch(IOException ie) {
			System.out.println("Error closing connection: "+ie.getMessage());
		}
	}
}
