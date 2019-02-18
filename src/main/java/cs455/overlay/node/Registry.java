package cs455.overlay.node;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import cs455.overlay.transport.ServerSocketListener;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.util.Overlay;
import cs455.overlay.util.OverlayNode;
import cs455.overlay.util.WeightedConnection;
import cs455.overlay.wireformats.Deregister;
import cs455.overlay.wireformats.DeregisterResponse;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.MessagingNodesList;
import cs455.overlay.wireformats.PullTaskSummary;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.TaskComplete;
import cs455.overlay.wireformats.TaskSummary;

public class Registry implements Node{

	private static final Registry mainRegistry = null;
	private ArrayList<OverlayNode> nodes;
	private Overlay overlay = null;
	private int numConns = 4;
	private ServerSocketListener socketListener = null;
	private volatile boolean running;
	
	public Registry(int port) {
		this.nodes = new ArrayList<OverlayNode>();
		this.running = false;
		try {
			this.socketListener = new ServerSocketListener(this,port);
			Thread newListener = new Thread(socketListener);
			newListener.start();
		}catch(UnknownHostException uhe) {
			System.out.println(uhe.getMessage());
		}
	}
	
	public ArrayList<OverlayNode> getNodes(){
		return nodes;
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
					for(OverlayNode node:registry.getNodes()) {
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
						registry.sendOverlayToAllNodes();
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
		// do nothing
	}
	
	public void closeConnections() {
		// unimplemented method
	}
	
	@Override
	public void onEvent(Event e, TCPConnection connection) throws IOException {
		// Register Event
		if(e.getType()==1) {
			Register regReq = (Register) e;
			OverlayNode node = new OverlayNode(regReq.getIPAddress(),regReq.getNodePort(),connection);
			if(nodes.contains(node)) {
				registerResponse(0,"Error Host with that port is already registered",connection);
			}else {
				nodes.add(node);
				registerResponse(1,"Added Node to Messaging node list",connection);
			}
		// DeRegister Event
		}else if(e.getType()==3) {
			Deregister de = (Deregister)e;
			if(connection.getIPAddress().equalsIgnoreCase(de.getIPAddress())) {
				OverlayNode on = new OverlayNode(de.getIPAddress(),de.getNodePort(),connection);
				if(getNodes().contains(on)) {
					getNodes().remove(on);
					connection.sendData(DeregisterResponse.createMessage("Removed from overlay", 1));
				}else {
					connection.sendData(DeregisterResponse.createMessage("Node not found in overlay", 0));
				}
			}else {
				connection.sendData(DeregisterResponse.createMessage("Address does not match connection", 0));
			}
		}else if(e.getType()==7) {
			TaskComplete tc = (TaskComplete)e;
			// wait for 15 seconds
			byte[] message = PullTaskSummary.createMessage();
			connection.sendData(message);
		}else if(e.getType()==9) {
			TaskSummary ts = (TaskSummary)e;
		}else {
			System.out.println("Error should not be recieving messages of this type");
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
		HashMap<OverlayNode,String> messages = new HashMap<OverlayNode,String>();
		for(WeightedConnection wc:overlay.getLinks()) {
			if(messages.containsKey(wc.getFirstNode())) {
				messages.replace(wc.getFirstNode(), messages.get(wc.getFirstNode())+" "+wc.getSecondNode());
			}else {
				messages.put(wc.getFirstNode(), wc.getSecondNode().toString());
			}
		}
		for(OverlayNode node:messages.keySet()) {
			String message = messages.get(node);
			System.out.println(message);
			message.trim();
			int count = message.split(" ").length;
			byte[] bytes = MessagingNodesList.createMessage(messages.get(node), count);
			node.getConnection().sendData(bytes);
		}
	}
	
	private void sendOverlayToAllNodes(){
		String links = "";
		for(WeightedConnection wc:overlay.getLinks()) {
			links+=wc.toString()+" ";
		}
		links.trim();
		try {
			byte[] bytes = LinkWeights.createMessage(links, overlay.getLinks().size());
			for(OverlayNode node:nodes) {
				node.getConnection().sendData(bytes);
			}
		}catch(IOException ioe) {
			System.out.println("Error sending messaging Nodes List: "+ioe.getMessage());
		}
	}
}
