/*
 * Copyright 2013 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logic.characters;

import org.terasology.components.ItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.events.ActivateEvent;
import org.terasology.events.DamageEvent;
import org.terasology.logic.characters.events.AttackTargetRequest;
import org.terasology.logic.characters.events.UseItemInDirectionRequest;
import org.terasology.logic.characters.events.UseItemOnTargetRequest;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockRegionComponent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterComponentSystem(RegisterMode.SERVER)
public class CharacterSystem implements EventHandlerSystem {

    @In
    private BulletPhysics physics;

    @In
    private NetworkSystem network;

    @In
    private WorldProvider worldProvider;

    private CollisionGroup[] filter = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD};

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    // TODO: May need to make this more lenient to account for lag - should work better with client side prediction algorithms in place
    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class})
    public void onUseItemOnTargetRequest(UseItemOnTargetRequest event, EntityRef character) {
        if (event.getItem().exists()) {
            if (!character.equals(event.getItem().getComponent(ItemComponent.class).container)) {
                return;
            }
        }

        if (event.getTarget().exists()) {
            LocationComponent location = character.getComponent(LocationComponent.class);
            CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
            Vector3f targetPos = event.getTargetPosition();
            Vector3f originPos = location.getWorldPosition();
            originPos.y += characterComponent.eyeOffset;

            Vector3f direction = new Vector3f(targetPos);
            direction.sub(originPos);
            // Too far away
            if (direction.lengthSquared() > characterComponent.interactionRange * characterComponent.interactionRange) {
                return;
            }

            direction.normalize();

            HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange, filter);
            if (result.getEntity().equals(event.getTarget())) {
                event.getItem().send(new ActivateEvent(event.getTarget(), character, originPos, direction, event.getTargetPosition(), result.getHitNormal()));
            }
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class})
    public void onUseItemInDirectionRequest(UseItemInDirectionRequest event, EntityRef character) {
        if (event.getItem().exists()) {
            if (!character.equals(event.getItem().getComponent(ItemComponent.class).container)) {
                return;
            }
        }

        LocationComponent location = character.getComponent(LocationComponent.class);
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        Vector3f originPos = location.getWorldPosition();
        originPos.y += characterComponent.eyeOffset;

        event.getItem().send(new ActivateEvent(character, originPos, event.getDirection()));
    }

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class})
    public void onAttackTargetRequest(AttackTargetRequest event, EntityRef character) {
        if (event.getItem().exists()) {
            if (!character.equals(event.getItem().getComponent(ItemComponent.class).container)) {
                return;
            }
        }

        int damage = 1;
        // Calculate damage from item
        ItemComponent item = event.getItem().getComponent(ItemComponent.class);
        if (item != null) {
            damage = item.baseDamage;

            BlockComponent blockComp = event.getTarget().getComponent(BlockComponent.class);
            BlockRegionComponent blockRegionComponent = event.getTarget().getComponent(BlockRegionComponent.class);
            if (blockComp != null || blockRegionComponent != null) {
                Block block = worldProvider.getBlock(blockComp.getPosition());
                if (item.getPerBlockDamageBonus().containsKey(block.getBlockFamily().getURI().toString())) {
                    damage += item.getPerBlockDamageBonus().get(block.getBlockFamily().getURI().toString());
                }
            }
        }

        // Check target
        if (event.getTarget().exists()) {
            LocationComponent location = character.getComponent(LocationComponent.class);
            CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
            Vector3f targetPos = event.getTargetPosition();
            Vector3f originPos = location.getWorldPosition();
            originPos.y += characterComponent.eyeOffset;

            Vector3f direction = new Vector3f(targetPos);
            direction.sub(originPos);
            // Too far away
            if (direction.lengthSquared() > characterComponent.interactionRange * characterComponent.interactionRange) {
                return;
            }

            direction.normalize();

            HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange, filter);
            if (result.getEntity().equals(event.getTarget())) {
                event.getTarget().send(new DamageEvent(damage, character));
            }
        }
    }

}
