package cs455.overlay.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import cs455.overlay.node.Node;

public class ServerSocketListener implements Runnable{

	private volatile boolean isListening;
	private ServerSocket serverSocket;
	private volatile int port;
	private String address;
	private Node parent;
	
	public ServerSocketListener(Node parent) throws UnknownHostException {
		this.isListening = false;
		this.parent = parent;
		this.address = InetAddress.getLocalHost().getHostAddress();
		this.isListening = false;
		this.port = 5001;
	}
	
	public ServerSocketListener(Node parent, int port) throws UnknownHostException {
		this.isListening = false;
		this.parent = parent;
		this.address = InetAddress.getLocalHost().getHostAddress();
		this.port = port;
		this.isListening = false;
	}
	
	@Override
	public void run() {
		createServerSocket();
		listen();
	}
	
	public void createServerSocket() {
		serverSocket = null;
		boolean bound = false;
		while(!bound) {
			try {
				serverSocket = new ServerSocket(port);
				bound = true;
			} catch (IOException e) {
				bound = false;
				port++;
			}
		}
		parent.onListening(port);
	}
	
	public void listen() {
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
		System.out.println("Server listening on "+ serverSocket.getLocalPort());
		return port;
	}

	public String getAddress() {
		return address;
	}
}
