package cs455.overlay.node;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import cs455.overlay.transport.ServerSocketListener;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.util.Overlay;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Register;

public class Registry implements Node{

	private static Registry mainRegistry = null;
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
				String command = input.nextLine();
				if(command.equalsIgnoreCase("quit")) {
					registry.running = false;
					System.out.println("Stopping");
				} else if(command.equalsIgnoreCase("list-messaging-nodes")){
					
				} else if(command.equalsIgnoreCase("list-weights")){
					if(registry.overlay==null) {
						System.out.println("Overlay must be setup to list the weights");
					}else {
						// print weights between nodes
					}
					
				} else if(command.equalsIgnoreCase("setup-overlay")){
					if(input.hasNextLine()) {
						try {
							registry.numConns = Integer.parseInt(input.nextLine());
						}catch(NumberFormatException ne) {
							System.out.println("Cannot convert number-of-connections argument into integer");
						}
					}
					registry.setupOverlay();
				} else {
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
		byte[] infoBytes = info.getBytes();
		byte[] marshallBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));
		dos.writeInt(2);
		dos.writeInt(result);
		dos.write(infoBytes);
		dos.flush();
		marshallBytes = baos.toByteArray();
		baos.close();
		dos.close();
		conn.sendData(marshallBytes);
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
			System.out.println("Recieved a Register Request");
			String key = regReq.getIPAddress()+":"+regReq.getNodePort();
			if(nodes.contains(key)) {
				registerResponse(0,"Error Host with that port is already registered",connection);
			}else {
				// Add the messenger node to the list
				nodes.add(key);
				registerResponse(1,"Added Node to Messaging node list",connection);
			}
			// Find nodes to connect to and add them to values
			
			// 1 is a SUCCESS 0 is a FAILURE
			registerResponse(1,"",connection);
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
		}
	}
}
