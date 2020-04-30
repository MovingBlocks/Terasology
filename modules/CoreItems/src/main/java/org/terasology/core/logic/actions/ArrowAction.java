/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.core.logic.actions;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.event.DoDamageEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ArrowAction extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;

    @In
    private Physics physicsRenderer;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    @In
    private Time time;

    private CollisionGroup filter = StandardCollisionGroup.ALL;
    private float lastTime;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity, ArrowActionComponent arrowActionComponent) {

        if (time.getGameTime() > lastTime + 1.0f / arrowActionComponent.arrowsPerSecond) {
            Vector3f target = event.getHitNormal();
            Vector3i blockPos = new Vector3i(target);

            Vector3f position = new Vector3f(event.getOrigin());
            Vector3f dir = new Vector3f(event.getDirection());

            HitResult result;
            result = physicsRenderer.rayTrace(position, dir, arrowActionComponent.maxDistance, filter);

            Block currentBlock = worldProvider.getBlock(blockPos);

            if (currentBlock.isDestructible()) {
                EntityBuilder builder = entityManager.newBuilder("CoreAssets:defaultBlockParticles");
                builder.getComponent(LocationComponent.class).setWorldPosition(target);
                builder.build();
            }
            EntityRef blockEntity = result.getEntity();
            blockEntity.send(new DoDamageEvent(arrowActionComponent.damageAmount, arrowActionComponent.damageType));
            lastTime = time.getGameTime();
        }
    }
}
