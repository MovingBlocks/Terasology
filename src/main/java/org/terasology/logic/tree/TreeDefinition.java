package org.terasology.logic.tree;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;

public interface TreeDefinition {
    public void updateTree(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef treeRef);
}
