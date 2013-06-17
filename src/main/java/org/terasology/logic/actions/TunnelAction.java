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
package org.terasology.logic.actions;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.DamageEvent;
import org.terasology.logic.inventory.ItemPickupFactory;
import org.terasology.math.Vector3i;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.ImpulseEvent;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.items.BlockItemFactory;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class TunnelAction implements ComponentSystem {

    private static int MAX_DESTROYED_BLOCKS = 100;

    private WorldProvider worldProvider;
    private FastRandom random = new FastRandom();
    private BulletPhysics physicsRenderer;
    private BlockEntityRegistry blockEntityRegistry;
    private ItemPickupFactory itemPickupFactory;
    private BlockItemFactory blockItemFactory;

    @Override
    public void initialise() {
        worldProvider = CoreRegistry.get(WorldProvider.class);
        physicsRenderer = CoreRegistry.get(BulletPhysics.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
        itemPickupFactory = new ItemPickupFactory(CoreRegistry.get(EntityManager.class));
        blockItemFactory = new BlockItemFactory(CoreRegistry.get(EntityManager.class));
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = TunnelActionComponent.class)
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

                    if (currentBlock.isDestructible()) {
                        if (random.randomInt(6) == 0) {
                            EntityRef blockEntity = blockEntityRegistry.getEntityAt(blockPos);
                            blockEntity.send(new DamageEvent(1000, EntityRef.NULL));
                        }

                        blockCounter--;
                    }

                    if (blockCounter <= 0)
                        return;
                }
            }
        }
    }
}
