package weiner.noah.groceryguide;

import java.sql.Array;
import java.util.ArrayList;

public class TreeNode {
    private Integer id;
    private boolean visited;

    //this node's children
    private final ArrayList<TreeNode> children = new ArrayList<TreeNode>();

    public TreeNode(Integer id) {
        this.id = id;
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }

    public Integer getId() {
        return id;
    }

    public ArrayList<TreeNode> getChildren() {
        return children;
    }
}
