package weiner.noah.groceryguide;

import java.util.HashMap;

public class MinSpanningTree {
    private Integer srcNode;

    public MinSpanningTree(Integer srcNode) {
        this.srcNode = srcNode;
    }

    //Map to keep track of each node's "parent" node in the spanning tree
    private HashMap<Integer, Integer> cameFrom = new HashMap<>();

    public void addCameFromEntry(Integer to, Integer from) {
        cameFrom.put(to, from);
    }

    public Integer getSrcNode() {
        return srcNode;
    }
}
