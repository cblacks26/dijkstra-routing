package cs455.overlay.node;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

import cs455.overlay.transport.ServerSocketListener;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.util.Overlay;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.MessagingNodesList;
import cs455.overlay.wireformats.RegisterResponse;

public class MessagingNode implements Node{

	private String registry;
	private HashMap<String,TCPConnection> conns;
	private ServerSocketListener listener;
	private Overlay overlay;
	
	public MessagingNode(String host, int port) {
		this.conns = new HashMap<String,TCPConnection>();
		this.registry = host+":"+port;
		this.listener = null;
		this.overlay = null;
		createSocket(this,host,port);
		try {
			this.listener = new ServerSocketListener(this);
			Thread thread = new Thread(listener);
			thread.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
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
		MessagingNode node = new MessagingNode(host,port);
	}
	
	private void createSocket(Node node, String host, int port) {
		TCPConnection conn = new TCPConnection(node, host, port);
		conns.put(host+":"+port, conn);
	}
	
	private void register(){
		int type = 1;
		byte[] ip = listener.getAddress().getBytes();
		int port = listener.getPort();
		byte[] marshalBytes = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baos));
			
			dataOut.writeInt(type);
			dataOut.write(ip);
			dataOut.writeInt(port);
			dataOut.flush();
			marshalBytes = baos.toByteArray();
			dataOut.close();
			baos.close();
			conns.get(registry).sendData(marshalBytes);
		}catch(IOException ioe) {
			System.out.println("Error sending register message to registry: "+ioe.getMessage());
		}
	}
	
	@Override
	public void onEvent(Event e, TCPConnection conn) throws UnknownHostException {
		// Register Response
		if(e.getType()==2) {
			RegisterResponse regRes = (RegisterResponse)e;
			// SUCCESS
			if(regRes.getResult()==1) {
				listener = new ServerSocketListener(this);
				System.out.println("Register successful");
			// FAILURE
			}else {
				System.out.println("error");
				System.out.println(regRes.getExtraInfo());
				System.exit(1);
			}
		} else if(e.getType() == 4) {
			MessagingNodesList mnl = (MessagingNodesList)e;
			for(String node:mnl.getNodes()) {
				String[] info = node.split(":");
				createSocket(this,info[0],Integer.parseInt(info[1]));
			}
			System.out.println("All connections are established. Number of connections: "+mnl.getNumberOfNodes());
		}else if(e.getType() == 5) {
			LinkWeights lw = (LinkWeights)e;
			this.overlay = new Overlay(lw.getLinks());
			System.out.println("Link weights are received and processed. Ready to send messages.");
		}else {
			System.out.println("Error should not be recieving messages of this type");
		}
	}

	@Override
	public void onConnection(TCPConnection connection) {
		conns.put(connection.getIPAddress()+":"+connection.getListeningPort(),connection);
	}

	@Override
	public void onListening(int port){
		register();
	}

	@Override
	public void errorListening(String message) {
		// TODO Auto-generated method stub
		
	}

}
