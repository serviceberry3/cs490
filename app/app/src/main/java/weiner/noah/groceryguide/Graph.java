package weiner.noah.groceryguide;

import java.sql.Array;
import java.util.ArrayList;
import java.util.LinkedList;

public class Graph {
    private int numNodes;

    //the entire data structure is a list of linked lists, each list representing a node
    //the adj list for each node contains all of the nodes that share an edge with it

    //this list can be used to get a Node by its ID quickly, then get info about the Node
    private ArrayList<Node> data;

    private ArrayList<ArrayList<Integer>> adjacencyLists;

    public Graph() {
        adjacencyLists = new ArrayList<ArrayList<Integer>>();
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
}
