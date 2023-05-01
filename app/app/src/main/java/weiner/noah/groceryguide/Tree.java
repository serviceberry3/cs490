package weiner.noah.groceryguide;

import java.util.ArrayList;
import java.util.Objects;

public class Tree {
    private TreeNode root;
    private final ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();

    public Tree(TreeNode root) {
        this.root = root;
        nodes.add(root);
    }

    public TreeNode getRoot() {
        return this.root;
    }

    public void setRoot(TreeNode newRoot) {
        this.root = newRoot;
    }

    public void addNode(TreeNode node) {
        nodes.add(node);
    }

    public TreeNode getNodeById(Integer id) {
        for (TreeNode n : nodes) {
            if (Objects.equals(n.getId(), id)) {
                return n;
            }
        }

        //can't find
        return null;
    }
}
