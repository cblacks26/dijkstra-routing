package cs455.overlay.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import cs455.overlay.transport.TCPRecieverThread;

public class Registry implements Node{

	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		boolean running = true;
		try {
			serverSocket = new ServerSocket();
		} catch (IOException e) {
			System.out.println("Error creating ServerSocket: "+e.getMessage());
		}
		while(running) {
			try {
				Socket inSocket = serverSocket.accept();
				
				TCPRecieverThread recThread = new TCPRecieverThread(inSocket);
				
				recThread.run();
			}catch(IOException ie) {
				System.out.println("Error accepting connection: "+ie.getMessage());
			}
		}
		
	}
	
	@Override
	public void onEvent() {
		
	}
	
}
