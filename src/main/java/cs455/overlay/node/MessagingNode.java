package cs455.overlay.node;

import java.io.IOException;
import java.net.UnknownHostException;
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
					node.deregister();
					System.out.println("Sent deregister message");
				}else if(command.equalsIgnoreCase("print-shortest-path")) {
					System.out.print(node.router.getShortestPaths());
				}else {
					System.out.println("Command not recognized");
				}
			}
		}
		input.close();
	}
	
	private void createSocket(Node node, String host, int port) {
		TCPConnection conn = new TCPConnection(node, host, port);
		conns.put(host+":"+port, conn);
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
			this.router = new Router(node,lw.getLinks());
			System.out.println("Link weights are received and processed. Ready to send messages.");
		} else if(e.getType() == 6) {
			System.out.println("Recieved TaskInitiate");
			TaskInitiate ti = (TaskInitiate)e;
			sendMessages(ti.getNumberOfRounds());
			System.out.println("Passed send Messages");
			// start sending messages
		}else if(e.getType() == 8) {
			PullTaskSummary pts = (PullTaskSummary)e;
			conns.get(registry).sendData(TaskSummary.createMessage(address, port, getAndResetNumberSent(), getAndResetSumSent(), 
					getAndResetNumberRecieved(), getAndResetSumRecieved(), getAndResetNumberRelayed()));
		}else if(e.getType() == 10) {
			System.out.println("Recieved Message");
			Message m = (Message)e;
			incrementNumberRecieved();
			String[] links = m.getPath().split("-");
			int index = findNodeIndex(links);
			if(index==links.length) {
				addSumRecieved(m.getNumber());
				// target address
			}else {
				conns.get(links[index+1]).sendData(m.getBytes());
				incrementNumberRelayed();
				//relay message
			}
			// if relay route detected check if needs to be sent
		}else if(e.getType() == 11) {
			DeregisterResponse dr = (DeregisterResponse)e;
			if(dr.getResult()==1) {
				System.out.println(dr.getExtraInfo());
				this.running = false;
			}else {
				System.out.println(dr.getExtraInfo());
			}
		}else {
			System.out.println("Error should not be recieving messages of this type");
		}
	}

	private int findNodeIndex(String[] links) {
		for(int i = 0;i<links.length;i++) {
			if(node.equalsIgnoreCase(links[i]))return i;
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
	
	private void incrementNumberRelayed() {
		this.numRelay++;
	}
	
	private int getAndResetNumberRelayed() {
		int temp = this.numRelay;
		this.numRelay = 0;
		return temp;
	}
	
	private void incrementNumberRecieved() {
		this.numRec++;
	}
	
	private int getAndResetNumberRecieved() {
		int temp = numRec;
		this.numRec = 0;
		return temp;
	}
	
	private void setNumberSent(int sent) {
		this.numSent = sent;
	}
	
	private int getAndResetNumberSent() {
		int temp = numSent;
		this.numSent = 0;
		return temp;
	}
	
	private void sendMessages(int numberRounds) {
		TCPConnection conn = null;
		Random rand = new Random();
		for(int i = 0; i < numberRounds;i++) {
			int num = rand.nextInt();
			try {
				conn.sendData(Message.createMessage(router.getRandomPath().toString(), num));
			} catch (IOException e) {
				e.printStackTrace();
			}
			addSumSent(num);
		}
		setNumberSent(numberRounds);
		try {
			conns.get(registry).sendData(TaskComplete.createMessage(address, port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onConnection(TCPConnection connection) {
		conns.put(connection.getIPAddress()+":"+connection.getListeningPort(),connection);
	}

	@Override
	public void onListening(int port){
		this.port = port;
		this.address = listener.getAddress().replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
		this.node = (address+":"+port);
		register();
	}

	@Override
	public void errorListening(String message) {
		// TODO Auto-generated method stub
		
	}
}