/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.network.NetworkComponent;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.typeEntity.BlockTypeComponent;

import java.util.Optional;

public class InitialiseBlockTypeEntities extends SingleStepLoadProcess {

    private final Context context;

    public InitialiseBlockTypeEntities(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initialising Block Type Entities";
    }

    @Override
    public boolean step() {
        BlockManagerImpl blockManager = (BlockManagerImpl) context.get(BlockManager.class);
        EntityManager entityManager = context.get(EntityManager.class);
        Prefab blockTypePrefab = entityManager.getPrefabManager().getPrefab("engine:blockType");

        // connect existing entities
        for (EntityRef entity : entityManager.getEntitiesWith(BlockTypeComponent.class)) {
            BlockTypeComponent blockTypeComp = entity.getComponent(BlockTypeComponent.class);
            if (blockTypeComp.block == null) {
                entity.destroy();
            } else {
                blockTypeComp.block.setEntity(entity);
            }
        }

        // generate for existing blocks
        for (BlockFamily blockFamily : blockManager.listRegisteredBlockFamilies()) {
            for (Block block : blockFamily.getBlocks()) {
                if (!block.getEntity().exists()) {
                    EntityBuilder builder = entityManager.newBuilder(blockTypePrefab);
                    builder.getComponent(BlockTypeComponent.class).block = block;
                    // TODO: Copy across settings as necessary
                    Optional<Prefab> prefab = block.getPrefab();
                    if (prefab.isPresent()) {
                        for (Component comp : prefab.get().iterateComponents()) {
                            if (!(comp instanceof NetworkComponent)) {
                                builder.addComponent(entityManager.getComponentLibrary().copy(comp));
                            }
                        }
                    }
                    block.setEntity(builder.build());
                }
            }
        }
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
