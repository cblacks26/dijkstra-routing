package cs455.overlay.util;

import java.util.Random;

public class WeightedConnection {

	private String nodeA;
	private String nodeB;
	private int weight;
	
	public WeightedConnection(String nodeA, String nodeB) {
		this.nodeA = nodeA;
		this.nodeB = nodeB;
		Random rand = new Random();
		this.weight = rand.nextInt(10)+1;
	}
	
	public WeightedConnection(String nodeA, String nodeB, int weight) {
		this.nodeA = nodeA;
		this.nodeB = nodeB;
		this.weight = weight;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public String getFirstNode() {
		return nodeA;
	}
	
	public String getSecondNode() {
		return nodeB;
	}
	
	@Override
	public String toString() {
		return getFirstNode()+" "+getSecondNode()+" "+getWeight();
	}
	
	public boolean equals(Object o) {
		if(o instanceof WeightedConnection) {
			return equals((WeightedConnection) o);
		}
		return false;
	}
	
	public boolean equals(WeightedConnection wc) {
		if(wc.getFirstNode().equalsIgnoreCase(getFirstNode())) {
			if(wc.getSecondNode().equalsIgnoreCase(getSecondNode())) return true;
		}
		return false;
	}
	
	public static boolean equals(WeightedConnection wc1, WeightedConnection wc2) {
		return wc1.equals(wc2);
	}
	
}
