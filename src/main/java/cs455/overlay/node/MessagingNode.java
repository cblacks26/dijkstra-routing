package cs455.overlay.node;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import cs455.overlay.transport.ServerSocketListener;
import cs455.overlay.transport.TCPConnection;
import cs455.overlay.util.Router;
import cs455.overlay.wireformats.Deregister;
import cs455.overlay.wireformats.DeregisterResponse;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.LinkWeights;
import cs455.overlay.wireformats.Message;
import cs455.overlay.wireformats.MessagingNodesList;
import cs455.overlay.wireformats.PullTaskSummary;
import cs455.overlay.wireformats.Register;
import cs455.overlay.wireformats.RegisterResponse;
import cs455.overlay.wireformats.TaskComplete;
import cs455.overlay.wireformats.TaskInitiate;
import cs455.overlay.wireformats.TaskSummary;

public class MessagingNode implements Node{

	private String registry;
	private String node;
	private String address;
	private HashMap<String,TCPConnection> conns;
	private ServerSocketListener listener;
	private int port;
	private Router router;
	private int numSent;
	private int numRec;
	private long sumSent;
	private long sumRec;
	private int numRelay;
	private volatile boolean running = true;
	
	public MessagingNode(String host, int port) {
		this.conns = new HashMap<String,TCPConnection>();
		this.listener = null;
		this.router = null;
		TCPConnection con = createSocket(this,host,port);
		this.registry = (con.getIPAddress()+":"+con.getListeningPort()).replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
		conns.put(registry, con);
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
					node.deregister();
					System.out.println("Sent deregister message");
				}else if(command.equalsIgnoreCase("print-shortest-path")) {
					node.router.printShortestPaths();
				}else {
					System.out.println("Command not recognized");
				}
			}
		}
		input.close();
	}
	
	private TCPConnection createSocket(Node node, String host, int port) {
		TCPConnection conn = new TCPConnection(node, host, port);
		return conn;
	}
	
	private void deregister() {
		try {
			conns.get(registry).sendData(Deregister.createMessage(address, port));
		}catch(IOException ioe) {
			System.out.println("Error sending deregister message to registry: "+ioe.getMessage());
		}
	}
	
	private void register(){
		try {
			conns.get(registry).sendData(Register.createMessage(address, port));
		}catch(IOException ioe) {
			System.out.println("Error sending register message to registry: "+ioe.getMessage());
		}
	}
	
	@Override
	public void onEvent(Event e, TCPConnection conn) throws IOException {
		if(e.getType()==1) {
			Register regReq = (Register) e;
			String addr = (regReq.getIPAddress()+":"+regReq.getNodePort()).replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
			if(!conns.containsKey(addr)) {
				conns.put(addr, conn);
				conn.sendData(RegisterResponse.createMessage("Connected and registered successfully",1));
			}else conn.sendData(RegisterResponse.createMessage("Already connected",0));
		}else if(e.getType()==2) {
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
				String addr = node.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
				conns.put(addr, createSocket(this,info[0],Integer.parseInt(info[1])));
				conns.get(addr).sendData(Register.createMessage(address,port));
			}
			System.out.println("All connections are established. Number of connections: "+mnl.getNumberOfNodes());
			System.out.println("Listening on port "+port);
		}else if(e.getType() == 5) {
			LinkWeights lw = (LinkWeights)e;
			this.router = new Router(node,lw.getLinks());
			System.out.println("Link weights are received and processed. Ready to send messages.");
		} else if(e.getType() == 6) {
			System.out.println("Recieved TaskInitiate");
			TaskInitiate ti = (TaskInitiate)e;
			System.out.println("about to send rounds");
			sendMessages(ti.getNumberOfRounds());
			// start sending messages
		}else if(e.getType() == 8) {
			System.out.println("Recieved pull task summary");
			PullTaskSummary pts = (PullTaskSummary)e;
			conns.get(registry).sendData(TaskSummary.createMessage(address, port, getAndResetNumberSent(), getAndResetSumSent(), 
					getAndResetNumberRecieved(), getAndResetSumRecieved(), getAndResetNumberRelayed()));
		}else if(e.getType() == 10) {
			System.out.println("Recieved Message");
			Message m = (Message)e;
			incrementNumberRecieved();
			String[] links = m.getPath().split("-");
			int index = findNodeIndex(links);
			System.out.println("Found index "+index+" of "+links.length);
			if(index==links.length-1) {
				System.out.println("Triggered reception statement");
				addSumRecieved(m.getNumber());
				// target address
			}else {
				System.out.println("Triggered relay statement");
				findConnection(links[index+1]).sendData(m.getBytes());
				incrementNumberRelayed();
				//relay message
			}
		}else if(e.getType() == 11) {
			DeregisterResponse dr = (DeregisterResponse)e;
			if(dr.getResult()==1) {
				System.out.println(dr.getExtraInfo());
				this.running = false;
				for(TCPConnection con:conns.values()) {
					con.closeConnection();
				}
				System.exit(0);
			}else {
				System.out.println(dr.getExtraInfo());
			}
		}else {
			System.out.println("Error should not be recieving messages of this type");
		}
	}
	// tester method
	private TCPConnection findConnection(String addr) {
		for(String s:conns.keySet()) {
			System.out.println(s+" - "+addr+"  -  Lengths: "+s.length()+" : "+addr.length());
			if(s.equalsIgnoreCase(addr)) return conns.get(s);
		}
		System.out.println("Returning null");
		return null;
	}

	private int findNodeIndex(String[] links) {
		for(int i = 0;i<links.length;i++) {
			String adr = links[i].replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
			if(node.equalsIgnoreCase(adr))return i;
		}
		return -1;
	}
	
	private void addSumSent(int sum) {
		this.sumSent+=sum;
	}
	
	private long getAndResetSumSent() {
		long temp = sumSent;
		this.sumSent = 0;
		return temp;
	}
	
	private void addSumRecieved(int sum) {
		this.sumRec+=sum;
	}
	
	private long getAndResetSumRecieved() {
		long temp = sumRec;
		this.sumRec = 0;
		return temp;
	}
	
	private synchronized void incrementNumberRelayed() {
		this.numRelay++;
	}
	
	private int getAndResetNumberRelayed() {
		int temp = this.numRelay;
		this.numRelay = 0;
		return temp;
	}
	
	private synchronized void incrementNumberRecieved() {
		this.numRec++;
	}
	
	private int getAndResetNumberRecieved() {
		int temp = numRec;
		this.numRec = 0;
		return temp;
	}
	
	private synchronized void setNumberSent(int sent) {
		this.numSent = sent;
	}
	
	private int getAndResetNumberSent() {
		int temp = numSent;
		this.numSent = 0;
		return temp;
	}
	
	private void sendMessages(int numberRounds) {
		Random rand = new Random();
		for(int i = 0; i < numberRounds;i++) {
			String path = router.getRandomPathToNode();
			String[] links = path.split("-");
			System.out.println(Arrays.toString(links));
			TCPConnection con = findConnection(links[1]);
			for(int j = 0; j < 5; j++) {
				int num = rand.nextInt();
				try {
					con.sendData(Message.createMessage(path, num));
				} catch (IOException e) {
					e.printStackTrace();
				}
				addSumSent(num);
			}
		}
		System.out.println("sent messages");
		setNumberSent(numberRounds);
		try {
			conns.get(registry).sendData(TaskComplete.createMessage(address, port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onListening(int port){
		this.port = port;
		this.address = listener.getAddress().replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
		this.node = (address+":"+port).replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
		register();
	}

	@Override
	public void errorListening(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnection(TCPConnection connection) {
		// TODO Auto-generated method stub
		
	}
}