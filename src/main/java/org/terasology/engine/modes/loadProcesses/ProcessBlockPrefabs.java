package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.CoreRegistry;
import org.terasology.engine.modes.LoadProcess;
import org.terasology.entitySystem.EntityManager;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.block.management.BlockManagerImpl;
import org.terasology.world.block.management.BlockPrefabManager;

/**
 * @author Immortius
 */
public class ProcessBlockPrefabs implements LoadProcess {

    @Override
    public String getMessage() {
        return "Initialising Block Type Entities";
    }

    @Override
    public boolean step() {
        BlockManagerImpl blockManager = (BlockManagerImpl) CoreRegistry.get(BlockManager.class);
        blockManager.subscribe(new BlockPrefabManager(CoreRegistry.get(EntityManager.class), blockManager));
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }
}