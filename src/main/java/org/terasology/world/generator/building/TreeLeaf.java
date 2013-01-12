package org.terasology.world.generator.building;

import org.terasology.logic.grammar.shapes.Shape;
import org.terasology.logic.grammar.shapes.TerminalShape;
import org.terasology.model.structures.BlockCollection;

import java.util.List;

/**
 * @author Tobias 'Skaldarnar' Nett
 *         <p/>
 *         A tree leaf contains only terminal shapes in the derivation tree.
 */
public class TreeLeaf extends Tree {

    private TerminalShape terminal;
    private Tree parent;

    public TreeLeaf(TerminalShape terminal) {
        BlockCollection c = new BlockCollection();
        this.terminal = terminal;
    }

    @Override
    public BlockCollection derive() {
        System.out.println(terminal.getValue());
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
