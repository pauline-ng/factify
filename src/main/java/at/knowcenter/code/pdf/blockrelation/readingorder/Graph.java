/**
 * Copyright (C) 2010
 * "Kompetenzzentrum fuer wissensbasierte Anwendungen Forschungs- und EntwicklungsgmbH"
 * (Know-Center), Graz, Austria, office@know-center.at.
 *
 * Licensees holding valid Know-Center Commercial licenses may use this file in
 * accordance with the Know-Center Commercial License Agreement provided with
 * the Software or, alternatively, in accordance with the terms contained in
 * a written agreement between Licensees and Know-Center.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.knowcenter.code.pdf.blockrelation.readingorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * a utility class implementing a simple directed graph for reading order extraction
 * 
 * @author sklampfl
 * @see {@link ReadingOrderExtractor}
 *
 */
class Graph {
	
	public class Node {
		public final int node;
		public final int fanout;
		public final int priority;
		public Node(int node, int fanout, int priority) { this.node=node; this.fanout=fanout; this.priority=priority; }
		@Override
		public String toString() { return "("+node+","+fanout+")"; }
	}
	
	private final int numNodes;
	private final boolean[][] adjacencyMatrix;
	private final int[] priority;
	private int numEdges;
	
	public Graph(int numNodes) {
		if (numNodes<0) {
			throw new IllegalArgumentException("Graph must have a positive number of nodes: "+numNodes);
		}
		this.numNodes = numNodes;
		this.adjacencyMatrix = new boolean[numNodes][numNodes];
		this.priority = new int[numNodes];
	}
	
	public void addEdge(int node1, int node2) {
		if (node1<0 || node1>=numNodes || node2<0 || node2>=numNodes) {
			throw new IllegalArgumentException("Invalid edge: "+node1+", "+node2);
		}
		if (!adjacencyMatrix[node1][node2]) {
			adjacencyMatrix[node1][node2] = true;
			numEdges++;
		}
	}
	
	public boolean containsEdge(int node1, int node2) {
		if (node1<0 || node1>=numNodes || node2<0 || node2>=numNodes) {
			throw new IllegalArgumentException("Invalid edge: "+node1+", "+node2);
		}
		return adjacencyMatrix[node1][node2];
	}
	
	public void removeEdge(int node1, int node2) {
		if (node1<0 || node1>=numNodes || node2<0 || node2>=numNodes) {
			throw new IllegalArgumentException("Invalid edge: "+node1+", "+node2);
		}
		if (adjacencyMatrix[node1][node2]) {
			adjacencyMatrix[node1][node2] = false;
			numEdges--;
		}
	}
	
	public void removeSelfLoops() {
		for (int node=0; node<numNodes; node++) {
			if (adjacencyMatrix[node][node]) {
				adjacencyMatrix[node][node] = false;
				numEdges--;
			}
		}
	}
	
	public int getNumEdges() {
		return numEdges;
	}
	
	public int getNumNodes() {
		return numNodes;
	}
	
	public boolean isTransitive() {
		boolean result = true;
		for (int node1=0; node1<numNodes; node1++) {
			for (int node2=0; node2<numNodes; node2++) {
				if (adjacencyMatrix[node1][node2]) {
					for (int node3=0; node3<numNodes; node3++) {
						if (adjacencyMatrix[node2][node3]) {
							result = result && adjacencyMatrix[node1][node3];
						}
					}
				}
			}
		}
		return result;
	}
	
	public List<Node> getNodeList() {
		List<Node> result = new ArrayList<Node>(numNodes);
		for (int node=0; node<numNodes; node++) {
			int fanout = 0, fanin = 0;
			for (int node2=0; node2<numNodes; node2++) {
				if (adjacencyMatrix[node][node2]) {
					fanout++;
				}
				if (adjacencyMatrix[node2][node]) {
					fanin++;
				}
			}
			if (fanin>0 || fanout>0) {
				result.add(new Node(node,fanout,priority[node]));
			}
		}
		Collections.sort(result, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
			    int d = o2.fanout-o1.fanout;
			    if (d == 0) {
			        d = o1.priority - o2.priority;
			        if (d == 0) {
			            d = -(o1.node - o2.node);
			        }
			    }
				return d;
			}
		});
		return result;
	}
	
	public void removeNode(int node) {
		if (node<0 || node>=numNodes) {
			throw new IllegalArgumentException("Invalid node: "+node);
		}
		for (int node2=0; node2<numNodes; node2++) {
			if (adjacencyMatrix[node][node2]) {
				adjacencyMatrix[node][node2] = false;
				numEdges--;
			}
			if (adjacencyMatrix[node2][node]) {
				adjacencyMatrix[node2][node] = false;
				numEdges--;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Graph [numNodes=" + numNodes + ", numEdges="+numEdges+", edges=");
		for (int node1=0; node1<numNodes; node1++) {
			for (int node2=0; node2<numNodes; node2++) {
				if (adjacencyMatrix[node1][node2]) {
					buffer.append("("+node1+","+node2+"),");
				}
			}
		}
		buffer.deleteCharAt(buffer.length()-1);
		buffer.append("]");
//		buffer.append("\n").append(numNodes*(numNodes-1)/2.0).append("\n");
		buffer.append("\n");
		for (int node=0; node<numNodes; node++) {
			for (int node2=0; node2<numNodes; node2++) {
				buffer.append((adjacencyMatrix[node][node2])?1:0).append(" ");
			}
			buffer.append("\n");
		}
		return buffer.toString();
	}

    /**
     * @param i
     * @param minimumSequenceId
     */
    public void setPriority(int i, int minimumSequenceId) {
        priority[i] = minimumSequenceId;
    }
	
	

}
