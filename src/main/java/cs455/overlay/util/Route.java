package cs455.overlay.util;

import java.util.ArrayList;

public class Route {
	
	private int weight;
	public ArrayList<RouterNode> path;
	public ArrayList<Integer> weights;
	
	public Route(RouterNode node, int weight) {
		this.path = new ArrayList<RouterNode>();
		this.weights = new ArrayList<Integer>();
		this.weight = weight;
		path.add(node);
	}
	
	public int getTotalWeight() {
		return weight;
	}
	
	public void addNodeToRoute(RouterNode node, int weight) {
		path.add(node);
		weights.add(weight);
		this.weight+=weight;
	}
	
	public String printPath() {
		String result = "";
		for(int i = path.size()-1; i > 0;i--) {
			result+=path.get(i)+"--"+weights.get(i)+"--";
		}
		result+=path.get(0);
		return result;
	}
	
}
