package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EventFactory{

	public static Event create(int dataLength, byte[] bytes) throws IOException, Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(new BufferedInputStream(bais));
		int type = dis.readInt();
		System.out.println("Recieved message of type "+type);
		Event event = null;
		if(type == 1) {
			event = new Register(bytes);
		}else if (type == 2) {
			event = new RegisterResponse(bytes);
		}else if (type == 3){
			event = new Deregister(bytes);
		}else if (type == 4) {
			event = new MessagingNodesList(bytes);
		}else if (type == 5) {
			event = new LinkWeights(bytes);
		}else if(type == 6){
			event = new TaskInitiate(bytes);
		}else if(type == 7){
			event = new TaskComplete(bytes);
		}else if(type == 8){
			event = new PullTaskSummary(null);
		}else if(type == 9){
			event = new TaskSummary(bytes);
		}else if(type == 10){
			event = new Message(bytes);
		}else if(type == 11){
			event = new DeregisterResponse(bytes);
		}else {
			System.out.println("Recieved message of type "+type);
			throw new Exception("Unrecognized Message Type "+type);
		}
		dis.close();
		bais.close();
		return event;
	}
	
}
