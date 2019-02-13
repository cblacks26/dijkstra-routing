package cs455.overlay.util;

import java.util.ArrayList;

public class Overlay {

	private ArrayList<WeightedConnection> conns;
	
	public Overlay(ArrayList<String> nodes, int numberConnections) {
		this.conns = new ArrayList<WeightedConnection>();
		setup(nodes,numberConnections);
	}
	
	public void setup(ArrayList<String> nodes, int numberConnections) {
		for(int i = 0; i < nodes.size();i++) {
			int start = i - (numberConnections/2);
			String node = nodes.get(i);
			for(int k = 0,n = start; k<numberConnections;k++,n++) {
				if(n<0){
					n = nodes.size() + n;
				}else if(n>=nodes.size()){
					n = 0;
				}
				WeightedConnection wc = new WeightedConnection(node,nodes.get(n));
				if(!conns.contains(wc))conns.add(wc);
			}
		}
	}
}
