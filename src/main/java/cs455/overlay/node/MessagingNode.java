package cs455.overlay.node;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import cs455.overlay.transport.ServerSocketListener;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.RegisterResponse;

public class MessagingNode implements Node{

	private Socket regSocket;
	private TCPConnection conn;
	
	public static void main(String args[]) throws IOException{
		String host = null;
		int port = 0;
		if(args.length!=2) {
			System.out.println("Need the host ip address and port");
			System.exit(0);
		}else {
			host = args[0];
			port = Integer.parseInt(args[1]);
		}
		MessagingNode node = new MessagingNode();
		node.createSocket(node,host,port);
		node.register();
	}
	
	private void createSocket(Node node, String host, int port) {
		try {
			regSocket = new Socket(host,port);
			conn = new TCPConnection(node, regSocket);
		} catch (IOException e) {
			System.out.println("Error connecting to Registry: "+ e.getMessage());
			
		}
	}
	
	private void register() throws IOException {
		byte[] marshalBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baos));
		int type = 1;
		byte[] ip = InetAddress.getLocalHost().getHostAddress().getBytes();
		int port = conn.getListeningPort();
		dataOut.writeInt(type);
		dataOut.write(ip);
		dataOut.writeInt(port);
		dataOut.flush();
		marshalBytes = baos.toByteArray();
		dataOut.close();
		baos.close();
		conn.sendData(marshalBytes);
	}
	
	@Override
	public void onEvent(Event e, TCPConnection conn) throws UnknownHostException {
		// Register Response
		if(e.getType()==2) {
			RegisterResponse regRes = (RegisterResponse)e;
			// SUCCESS
			if(regRes.getResult()==1) {
				ServerSocketListener listener = new ServerSocketListener(this);
			// FAILURE
			}else {
				
			}
		} else {
			System.out.println("Error should not be recieving messages of this type");
		}
	}

	@Override
	public void onConnection(TCPConnection connection) {
		
	}

	@Override
	public void onListening(int port) {
		// TODO Auto-generated method stub
		
	}

}
