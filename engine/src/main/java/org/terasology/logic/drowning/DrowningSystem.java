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
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;

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
            DrownsComponent drowns = entity.getComponent(DrownsComponent.class);

            // update breath time
            if (drowning.isBreathing()) {
                drowning.breathRemaining += delta * drowns.breathRechargeRate;
            } else {
                drowning.breathRemaining -= delta;
            }

            // check for out of breath and full breath conditions
            if (drowning.breathRemaining > drowns.breathCapacity) {
                // clean up the drowning component
                entity.removeComponent(DrowningComponent.class);
            } else {
                if (drowning.breathRemaining <= drowns.timeBetweenDrownDamage * -1) {
                    // damage the entity,  give breath in trade for taking damage
                    EntityRef liquidBlock = blockEntityProvider.getBlockEntityAt(loc.getWorldPosition());
                    entity.send(new DoDamageEvent(drowns.drownDamage, EngineDamageTypes.DROWNING.get(), liquidBlock));
                    drowning.breathRemaining += drowns.timeBetweenDrownDamage;
                }
                entity.saveComponent(drowning);
            }
        }
    }

    @ReceiveEvent(components = {DrownsComponent.class})
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity, DrownsComponent drowns) {
        // only trigger drowning if liquid is covering the top of the character (aka,  the head)
        if (isHeadLevel(event.getCharacterRelativePosition(), entity)) {
            DrowningComponent drowning = entity.getComponent(DrowningComponent.class);
            if (event.getNewBlock().getEntity().hasComponent(UnbreathableBlockComponent.class)) {
                if (drowning != null) {
                    drowning.setBreathing(false);
                    entity.saveComponent(drowning);
                } else {
                    drowning = new DrowningComponent();
                    drowning.breathRemaining = drowns.breathCapacity;
                    entity.addComponent(drowning);
                }
            } else {
                if (drowning != null) {
                    drowning.setBreathing(true);
                    entity.saveComponent(drowning);
                }
            }
        }
    }

    private boolean isHeadLevel(Vector3i relativePosition, EntityRef entity) {
        CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
        return (int) Math.ceil(characterMovementComponent.height) - 1 == relativePosition.y;
    }
}
