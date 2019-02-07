package cs455.overlay.node;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

import cs455.overlay.transport.ServerSocketListener;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.Register;

public class Registry implements Node{

	private static Registry mainRegistry = null;
	private HashMap<String,String[]> nodes;
	private ServerSocketListener socketListener = null;
	private HashMap<String,TCPConnection> connections;
	private volatile boolean running;
	
	public Registry() {
		this.nodes = new HashMap<String,String[]>();
		this.running = false;
		this.connections = new HashMap<String,TCPConnection>();
		try {
			this.socketListener = new ServerSocketListener(this);
		}catch(UnknownHostException uhe) {
			System.out.println(uhe.getMessage());
		}
		
	}
	
	public static void main(String[] args) throws UnknownHostException {
		Registry registry = new Registry();
		registry.running = true;
		System.out.println("Listening on port "+registry.socketListener.getPort());
		Scanner input = new Scanner(System.in);
		while(registry.running) {
			if(input.hasNextLine()) {
				String command = input.nextLine();
				if(command.equalsIgnoreCase("quit")) {
					registry.running = false;
					System.out.println("Stopping");
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
	
	public void registerResponse(int result, TCPConnection conn) throws IOException {
		String info = "";
		byte[] infoBytes = info.getBytes();
		int msgLength = infoBytes.length+8;
		byte[] marshallBytes = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(baos));
		dos.writeInt(msgLength);
		dos.writeInt(2);
		dos.writeInt(result);
		dos.write(infoBytes);
		dos.flush();
		marshallBytes = baos.toByteArray();
		baos.close();
		dos.close();
		conn.sendData(marshallBytes);
	}
	
	public void onListening(Node node) {
		Registry reg = (Registry)node;
		System.out.println("Listening on port "+reg.socketListener.getPort());
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
			// Find nodes to connect to and add them to values
			String[] values = new String[4];
			// Add the messenger node to the list
			nodes.put(key, values);
			// 1 is a SUCCESS 0 is a FAILURE
			registerResponse(1,connection);
		// DeRegister Event
		}else if(e.getType()==3) {
			
		// Message Event
		}else if(e.getType()==4) {
			
		}else {
			
		}
	}
}
