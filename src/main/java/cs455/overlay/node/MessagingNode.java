package cs455.overlay.node;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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

	private String regist;
	private HashMap<String,TCPConnection> conns;
	private Overlay overlay;
	
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
		node.regist = host+":"+port;
		node.createSocket(node,host,port);
		node.register();
	}
	
	private void createSocket(Node node, String host, int port) {
		try {
			conns.put(regist,new TCPConnection(node, new Socket(host,port)));
		} catch (IOException e) {
			System.out.println("Error creating socket to "+host+":"+port+": "+ e.getMessage());
			
		}
	}
	
	private void register() throws IOException {
		byte[] marshalBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(baos));
		int type = 1;
		byte[] ip = InetAddress.getLocalHost().getHostAddress().getBytes();
		int port = conns.get(0).getListeningPort();
		dataOut.writeInt(type);
		dataOut.write(ip);
		dataOut.writeInt(port);
		dataOut.flush();
		marshalBytes = baos.toByteArray();
		dataOut.close();
		baos.close();
		conns.get(regist).sendData(marshalBytes);
	}
	
	@Override
	public void onEvent(Event e, TCPConnection conn) throws UnknownHostException {
		// Register Response
		if(e.getType()==2) {
			RegisterResponse regRes = (RegisterResponse)e;
			// SUCCESS
			if(regRes.getResult()==1) {
				ServerSocketListener listener = new ServerSocketListener(this);
				System.out.println("Register successful");
			// FAILURE
			}else {
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
		
	}

	@Override
	public void onListening(int port) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void errorListening(String message) {
		// TODO Auto-generated method stub
		
	}

}
