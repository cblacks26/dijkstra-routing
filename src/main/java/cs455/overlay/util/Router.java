package cs455.overlay.util;

import java.util.Arrays;
import java.util.HashMap;

public class Router {

	private RouterNode node;
	private HashMap<String,RouterNode> nodes;
	private HashMap<String,Route> paths;
	
	public Router(String address, String[] links) {
		this.node = new RouterNode(address.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim());
		this.nodes = new HashMap<String,RouterNode>();
		this.paths = new HashMap<String,Route>();
		for(String link:links) {
			String[] parts = link.split(",");
			int weight = Integer.parseInt(parts[2]);
			String node1 = parts[0].replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
			String node2 = parts[1].replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
			RouterNode n1 = null;
			RouterNode n2 = null;
			if(node.getNodeAddress().equalsIgnoreCase(node1)) {
				n1 = node;
			}else if(nodes.containsKey(node1)) {
				n1 = nodes.get(node1);
			}else {
				n1 = new RouterNode(node1);
				nodes.put(node1, n1);
			}
			if(node.getNodeAddress().equalsIgnoreCase(node2)) {
				n2 = node;
			}else if(nodes.containsKey(node2)) {
				n2 = nodes.get(node2);
			}else {
				n2 = new RouterNode(node2);
				nodes.put(node2, n2);
			}
			n1.addNode(n2, weight);
			n2.addNode(n1, weight);
		}
		createPaths();
		System.out.println(getShortestPaths());
	}
	
	private void resetVisitedNodes() {
		node.resetVisits();
		for(String address:nodes.keySet()) {
			nodes.get(address).resetVisits();
		}
	}
	
	public void createPaths() {
		for(String address:nodes.keySet()) {
			Route path = node.pathToNode(address, 0);
			paths.put(address,path);
			resetVisitedNodes();
		}
	}
	
	public Route getPathToAddress(String address) {
		return paths.get(address);
	}
	
	public String getShortestPaths() {
		String out = "";
		for(Route route:paths.values()) {
			out+=route.printPath()+"/n";
		}
		return out;
	}
	
}
