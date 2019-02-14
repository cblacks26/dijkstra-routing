package cs455.overlay.node;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import cs455.overlay.transport.ServerSocketListener;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.util.Overlay;
import cs455.overlay.util.WeightedConnection;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.MessagingNodesList;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.RegisterResponse;

public class Registry implements Node{

	private static final Registry mainRegistry = null;
	private ArrayList<String> nodes;
	private Overlay overlay = null;
	private int numConns = 4;
	private ServerSocketListener socketListener = null;
	private HashMap<String,TCPConnection> connections;
	private volatile boolean running;
	
	public Registry(int port) {
		this.nodes = new ArrayList<String>();
		this.running = false;
		this.connections = new HashMap<String,TCPConnection>();
		try {
			this.socketListener = new ServerSocketListener(this,port);
			Thread newListener = new Thread(socketListener);
			newListener.start();
		}catch(UnknownHostException uhe) {
			System.out.println(uhe.getMessage());
		}
	}
	
	public static void main(String[] args) throws UnknownHostException {
		int portArg = 0;
		if(args.length != 1) {
			System.out.println("Must have a port specified!");
		}else {
			portArg = Integer.parseInt(args[0]);
		}
		Registry registry = new Registry(portArg);
		registry.running = true;
		Scanner input = new Scanner(System.in);
		while(registry.running) {
			if(input.hasNextLine()) {
				String command = input.nextLine().trim();
				if(command.equalsIgnoreCase("list-messaging-nodes")){
					for(String node:registry.nodes) {
						System.out.println(node);
					}
				} else if(command.equalsIgnoreCase("list-weights")){
					if(registry.overlay==null) {
						System.out.println("Overlay must be setup to list the weights");
					}else {
						for(WeightedConnection wc:registry.overlay.getLinks()) {
							System.out.println(wc);
						}
					}
				} else if(command.contains("setup-overlay")){
					if(Character.isDigit(command.charAt(command.length()-1))&&command.contains(" ")) {
						String[] split = command.split(" ");
						try {
							registry.numConns = Integer.parseInt(split[1]);
						}catch(NumberFormatException ne) {
							System.out.println("Cannot convert number-of-connections argument into integer");
						}
					}
					registry.setupOverlay();
				}else if(command.equalsIgnoreCase("send-overlay-link-weights")){
					if(registry.overlay==null) {
						System.out.println("Overlay must be setup to list the weights");
					}else {
						registry.sendOverlay();
						System.out.println("Sent Overlay");
					}
				} else if(command.contains("start")){
					if(Character.isDigit(command.charAt(command.length()-1))&&command.contains(" ")) {
						String[] split = command.split(" ");
						try {
							int rounds = Integer.parseInt(split[1]);
							// start rounds
						}catch(NumberFormatException ne) {
							System.out.println("Cannot convert number-of-connections argument into integer");
						}
					}
				}else {
					System.out.println("Command Not recognized");
				}
			}
		}
		registry.closeConnections();
		input.close();
	}
	
	public static Registry getRegistry() {
		return mainRegistry;
	}

	public ServerSocketListener getServerSocketListener() {
		return socketListener;
	}
	
	public void registerResponse(int result, String info, TCPConnection conn) throws IOException {
		conn.sendData(RegisterResponse.createMessage(info, result));
	}
	
	public void onListening(int port) {
		System.out.println("Listening to port "+port);
	}
	
	public void onConnection(TCPConnection connection) {
		String ip = connection.getIPAddress();
		int port = connection.getListeningPort();
		connections.put(ip+":"+port, connection);
	}
	
	public void closeConnections() {
		for(TCPConnection conn:connections.values()) {
			conn.closeConnection();
		}
	}
	
	@Override
	public void onEvent(Event e, TCPConnection connection) throws IOException {
		// Register Event
		if(e.getType()==1) {
			Register regReq = (Register) e;
			String key = regReq.getIPAddress()+":"+regReq.getNodePort();
			System.out.println("Recieved a Register Request from "+key);
			if(nodes.contains(key)) {
				registerResponse(0,"Error Host with that port is already registered",connection);
			}else {
				nodes.add(key);
				registerResponse(1,"Added Node to Messaging node list",connection);
			}
		// DeRegister Event
		}else if(e.getType()==3) {
			
		// Message Event
		}else if(e.getType()==4) {
			
		}else {
			
		}
	}

	@Override
	public void errorListening(String message) {
		System.out.println(message);
		System.exit(1);
	}
	
	private void setupOverlay() {
		if(nodes.size()<numConns) {
			System.out.println("Error must have atleast "+numConns +" to setup the overlay");
		}else {
			this.overlay = new Overlay(nodes, numConns);
			System.out.println(overlay.toString());
			try {
				sendLinkCommand();
				System.out.println("Sent Link Command");
			}catch(IOException ioe) {
				System.out.println("Error sending messaging nodes list or Overlay "+ioe.getMessage());
				System.exit(1);
			}
		}
	}
	
	private void sendLinkCommand() throws IOException {
		for(String node:connections.keySet()) {
			int numLinks = 0;
			String links = "";
			for(WeightedConnection wc:overlay.getLinks()) {
				if(wc.getFirstNode().equalsIgnoreCase(node)) {
					numLinks++;
					links+=wc.getSecondNode()+" ";
				}
			}
			if(numLinks>0) {
				links.trim();
				connections.get(node).sendData(MessagingNodesList.createMessage(links, numLinks));
			}
		}
	}
	
	private void sendOverlay(){
		String links = "";
		for(WeightedConnection wc:overlay.getLinks()) {
			links+=wc.getFirstNode()+","+wc.getSecondNode()+","+wc.getWeight()+" ";
		}
		links.trim();
		try {
			byte[] bytes = LinkWeights.createMessage(links, overlay.getLinks().size());
			for(TCPConnection con:connections.values()) {
				con.sendData(bytes);
			}
		}catch(IOException ioe) {
			System.out.println("Error sending messaging Nodes List: "+ioe.getMessage());
		}
	}
}
