package weiner.noah.groceryguide;

import java.sql.Array;
import java.util.ArrayList;
import java.util.LinkedList;


/**The entire data structure is a list of lists, each list representing a node.
 * The adjacency list for each node contains all of the nodes that share an edge with it.
 * This list can be used to get a Node by its ID quickly, then get info about the Node.
 */
public class Graph {
    private int numNodes;

    //this is the list of actual Nodes
    private ArrayList<Node> data;

    private ArrayList<ArrayList<Integer>> adjacencyLists;

    public Graph() {
        adjacencyLists = new ArrayList<ArrayList<Integer>>();

        //init the adjacency list of each node
        for (int i = 0; i < Constants.numCells; i++) {
            adjacencyLists.add(new ArrayList<Integer>());
        }

        data = new ArrayList<Node>();
        numNodes = 0;
    }

    //add an edge between node with index (id) u and node with id v
    public void addEdge(int u, int v) {
        adjacencyLists.get(u).add(v);
        adjacencyLists.get(v).add(u);
    }

    public void addNode(Node n) {
        data.add(n);
    }

    public ArrayList<Node> getData() {
        return data;
    }

    //check if the graph contains a node with a certain ID (cell)
    public boolean containsNode(int id) {
        return data.get(id).getCellBounds() != null;
    }

    public ArrayList<ArrayList<Integer>> getAdjacencyLists() {
        return adjacencyLists;
    }
}
