package cs455.overlay.util;

import java.util.ArrayList;
import java.util.HashMap;

public class Overlay {

	private HashMap<String,Integer> graph;
	private ArrayList<WeightedConnection> conns;
	
	public Overlay(ArrayList<String> nodes, int numberConnections) {
		this.conns = new ArrayList<WeightedConnection>();
		this.graph = new HashMap<String,Integer>();
		setup(nodes,numberConnections);
	}
	
	public void setup(ArrayList<String> nodes, int numberConnections) {
		for(String node:nodes) {
			graph.put(node,0);
		}
		for(int i = 0; i < nodes.size();i++) {
			String node = nodes.get(i);
			for(int k = graph.get(node); k<numberConnections;k++) {
				String newCon;
				try {
					newCon = findNextLink(node,numberConnections);
				}catch(Exception e) {
					System.out.println(e.getMessage());
					return;
				}
				WeightedConnection wc = new WeightedConnection(node,newCon);
				conns.add(wc);
				graph.replace(node, graph.get(node)+1);
				graph.replace(newCon, graph.get(newCon)+1);
			}
		}
	}
	
	public String findNextLink(String base,int numberConnections) throws Exception {
		int min = Integer.MAX_VALUE;
		String result = "";
		for(String node:graph.keySet()) {
			if(!node.equalsIgnoreCase(base)) {
				int n = graph.get(node);
				if(n<min) {
					min = n;
					result = node;
				}
			}
		}
		if(min>numberConnections) throw new Exception("Overlay exceeded number of connections");
		return result;
	}
	
	public ArrayList<WeightedConnection> getLinks(){
		return conns;
	}
	
	public String toString() {
		String result = "";
		for(WeightedConnection wc:conns) {
			result+=wc+"\n";
		}
		return result;
	}
}
