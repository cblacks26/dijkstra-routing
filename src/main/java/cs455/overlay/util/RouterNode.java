package cs455.overlay.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class RouterNode {
	
	private String address;
	private List<RouterNode> shortestPath = new LinkedList<>();
	private List<Integer> pathWeights = new LinkedList<>();
	private HashMap<RouterNode,Integer> adjacentNodes = new HashMap<>();
	private int distance = Integer.MAX_VALUE;
	
	public RouterNode(String address) {
		this.address = address;
	}

	public String getNodeAddress() {
		return address;
	}
	
	public void addAdjecentNodes(RouterNode node, int distance) {
		adjacentNodes.put(node, distance);
	}
	
	public void addNodeToShortestPath(RouterNode node, int weight) {
		shortestPath.add(node);
		pathWeights.add(weight);
		distance+=weight;
	}
	
	public int getDistance() {
		return distance;
	}
	
	public void setDistance(int dist) {
		distance = dist;
	}
	
	public String toString() {
		String result = "";
		for(int i = 0; i<shortestPath.size()-1;i++) {
			result+=shortestPath.get(i)+"--"+pathWeights.get(i)+"--";
		}
		result+=shortestPath.get(shortestPath.size()-1);
		return result;
	}

	public HashMap<RouterNode,Integer> getAdjacentNodes() {
		return adjacentNodes;
	}
	
	public List<RouterNode> getShortestPath(){
		return shortestPath;
	}
	
	public List<Integer> getShortestPathWeights(){
		return pathWeights;
	}
	
	public void setShortestPath(List<RouterNode> shortList, List<Integer> weightsList) {
		this.shortestPath = shortList;
		this.pathWeights = weightsList;
	}
}
