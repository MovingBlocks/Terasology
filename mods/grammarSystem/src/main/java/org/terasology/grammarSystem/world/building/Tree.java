package org.terasology.grammarSystem.world.building;

import org.terasology.grammarSystem.logic.grammar.shapes.Shape;
import org.terasology.model.structures.BlockCollection;

import java.util.List;

/**
 * @author Tobias 'Skaldarnar' Nett
 *         <p/>
 *         A basic composite pattern for derivation trees.
 */
public abstract class Tree {
    public abstract BlockCollection derive();

    public abstract void setParent(Tree parent);

    public abstract Shape getShape();

    public abstract List<TreeNode> findActiveNodes();
}