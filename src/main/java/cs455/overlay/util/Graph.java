package cs455.overlay.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

public class Graph {

	Set<RouterNode> nodes = new HashSet<>();
	
	public void addNode(RouterNode n) {
		nodes.add(n);
	}
	
	public void setNodes(Set<RouterNode> nodes) {
		this.nodes = nodes;
	}
	
	public Set<RouterNode> getNodes(){
		return nodes;
	}
	
	public Set<RouterNode> calculateShortestPathFromNode(RouterNode sourceNode) {
		sourceNode.setDistance(0);
		
		Set<RouterNode> settledNodes = new HashSet<>();
		Set<RouterNode> unsettledNodes = new HashSet<>();
		
		unsettledNodes.add(sourceNode);
		
		while(unsettledNodes.size()>0) {
			RouterNode currNode = getShortestDistanceNode(unsettledNodes);
			for(Entry<RouterNode,Integer> adjacent: currNode.getAdjacentNodes().entrySet()){
				RouterNode adjNode = adjacent.getKey();
				int weight = adjacent.getValue();
				if (!settledNodes.contains(adjNode)) {
	                calculateMinimumDistance(adjNode, weight, currNode);
	                unsettledNodes.add(adjNode);
	            }
	        }
	        settledNodes.add(currNode);
	        unsettledNodes.remove(currNode);
		}
		return settledNodes;
	}
	
	
	
	private static void calculateMinimumDistance(RouterNode evalNode, int weight, RouterNode sourceNode) {
	    Integer sourceDistance = sourceNode.getDistance();
	    if (sourceDistance + weight < evalNode.getDistance()) {
	        evalNode.setDistance(sourceDistance + weight);
	        LinkedList<RouterNode> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
	        LinkedList<Integer> shortestWeights = new LinkedList<>(sourceNode.getShortestPathWeights());
	        shortestPath.add(sourceNode);
	        shortestWeights.add(weight);
	        evalNode.setShortestPath(shortestPath, shortestWeights);
	    }
	}

	private static RouterNode getShortestDistanceNode(Set<RouterNode> unsettledNodes) {
		RouterNode low = null;
		int lowDist = Integer.MAX_VALUE;
		for(RouterNode node:unsettledNodes) {
			int dist = node.getDistance();
			if(dist<lowDist) {
				lowDist = dist;
				low = node;
			}
		}
		return low;
	}
}
