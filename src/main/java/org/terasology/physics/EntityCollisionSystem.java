/*
 * Copyright 2012
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

package org.terasology.physics;

import org.terasology.components.HealthComponent;
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.DamageEvent;
import org.terasology.physics.character.CharacterMovementComponent;

/**
 * @author aherber
 */
@RegisterComponentSystem
public class EntityCollisionSystem implements EventHandlerSystem {

    @ReceiveEvent(components=ItemComponent.class)
    public void onCollision(CollideEvent event, EntityRef entity) {
    	System.out.println("Collision with Item");
        event.getOtherEntity().send(new DamageEvent(10));
    }
    
    @ReceiveEvent(components=CharacterMovementComponent.class)
    public void onBump(CollideEvent event, EntityRef entity) {
    	CharacterMovementComponent component = entity.getComponent(CharacterMovementComponent.class);
    //	CharacterMovementComponent componentOtherEntity = event.getOtherEntity().getComponent(CharacterMovementComponent.class);
    	component.getDrive().negate();
    	component.getVelocity().negate();
//    	componentOtherEntity.setDrive(component.getDrive());
    //	componentOtherEntity.getDrive().negate();
    //	componentOtherEntity.getVelocity().negate();
//    	componentOtherEntity.getVelocity().dot(component.getVelocity());
//    	componentOtherEntity.setVelocity(component.getVelocity());
    	entity.saveComponent(component);
    //	event.getOtherEntity().saveComponent(componentOtherEntity);
    }


    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }
}
