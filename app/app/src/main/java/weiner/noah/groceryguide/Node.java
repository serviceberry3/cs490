package weiner.noah.groceryguide;

import android.graphics.RectF;

//each node in the graph represents a square cell on the map
//we'll have somewhere on the order of 40000 nodes in the graph, but each node is only connected to a maximum of
//4 other nodes (those it shares a cell edge with)
public class Node {
    //each node has an identifying number
    private int id;

    private RectF cellBounds;

    public Node(int id, RectF cellBounds) {
        this.id = id;
        this.cellBounds = cellBounds;
    }

    public RectF getCellBounds() {
        return cellBounds;
    }
}
