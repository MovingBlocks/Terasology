package org.terasology.logic.tree;

import org.terasology.entitySystem.EntityRef;
import org.terasology.world.WorldProvider;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface TreeDefinition {
    public void updateTree(WorldProvider worldProvider, EntityRef treeRef);
}
