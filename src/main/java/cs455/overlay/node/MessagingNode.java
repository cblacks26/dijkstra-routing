package cs455.overlay.node;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

import cs455.overlay.transport.ServerSocketListener;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.util.Router;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.MessagingNodesList;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.RegisterResponse;

public class MessagingNode implements Node{

	private String registry;
	private HashMap<String,TCPConnection> conns;
	private ServerSocketListener listener;
	private int port;
	private Router router;
	private volatile boolean running = true;
	
	public MessagingNode(String host, int port) {
		this.conns = new HashMap<String,TCPConnection>();
		this.registry = host+":"+port;
		this.listener = null;
		this.router = null;
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
		Scanner input = new Scanner(System.in);
		while(node.running) {
			if(input.hasNextLine()) {
				String command = input.nextLine().trim();
				if(command.equalsIgnoreCase("quit-overlay")) {
					node.running = false;
					System.out.println("Stopping");
				}else if(command.equalsIgnoreCase("print-shortest-path")) {
					System.out.print(node.router.getShortestPaths());
				}else {
					System.out.println("Command not recognized");
				}
			}
		}
	}
	
	private void createSocket(Node node, String host, int port) {
		TCPConnection conn = new TCPConnection(node, host, port);
		conns.put(host+":"+port, conn);
	}
	
	private void register(){
		try {
			conns.get(registry).sendData(Register.createMessage(listener.getAddress(), port));
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
			System.out.println("Listening on port "+port);
		}else if(e.getType() == 5) {
			LinkWeights lw = (LinkWeights)e;
			System.out.println("MessgingNode Listening on port "+port);
			this.router = new Router(listener.getAddress()+":"+port,lw.getLinks());
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
		this.port = port;
		register();
	}

	@Override
	public void errorListening(String message) {
		// TODO Auto-generated method stub
		
	}

}
