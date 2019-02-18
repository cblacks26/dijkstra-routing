package cs455.overlay.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

public class Router {

	private RouterNode node;
	private ArrayList<String> places;
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
		for(RouterNode n:node.getNodes().keySet()) {
			for(String addr:nodes.keySet()) {
				Route path = node.pathToNode(addr, node.getNodes().get(n));
				if(path!=null) {
					if(paths.containsKey(addr)) {
						if(paths.get(addr).getTotalWeight()>path.getTotalWeight()) paths.replace(addr,path);
					}else {
						paths.put(addr, path);
					}
				}
				resetVisitedNodes();
			}
		}
		
	}

	public Route getRandomPath() {
		Random rand = new Random();
		int count = rand.nextInt(paths.size());
		Iterator<Entry<String,Route>> it = paths.entrySet().iterator();
		Entry<String, Route> prev = (Entry<String,Route>)it.next();
		Entry<String, Route> curr = (Entry<String,Route>)it.next();
		while(count > 0) {
			it.next();
		}
		if(curr.getKey().equalsIgnoreCase(node.getNodeAddress())) return prev.getValue();
		else return curr.getValue();
	}
	
	public String getShortestPaths() {
		String out = "";
		for(Route route:paths.values()) {
			out+=route.printPath()+"\n";
		}
		return out;
	}
	
}
