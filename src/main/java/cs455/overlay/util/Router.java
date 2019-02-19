package cs455.overlay.util;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Router {

	private HashMap<String,RouterNode> nodes;
	private Graph graph;
	private String address;
	
	public Router(String addr, String[] links) {
		this.nodes = new HashMap<String,RouterNode>();
		this.address = address.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
		this.nodes.put(address, new RouterNode(address));
		for(String link:links) {
			String[] parts = link.split(",");
			int weight = Integer.parseInt(parts[2]);
			String node1 = parts[0].replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
			String node2 = parts[1].replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
			RouterNode n1 = null;
			RouterNode n2 = null;
			if(nodes.containsKey(node1)) {
				n1 = nodes.get(node1);
			}else {
				n1 = new RouterNode(node1);
				nodes.put(node1, n1);
			}
			if(nodes.containsKey(node2)) {
				n2 = nodes.get(node2);
			}else {
				n2 = new RouterNode(node2);
				nodes.put(node2, n2);
			}
			n1.addAdjecentNodes(n2, weight);
			n2.addAdjecentNodes(n1, weight);
		}
		this.graph = new Graph();
		this.graph.setNodes((Set<RouterNode>)nodes.values());
		this.graph = Graph.calculateShortestPathToNode(graph, nodes.get(address));
		
	}

	
	public void printShortestPaths() {
		for(RouterNode node:graph.getNodes()) {
			if(!node.getNodeAddress().equalsIgnoreCase(address)) {
				System.out.println(node.toString());
			}
		}
	}
}
