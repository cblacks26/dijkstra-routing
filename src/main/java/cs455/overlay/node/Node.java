package cs455.overlay.node;

import java.io.IOException;

import cs455.overlay.transport.TCPConnection;
import cs455.overlay.wireformats.Event;

public interface Node {

	public void onEvent(Event e, TCPConnection connection) throws IOException;
	
	public void onConnection(TCPConnection connection);
	
	public void onListening(Node node);
	
}
