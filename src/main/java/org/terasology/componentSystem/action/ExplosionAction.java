/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.componentSystem.action;

import javax.vecmath.Vector3f;

import org.terasology.components.actions.ExplosionActionComponent;
import org.terasology.entitySystem.In;
import org.terasology.world.block.BlockComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.DroppedBlockFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.ImpulseEvent;
import org.terasology.utilities.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem(authorativeOnly = true)
public class ExplosionAction implements EventHandlerSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    private FastRandom random = new FastRandom();
    private DroppedBlockFactory droppedBlockFactory;

    @Override
    public void initialise() {
        droppedBlockFactory = new DroppedBlockFactory(CoreRegistry.get(EntityManager.class));
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {ExplosionActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        ExplosionActionComponent explosionComp = entity.getComponent(ExplosionActionComponent.class);
        Vector3f origin = null;
        switch (explosionComp.relativeTo) {
            case Self:
                LocationComponent loc = entity.getComponent(LocationComponent.class);
                if (loc != null) {
                    origin = loc.getWorldPosition();
                }
                break;
            case Instigator:
                origin = event.getInstigatorLocation();
                break;
            default:
                origin = event.getTargetLocation();
                break;
        }

        if (origin == null) {
            return;
        }

        Vector3i blockPos = new Vector3i();
        for (int i = 0; i < 256; i++) {
            // TODO: Add a randomVector3f method to FastRandom?
            Vector3f direction = new Vector3f(random.randomFloat(), random.randomFloat(), random.randomFloat());
            direction.normalize();
            Vector3f impulse = new Vector3f(direction);
            impulse.scale(150);

            for (int j = 0; j < 4; j++) {
                Vector3f target = new Vector3f(origin);

                target.x += direction.x * j;
                target.y += direction.y * j;
                target.z += direction.z * j;
                blockPos.set((int) target.x, (int) target.y, (int) target.z);
                Block currentBlock = worldProvider.getBlock(blockPos);

                if (currentBlock.getId() == 0)
                    continue;

                /* PHYSICS */
                if (currentBlock.isDestructible()) {
                    // TODO: this should be handled centrally somewhere. Actions shouldn't be determining world behaviour
                    // like what happens when a block is destroyed.
                    worldProvider.setBlock(blockPos, BlockManager.getInstance().getAir(), currentBlock);

                    EntityRef blockEntity = blockEntityRegistry.getEntityAt(blockPos);
                    blockEntity.destroy();
                    if (random.randomInt(4) == 0) {
                        EntityRef block = droppedBlockFactory.newInstance(target, currentBlock.getPickupBlockFamily(), 5);
                        block.send(new ImpulseEvent(impulse));
                    }
                }
            }
        }
    }
}
