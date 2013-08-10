/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.logic.tools;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.BeforeDamagedEvent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.family.BlockFamily;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ToolDamageSystem implements ComponentSystem {
    @In
    private WorldProvider worldProvider;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void beforeDamage(BeforeDamagedEvent event, EntityRef entity, BlockComponent blockComp) {
        if (event.getDamageType() != null) {
            ToolDamageComponent toolDamage = event.getDamageType().getComponent(ToolDamageComponent.class);
            if (toolDamage != null) {
                BlockFamily block = worldProvider.getBlock(blockComp.getPosition()).getBlockFamily();
                for (String category : block.getCategories()) {
                    if (toolDamage.materialDamageMultiplier.containsKey(category)) {
                        event.multiply(toolDamage.materialDamageMultiplier.get(category));
                    }
                }
            }
        }
    }
}
