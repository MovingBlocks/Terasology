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

import org.terasology.logic.inventory.ItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.events.DamageEvent;
import org.terasology.logic.characters.events.AttackRequest;
import org.terasology.logic.characters.events.FrobRequest;
import org.terasology.logic.characters.events.UseItemRequest;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.entity.BlockComponent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterSystem()
public class CharacterSystem implements ComponentSystem {

    @In
    private BulletPhysics physics;

    @In
    private WorldProvider worldProvider;

    @In
    private NetworkSystem networkSystem;

    private CollisionGroup[] filter = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD};

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class})
    public void onUseItem(UseItemRequest event, EntityRef character) {

        if (!event.getItem().exists() || !networkSystem.getOwnerEntity(event.getItem()).equals(networkSystem.getOwnerEntity(character))) {
            return;
        }

        LocationComponent location = character.getComponent(LocationComponent.class);
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        Vector3f direction = characterComponent.getLookDirection();
        Vector3f originPos = location.getWorldPosition();
        originPos.y += characterComponent.eyeOffset;

        HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange, filter);

        if (result.isHit()) {
            event.getItem().send(new ActivateEvent(result.getEntity(), character, originPos, direction, result.getHitPoint(), result.getHitNormal()));
        } else {
            originPos.y += characterComponent.eyeOffset;
            event.getItem().send(new ActivateEvent(character, originPos, direction));
        }

    }

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class})
    public void onAttackRequest(AttackRequest event, EntityRef character) {
        if (event.getItem().exists()) {
            if (!character.equals(event.getItem().getComponent(NetworkComponent.class).owner)) {
                return;
            }
        }

        LocationComponent location = character.getComponent(LocationComponent.class);
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        Vector3f direction = characterComponent.getLookDirection();
        Vector3f originPos = location.getWorldPosition();
        originPos.y += characterComponent.eyeOffset;

        HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange, filter);

        if (result.isHit()) {
            int damage = 1;
            // Calculate damage from item
            ItemComponent item = event.getItem().getComponent(ItemComponent.class);
            if (item != null) {
                damage = item.baseDamage;

                BlockComponent blockComp = result.getEntity().getComponent(BlockComponent.class);
                if (blockComp != null) {
                    Block block = worldProvider.getBlock(blockComp.getPosition());
                    if (item.getPerBlockDamageBonus().containsKey(block.getBlockFamily().getURI().toString())) {
                        damage += item.getPerBlockDamageBonus().get(block.getBlockFamily().getURI().toString());
                    }
                }
            }

            result.getEntity().send(new DamageEvent(damage, character));
        }
    }

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class})
    public void onFrob(FrobRequest event, EntityRef character) {
        LocationComponent location = character.getComponent(LocationComponent.class);
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        Vector3f direction = characterComponent.getLookDirection();
        Vector3f originPos = location.getWorldPosition();
        originPos.y += characterComponent.eyeOffset;

        HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange, filter);
        if (result.isHit()) {
            result.getEntity().send(new ActivateEvent(character, character, originPos, direction, result.getHitPoint(), result.getHitNormal()));
        }
    }
}
