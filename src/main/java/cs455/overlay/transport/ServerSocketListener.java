package cs455.overlay.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import cs455.overlay.node.Node;

public class ServerSocketListener implements Runnable{

	private volatile boolean isListening;
	private int port;
	private String address;
	private Node parent;
	
	public ServerSocketListener(Node parent) throws UnknownHostException {
		this.isListening = false;
		this.parent = parent;
		this.address = InetAddress.getLocalHost().getHostAddress();
		this.isListening = false;
	}
	
	@Override
	public void run() {
		int tempPort = 5000;
		ServerSocket serverSocket = null;
		while(!serverSocket.isBound()) {
			try {
				serverSocket = new ServerSocket(tempPort);
			} catch (IOException e) {
				System.out.println("Error creating ServerSocket: "+e.getMessage());
				tempPort++;
			}
		}
		port = tempPort;
		parent.onListening(port);
		this.isListening = true;
		while(isListening) {
			try {
				Socket inSocket = serverSocket.accept();
				
				TCPConnection connection = new TCPConnection(parent,inSocket);
				parent.onConnection(connection);
			}catch(IOException ie) {
				System.out.println("Error accepting connection: "+ie.getMessage());
			}
		}
	}
	
	public void stopListening() {
		this.isListening = false;
	}
	
	public int getPort() {
		return port;
	}

	public String getAddress() {
		return address;
	}
}
