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
package org.terasology.logic.drowning;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.events.OnEnterLiquidEvent;
import org.terasology.logic.characters.events.OnLeaveLiquidEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.location.LocationComponent;
import org.terasology.rendering.RenderHelper;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class DrowningSystem implements UpdateSubscriberSystem {

    @In
    private BlockEntityRegistry blockEntityProvider;

    @In
    private Time time;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(DrowningComponent.class, DrownsComponent.class, LocationComponent.class)) {
            DrowningComponent drowning = entity.getComponent(DrowningComponent.class);

            LocationComponent loc = entity.getComponent(LocationComponent.class);

            // Check if the player's head/eyes are actually below the water surface
            CharacterComponent charComp = entity.getComponent(CharacterComponent.class);
            if (charComp != null) {
                Vector3f worldPosition = new Vector3f(loc.getWorldPosition());
                worldPosition.y += charComp.eyeOffset;
                worldPosition.y -= RenderHelper.evaluateOceanHeightAtPosition(worldPosition, worldProvider.getTime().getDays());

                if (worldProvider.isBlockRelevant(new Vector3f(worldPosition))) {
                    Block block = worldProvider.getBlock(new Vector3f(worldPosition));

                    if (!block.isLiquid()) {
                        resetDrowning(drowning, entity.getComponent(DrownsComponent.class));
                        continue;
                    }
                }
            }

            if (drowning.nextDrownDamageTime < time.getGameTimeInMs()) {
                DrownsComponent drowns = entity.getComponent(DrownsComponent.class);

                drowning.nextDrownDamageTime = time.getGameTimeInMs() + (long) (drowns.timeBetweenDrownDamage * 1000);
                entity.saveComponent(drowning);

                EntityRef liquidBlock = blockEntityProvider.getBlockEntityAt(loc.getWorldPosition());
                entity.send(new DoDamageEvent(drowns.drownDamage, EngineDamageTypes.DROWNING.get(), liquidBlock));
            }
        }
    }

    @ReceiveEvent
    public void onEnterLiquid(OnEnterLiquidEvent event, EntityRef entity, DrownsComponent drowns) {
        DrowningComponent drowning = new DrowningComponent();
        resetDrowning(drowning, drowns);
        entity.addComponent(drowning);
    }

    @ReceiveEvent(components = DrowningComponent.class)
    public void onLeaveLiquid(OnLeaveLiquidEvent event, EntityRef entity) {
        entity.removeComponent(DrowningComponent.class);
    }

    private void resetDrowning(DrowningComponent drowningComponent, DrownsComponent drownsComponent) {
        drowningComponent.startDrowningTime = time.getGameTimeInMs() + (long) (1000 * drownsComponent.timeBeforeDrownStart);
        drowningComponent.nextDrownDamageTime = drowningComponent.startDrowningTime;
    }
}
