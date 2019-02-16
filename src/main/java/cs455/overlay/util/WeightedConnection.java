package cs455.overlay.util;

import java.util.Random;

public class WeightedConnection {

	private OverlayNode nodeA;
	private OverlayNode nodeB;
	private int weight;
	
	public WeightedConnection(OverlayNode nodeA, OverlayNode nodeB) {
		this.nodeA = nodeA;
		this.nodeB = nodeB;
		Random rand = new Random();
		this.weight = rand.nextInt(10)+1;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public OverlayNode getFirstNode() {
		return nodeA;
	}
	
	public OverlayNode getSecondNode() {
		return nodeB;
	}
	
	@Override
	public String toString() {
		return getFirstNode()+","+getSecondNode()+","+getWeight();
	}
	
	public boolean equals(Object o) {
		if(o instanceof WeightedConnection) {
			return equals((WeightedConnection) o);
		}
		return false;
	}
	
	public boolean equals(WeightedConnection wc) {
		if(wc.getFirstNode().toString().equalsIgnoreCase(getFirstNode().toString())) {
			if(wc.getSecondNode().toString().equalsIgnoreCase(getSecondNode().toString())) return true;
		}
		return false;
	}
	
	public static boolean equals(WeightedConnection wc1, WeightedConnection wc2) {
		return wc1.equals(wc2);
	}
	
}
