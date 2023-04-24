package weiner.noah.groceryguide;

import android.util.Pair;

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

    //the data structure for storing the (weighted) graph: using adjacency lists
    //each element in list has two values (Pair of Integer objs)
    private ArrayList<ArrayList<Pair<Integer, Integer>>> adjacencyLists;

    public Graph() {
        adjacencyLists = new ArrayList<ArrayList<Pair<Integer, Integer>>>();

        //init the adjacency list of each node
        for (int i = 0; i < Constants.numCells; i++) {
            adjacencyLists.add(new ArrayList<Pair<Integer, Integer>>());
        }

        data = new ArrayList<Node>();
        numNodes = 0;
    }

    //add an edge between node with index (id) u and node with id v
    public void addEdge(int u, int v, int dist) { //dist is the edge weight
        adjacencyLists.get(u).add(new Pair<Integer, Integer>(v, dist));
        adjacencyLists.get(v).add(new Pair<Integer, Integer>(u, dist));
    }

    //return node in this graph that has the given id
    public Node findNodeById(int id) {
        for (Node n : data) {
            if (n.getId() == id) {
                return n;
            }
        }

        return null;
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

    public ArrayList<ArrayList<Pair<Integer, Integer>>> getAdjacencyLists() {
        return adjacencyLists;
    }
}
