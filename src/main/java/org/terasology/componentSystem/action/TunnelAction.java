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

import org.terasology.components.actions.TunnelActionComponent;
import org.terasology.entityFactory.DroppedBlockFactory;
import org.terasology.entitySystem.*;
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
@RegisterComponentSystem
public class TunnelAction implements EventHandlerSystem {

    private static int MAX_DESTROYED_BLOCKS = 100;

    private WorldProvider worldProvider;
    private FastRandom random = new FastRandom();
    private BulletPhysics physicsRenderer;
    private BlockEntityRegistry blockEntityRegistry;
    private DroppedBlockFactory droppedBlockFactory;

    @Override
    public void initialise() {
        worldProvider = CoreRegistry.get(WorldProvider.class);
        physicsRenderer = CoreRegistry.get(BulletPhysics.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        droppedBlockFactory = new DroppedBlockFactory(CoreRegistry.get(EntityManager.class));
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = TunnelActionComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void onActivate(ActivateEvent event, EntityRef entity) {

        Vector3f dir = new Vector3f(event.getDirection());
        Vector3f origin = new Vector3f(event.getOrigin());
        Vector3i blockPos = new Vector3i();

        int blockCounter = MAX_DESTROYED_BLOCKS;
        for (int s = 4; s <= 10000; s += 30) {
            origin.add(dir);

            for (int i = 0; i < 64; i++) {
                Vector3f direction = random.randomVector3f();
                direction.normalize();
                Vector3f impulse = new Vector3f(direction);
                impulse.scale(200);

                for (int j = 0; j < 3; j++) {
                    Vector3f target = new Vector3f(origin);

                    target.x += direction.x * j;
                    target.y += direction.y * j;
                    target.z += direction.z * j;

                    blockPos.set((int) target.x, (int) target.y, (int) target.z);

                    Block currentBlock = worldProvider.getBlock(blockPos);

                    if (currentBlock.getId() == 0x0)
                        continue;

                    if (currentBlock.isDestructible()) {
                        worldProvider.setBlock(blockPos, BlockManager.getInstance().getAir(), currentBlock);

                        EntityRef blockEntity = blockEntityRegistry.getEntityAt(blockPos);
                        blockEntity.destroy();

                        if (random.randomInt(6) == 0) {
                            EntityRef block = droppedBlockFactory.newInstance(target, currentBlock.getBlockFamily(), 5);
                            block.send(new ImpulseEvent(impulse));
                        }

                        blockCounter--;
                    }

                    if (blockCounter <= 0)
                        return;
                }
            }
        }
        //If no blocks were destroyed, cancel the event
        if(blockCounter == MAX_DESTROYED_BLOCKS)
            event.cancel();
    }
}
