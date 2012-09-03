package org.terasology.world.generator.building;

import org.terasology.logic.grammar.Shape;
import org.terasology.model.structures.BlockCollection;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tobias
 * Date: 28.08.12
 * Time: 18:33
 * To change this template use File | Settings | File Templates.
 */
public interface Tree {
    public BlockCollection derive();

    public void setParent(Tree parent);

    public Shape getShape();

    public List<TreeNode> findActiveNodes();
    //public ShapeSymbol getShapeSymbol();
}
