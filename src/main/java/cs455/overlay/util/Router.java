package cs455.overlay.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class Router {

	private HashMap<String,RouterNode> nodes;
	private String[] addresses;
	private Graph graph;
	private String address;
	
	public Router(String addr, String[] links) {
		this.nodes = new HashMap<String,RouterNode>();
		this.address = addr.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
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
		for(RouterNode n:nodes.values()) {
			graph.addNode(n);
		}
		Set<RouterNode> result = graph.calculateShortestPathFromNode(nodes.get(address));
		for(RouterNode n:result) {
			nodes.replace(n.getNodeAddress(), n);
		}
		Iterator<String> it = nodes.keySet().iterator();
		this.addresses = new String[nodes.size()];
		int index = 0;
		while(it.hasNext()) {
			addresses[index] = it.next();
			index++;
		}
	}

	public static void main(String[] args) {
		RouterNode n1 = new RouterNode("1");
		RouterNode n2 = new RouterNode("2");
		RouterNode n3 = new RouterNode("3");
		
		n1.addAdjecentNodes(n2, 4);
		n1.addAdjecentNodes(n3, 8);
		
		n2.addAdjecentNodes(n1, 4);
		n2.addAdjecentNodes(n3, 3);
		
		n3.addAdjecentNodes(n1, 8);
		n3.addAdjecentNodes(n2, 3);
		
		Graph g = new Graph();
		Set<RouterNode> s = new HashSet<RouterNode>();
		s.add(n1);
		s.add(n2);
		s.add(n3);
		for(RouterNode n:s) {
			System.out.println();
		}
		g.setNodes(s);
		Set<RouterNode> result = g.calculateShortestPathFromNode(n1);
		
	}
	
	private String getPathToNode(String addr) {
		String a = addr.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
		RouterNode n = nodes.get(addr);
		String[] p = n.toString().split("--");
		String result = "";
		for(int i = 0;i<p.length;i++) {
			if(i%2==0) result+=p[i];
			else if(i%2==1) result+="-";
		}
		return result;
	}
	
	public String getRandomPathToNode() {
		Random rand = new Random();
		int index = rand.nextInt(addresses.length-1);
		if(addresses[index].equalsIgnoreCase(address)) {
			if(index<addresses.length-1) {
				index++;
			}else {
				index--;
			}
		}
		return getPathToNode(addresses[index]);
	}
	
	public void printShortestPaths() {
		for(RouterNode node:graph.getNodes()) {
			if(!node.getNodeAddress().equalsIgnoreCase(address)) {
				System.out.println(node.toString());
			}
		}
	}
}
