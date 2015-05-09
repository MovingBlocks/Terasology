/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.FlightModeHUDToggle.systems;

import org.terasology.HUDToggleButtons.systems.HUDToggleButtonsClientSystem;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.events.SetMovementModeEvent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.CLIENT)
public class FlightModeHUDToggleButton extends BaseComponentSystem implements HUDToggleButtonsClientSystem.HUDToggleButtonState {
    @In
    HUDToggleButtonsClientSystem toggleButtonsClientSystem;
    @In
    EntityManager entityManager;

    EntityRef localClientEntity;

    @Override
    public void initialise() {
        toggleButtonsClientSystem.registerToggleButton(this);
    }


    private EntityRef getLocalClientEntity() {
        if (localClientEntity == null) {
            for (EntityRef entityRef : entityManager.getEntitiesWith(ClientComponent.class)) {
                ClientComponent clientComponent = entityRef.getComponent(ClientComponent.class);
                if (clientComponent.local) {
                    localClientEntity = entityRef;
                    break;
                }
            }
        }

        return localClientEntity;
    }

    private EntityRef getLocalCharacterEntity() {
        EntityRef clientEntity = getLocalClientEntity();
        ClientComponent clientComponent = clientEntity.getComponent(ClientComponent.class);
        return clientComponent.character;
    }

    @Override
    public void toggle() {
        MovementMode nextMode = MovementMode.WALKING;
        if (getMovementMode() != MovementMode.FLYING) {
            nextMode = MovementMode.FLYING;
        }
        getLocalCharacterEntity().send(new SetMovementModeEvent(nextMode));
    }

    @Override
    public boolean isValid() {
        return true;
    }

    private MovementMode getMovementMode() {
        EntityRef character = getLocalCharacterEntity();
        CharacterMovementComponent movementComponent = character.getComponent(CharacterMovementComponent.class);
        return movementComponent.mode;
    }

    @Override
    public String getText() {
        switch (getMovementMode()) {
            case CLIMBING:
                return "Climbing";
            case FLYING:
                return "Flying";
            case SWIMMING:
                return "Swimming";
            case WALKING:
                return "Walking";
            case GHOSTING:
                return "Ghosting";
            default:
                return "Unknown";
        }
    }
}
