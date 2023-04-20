package weiner.noah.groceryguide;

import android.graphics.RectF;

/**
 * Each node in the graph represents a square grid cell on the map
 * We'll have somewhere on the order of 40000 nodes in the graph, but each node is only connected to a maximum of
 * 4 other nodes (those it shares a cell edge with)
 */
public class Node {
    //each node has an identifying number
    private int id;

    //the RectF representing the bounds of the node's cell
    private RectF cellBounds;

    private float priority;

    public Node(int id, RectF cellBounds) {
        this.id = id;
        this.cellBounds = cellBounds;
        this.priority = Float.POSITIVE_INFINITY;
    }

    public RectF getCellBounds() {
        return cellBounds;
    }

    public int getId() {
        return id;
    }

    public float getPriority() {
        return this.priority;
    }

    public void setPriority(float priority) {
        this.priority = priority;
    }
}
