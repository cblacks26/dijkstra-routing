package cs455.overlay.node;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

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
import cs455.overlay.wireformats.TaskInitiate;
import cs455.overlay.wireformats.TaskSummary;

public class Registry implements Node{

	private static final Registry mainRegistry = null;
	private ArrayList<OverlayNode> nodes;
	private ArrayList<TaskSummary> summaries;
	private int tasksCompleted = 0;
	private int port;
	private String address;
	private Overlay overlay = null;
	private int numConns = 4;
	private ServerSocketListener socketListener = null;
	private volatile boolean running;
	
	public Registry(int port) {
		this.nodes = new ArrayList<OverlayNode>();
		this.summaries = new ArrayList<TaskSummary>();
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
							registry.sendTaskInitiate(rounds);
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
		conn.getSender().sendData(RegisterResponse.createMessage(info, result));
	}
	
	public void onListening(int port) {
		this.port = port;
		this.address = this.socketListener.getAddress().replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
		System.out.println("Listening to port "+port);
	}
	
	public void onConnection(TCPConnection connection) {
		// do nothing
	}
	
	public void closeConnections() {
		// unimplemented method
	}
	
	//tester method
	private boolean stringEquals(String s, String s2) {
		if(s.length()!=s2.length()) {
			System.out.println("Lengths not equal: "+s.length()+" - "+s2.length());
			return false;
		}else {
			return s.equals(s2);
		}
	}
	
	// helper and tester method
	private boolean findAndRemove(OverlayNode on) {
		for(OverlayNode n:nodes) {
			System.out.println(n);
			if(n.equals(on)) {
				nodes.remove(n);
				return true;
			}
		}
		return false;
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
			// stringEquals(connection.getIPAddress(),de.getIPAddress());
			// System.out.println(connection.getIPAddress()+"  "+connection.getIPAddress().length()+" -  "+de.getIPAddress()+" "+de.getIPAddress().length());
			if(connection.getIPAddress().equalsIgnoreCase(de.getIPAddress())) {
				OverlayNode on = new OverlayNode(de.getIPAddress(),de.getNodePort(),connection);
				boolean result = findAndRemove(on);
				if(result) {
					connection.getSender().sendData(DeregisterResponse.createMessage("Removed from overlay", 1));
				}else {
					connection.getSender().sendData(DeregisterResponse.createMessage("Node not found in overlay", 0));
				}
			}else {
				connection.getSender().sendData(DeregisterResponse.createMessage("Address does not match connection", 0));
			}
		}else if(e.getType()==7) {
			TaskComplete tc = (TaskComplete)e;
			tasksCompleted++;
			System.out.println("Recieved task Complete");
			if(tasksCompleted==nodes.size()) {
				try {
					// wait for 15 seconds
					TimeUnit.SECONDS.sleep(15);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				};
				System.out.println("sent pull task summary");
				byte[] message = PullTaskSummary.createMessage();
				for(OverlayNode n:nodes) {
					n.getConnection().getSender().sendData(message);
				}
			}
			
		}else if(e.getType()==9) {
			TaskSummary ts = (TaskSummary)e;
			System.out.println("Recieved task summary from "+ts.getIpAddress());
			summaries.add(ts);
			if(summaries.size()==nodes.size()) {
				printSummary();
			}
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
			message.trim();
			int count = message.split(" ").length;
			byte[] bytes = MessagingNodesList.createMessage(messages.get(node), count);
			node.getConnection().getSender().sendData(bytes);
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
				node.getConnection().getSender().sendData(bytes);
			}
		}catch(IOException ioe) {
			System.out.println("Error sending messaging Nodes List: "+ioe.getMessage());
		}
	}
	
	private void sendTaskInitiate(int numberOfRounds) {
		byte[] data = null;
		try {
			data = TaskInitiate.createMessage(numberOfRounds);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(OverlayNode on:nodes) {
			on.getConnection().getSender().sendData(data);
		}
	}
	
	private void printSummary() {
		System.out.println("Node | Number Messages Sent | Number Messages Recieved | Summation Sent Messgage | Summation Recieved Messages | Number Relayed Messages");
		long sumSent = 0;
		long sumRec = 0;
		long numSent = 0;
		long numRec = 0;
		long numRel = 0;
		for(TaskSummary ts:summaries) {
			sumSent+=ts.getSummationSentMessages();
			sumRec+=ts.getSummationRecievedMessages();
			numSent+=ts.getNumberSentMessages();
			numRec+=ts.getNumberRecievedMessages();
			numRel+=ts.getNumberRelayedMessages();
			System.out.println(getSummary(ts));
		}
		System.out.println("Sum | "+numSent+" | "+numRec+" | "+sumSent+" | "+sumRec+" | "+numRel);
	}
	
	private String getSummary(TaskSummary ts) {
		String row = ts.getIpAddress()+":"+ts.getPort()+" | ";
		row+=ts.getNumberSentMessages()+" | "+ts.getNumberRecievedMessages()+" | ";
		row+=ts.getSummationSentMessages()+" | "+ts.getSummationRecievedMessages()+" | ";
		row+=ts.getNumberRelayedMessages();
		return row;
	}
}
