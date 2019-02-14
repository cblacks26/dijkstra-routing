package cs455.overlay.util;

import java.util.ArrayList;
import java.util.HashMap;

public class Router {

	private HashMap<String,ArrayList<String>> graph;
	
	public Router(String node, String[] links) {
		this.graph = new HashMap<String,ArrayList<String>>();
		for(String link:links) {
			String[] parts = link.split(",");
			ArrayList<String> list = new ArrayList<String>();
			list.add(parts[1]+" "+parts[2]);
			graph.put(parts[0], list);
		}
	}
	
}
