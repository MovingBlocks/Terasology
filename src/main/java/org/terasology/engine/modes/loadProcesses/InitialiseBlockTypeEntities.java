package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.CoreRegistry;
import org.terasology.engine.modes.LoadProcess;
import org.terasology.entitySystem.EntityManager;
import org.terasology.world.block.typeEntity.BlockTypeEntityGenerator;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.block.management.BlockManagerImpl;

/**
 * @author Immortius
 */
public class InitialiseBlockTypeEntities implements LoadProcess {

    @Override
    public String getMessage() {
        return "Initialising Block Type Entities";
    }

    @Override
    public boolean step() {
        BlockManagerImpl blockManager = (BlockManagerImpl) CoreRegistry.get(BlockManager.class);
        blockManager.subscribe(new BlockTypeEntityGenerator(CoreRegistry.get(EntityManager.class), blockManager));
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }
}
