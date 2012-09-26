package org.terasology.world.generator.building;

import org.terasology.logic.grammar.Shape;
import org.terasology.logic.grammar.TerminalShape;
import org.terasology.model.structures.BlockCollection;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 28.08.12
 * Time: 18:53
 * To change this template use File | Settings | File Templates.
 */
public class TreeLeaf implements Tree {

    private TerminalShape terminal;
    private Tree parent;

    public TreeLeaf(TerminalShape terminal) {
        BlockCollection c = new BlockCollection();
        this.terminal = terminal;
    }

    @Override
    public BlockCollection derive() {
        return terminal.getValue();
    }

    @Override
    public void setParent(Tree parent) {
        this.parent = parent;
    }

    @Override
    public Shape getShape() {
        return terminal;
    }

    @Override
    public List<TreeNode> findActiveNodes() {
        return null;
    }

    public TerminalShape getTerminal() {
        return terminal;
    }

    public Tree getParent() {
        return parent;
    }
}
