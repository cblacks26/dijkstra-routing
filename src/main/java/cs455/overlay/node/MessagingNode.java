package cs455.overlay.node;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MessagingNode implements Node{

	public static void main(String args[]) throws UnknownHostException {
		String address = InetAddress.getLocalHost().getHostName().toString();
		
	}
	
	@Override
	public void onEvent() {
		// TODO Auto-generated method stub
		
	}

}
