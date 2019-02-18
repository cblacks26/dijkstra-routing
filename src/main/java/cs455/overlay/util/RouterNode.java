package cs455.overlay.util;

import java.util.ArrayList;
import java.util.HashMap;

public class RouterNode {
	
	private String address;
	private boolean visited;
	private HashMap<RouterNode,Integer> nodes;
	
	public RouterNode(String address) {
		this.address = address;
		this.visited = false;
		this.nodes = new HashMap<RouterNode,Integer>();
	}
	
	public Route pathToNode(String destination, int weight) {
		if(address.equalsIgnoreCase(destination)) {
			return new Route(this,weight);
		}else if(hasBeenVisited()) {
			return null;
		}else {
			visit();
			ArrayList<Route> routes = new ArrayList<Route>();
			for(RouterNode node:nodes.keySet()) {
				routes.add(node.pathToNode(address,nodes.get(node)));
			}
			Route best = null;
			for(Route route:routes) {
				if(route!=null) {
					if(best==null&&route!=null) {
						best = route;
					}else if(best.getTotalWeight()>route.getTotalWeight()) {
						best = route;
					}
				}
			}
			if(best!=null) best.addNodeToRoute(this, weight);
			return best;
		}
	}
	
	public HashMap<RouterNode,Integer> getNodes(){
		return nodes;
	}
	
	public void resetVisits() {
		visited = false;
	}
	
	public boolean hasBeenVisited() {
		return visited;
	}
	
	public void visit() {
		visited = true;
	}
	
	public String getNodeAddress() {
		return address;
	}
	
	public void addNode(RouterNode node, int weight) {
		nodes.put(node, weight);
	}
	
	public boolean equals(Object o) {
		if(o instanceof RouterNode) {
			return equals((RouterNode)o);
		}
		return false;
	}
	
	public boolean equals(RouterNode node) {
		return getNodeAddress().equalsIgnoreCase(node.getNodeAddress());
	}
	
	public boolean equals(RouterNode node1, RouterNode node2) {
		return node1.equals(node2);
	}
	
	public String toString() {
		return address;
	}
}
