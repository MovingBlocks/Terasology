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
package org.terasology.logic.actions;

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.utilities.procedural.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ExplosionAction implements ComponentSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    private Random random = new FastRandom();

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity, ExplosionActionComponent explosionComp) {
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

        EntityBuilder builder = entityManager.newBuilder("engine:smokeExplosion");
        builder.getComponent(LocationComponent.class).setWorldPosition(origin);
        builder.build();

        Vector3i blockPos = new Vector3i();
        for (int i = 0; i < 64; i++) {
            Vector3f direction = random.nextVector3f(1.0f);
            Vector3f impulse = new Vector3f(direction);
            impulse.scale(150);

            for (int j = 0; j < 4; j++) {
                Vector3f target = new Vector3f(origin);

                target.x += direction.x * j;
                target.y += direction.y * j;
                target.z += direction.z * j;
                blockPos.set((int) target.x, (int) target.y, (int) target.z);
                Block currentBlock = worldProvider.getBlock(blockPos);

                /* PHYSICS */
                if (currentBlock.isDestructible()) {
                    blockEntityRegistry.getEntityAt(blockPos).send(new DoDamageEvent(1000, EngineDamageTypes.EXPLOSIVE.get(), EntityRef.NULL));
                }
            }
        }
    }
}
