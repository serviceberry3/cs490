package weiner.noah.groceryguide;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Objects;

public class ShortestPath {
    //the two Nodes in question
    private Integer srcNode;
    private Integer dstNode;

    //length of the shortest path
    private int len;

    //the path
    private ArrayList<Integer> path = new ArrayList<Integer>();

    //Map to keep track of each node's "parent" node in the path
    private HashMap<Integer, Integer> cameFrom = new HashMap<>();

    //Map to keep track of total ACTUAL distance from start node to each explored node
    //The catch is that we need to keep track of this, since the Manhattan distance heuristic is ignoring obstacles
    //If we didn't keep track of this, the algorithm would be greedy and would waste time exploring directions that aren't promising due to obstacles.
    private HashMap<Integer, Float> cumulativeActualDist = new HashMap<>();

    public ShortestPath(Integer from, Integer to) {
        this.srcNode = from;
        this.dstNode = to;
    }

    public void addCameFromEntry(Integer to, Integer from) {
        cameFrom.put(to, from);
    }

    public ArrayList<Integer> reconstructPath() {
        //we didn't make it to dest!!
        if (!cameFrom.containsKey(dstNode)) {
            return null;
        }

        Integer currNode = dstNode;

        while (!Objects.equals(currNode, srcNode)) {
            path.add(currNode);
            currNode = cameFrom.get(currNode);
        }

        //finally, add the starting node on
        path.add(srcNode);

        return path;
    }

    public HashMap<Integer, Integer> getCameFrom() {
        return cameFrom;
    }

    public void addDistSoFarEntry(Integer to, Float dist) {
        cumulativeActualDist.put(to, dist);
    }

    //just to be clear
    public void updateDistSoFarEntry(Integer key, Float newVal) {
        cumulativeActualDist.put(key, newVal);
    }

    public HashMap<Integer, Float> getDistSoFar() {
        return cumulativeActualDist;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }
}
