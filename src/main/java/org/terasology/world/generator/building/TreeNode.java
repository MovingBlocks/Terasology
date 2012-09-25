package org.terasology.world.generator.building;

import org.terasology.logic.grammar.Shape;
import org.terasology.model.structures.BlockCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 28.08.12
 * Time: 18:35
 * To change this template use File | Settings | File Templates.
 */
public class TreeNode implements Tree {
    private Tree parent = null;
    private boolean active = true;
    private List<Tree> children = new ArrayList<Tree>();
    private Shape shape;

    public TreeNode(Shape shape) {
        this.shape = shape;
    }

    @Override
    public BlockCollection derive() {
        BlockCollection c = new BlockCollection();
        for (Tree child : children) {
            Shape s = child.getShape();
        }
        return c;
    }

    @Override
    public void setParent(Tree parent) {
        this.parent = parent;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void add(Tree child) {
        child.setParent(this);
        children.add(child);
    }

    public void addChildren(Collection<Tree> children) {
        for (Tree t : children) {
            t.setParent(this);
        }
        this.children.addAll(children);
    }

    public Tree getParent() {
        return parent;
    }

    public List<Tree> getChildren() {
        return children;
    }

    public Shape getShape() {
        return shape;
    }

    public List<TreeNode> findActiveNodes() {
        List<TreeNode> retVal = new ArrayList<TreeNode>();
        if (active) {
            retVal.add(this);
        }
        for (Tree t : children) {
            if (t instanceof TreeNode) {
                retVal.addAll(t.findActiveNodes());
            }
        }
        return retVal;
    }
}
