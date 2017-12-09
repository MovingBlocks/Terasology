/*
 * Copyright 2015 MovingBlocks
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

import com.google.common.collect.Lists;
import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.util.List;
import java.util.Optional;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TunnelAction extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;

    @In
    private Physics physicsRenderer;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    private Random random = new FastRandom();
    private List<Optional<StaticSound>> explosionSounds = Lists.newArrayList();

    @Override
    public void initialise() {
        explosionSounds.add(Assets.getSound("core:explode1"));
        explosionSounds.add(Assets.getSound("core:explode2"));
        explosionSounds.add(Assets.getSound("core:explode3"));
        explosionSounds.add(Assets.getSound("core:explode4"));
        explosionSounds.add(Assets.getSound("core:explode5"));
    }

    @Override
    public void shutdown() {
    }

    private StaticSound getRandomExplosionSound() {
        return explosionSounds.get(random.nextInt(0, explosionSounds.size() - 1)).get();
    }

    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity, TunnelActionComponent tunnelActionComponent) {

        Vector3f dir = new Vector3f(event.getDirection());
        dir.scale(4.0f);
        Vector3f origin = new Vector3f(event.getOrigin());
        origin.add(dir);
        Vector3i blockPos = new Vector3i();

        int particleEffects = 0;
        int blockCounter = tunnelActionComponent.maxDestroyedBlocks;
        for (int s = 0; s <= tunnelActionComponent.maxTunnelDepth; s++) {
            origin.add(dir);
            if (!worldProvider.isBlockRelevant(origin)) {
                break;
            }

            for (int i = 0; i < tunnelActionComponent.maxRaysCast; i++) {
                Vector3f direction = random.nextVector3f();
                Vector3f impulse = new Vector3f(direction);
                impulse.scale(tunnelActionComponent.explosiveForce);

                for (int j = 0; j < 3; j++) {
                    Vector3f target = new Vector3f(origin);

                    target.x += direction.x * j;
                    target.y += direction.y * j;
                    target.z += direction.z * j;

                    blockPos.set((int) target.x, (int) target.y, (int) target.z);

                    Block currentBlock = worldProvider.getBlock(blockPos);

                    if (currentBlock.isDestructible()) {
                        if (particleEffects < tunnelActionComponent.maxParticalEffects) {
                            EntityBuilder builder = entityManager.newBuilder("Core:smokeExplosion");
                            builder.getComponent(LocationComponent.class).setWorldPosition(target);
                            builder.build();
                            particleEffects++;
                        }
                        if (random.nextFloat() < tunnelActionComponent.thoroughness) {
                            EntityRef blockEntity = blockEntityRegistry.getEntityAt(blockPos);
                            blockEntity.send(new PlaySoundEvent(getRandomExplosionSound(), 1.0f));
                            blockEntity.send(new DoDamageEvent(tunnelActionComponent.damageAmount, tunnelActionComponent.damageType));
                        }

                        blockCounter--;
                    }

                    if (blockCounter <= 0) {
                        return;
                    }
                }
            }
        }
        // No blocks were destroyed, so cancel the event
        if (blockCounter == tunnelActionComponent.maxDestroyedBlocks) {
            event.consume();
        }
    }
}
