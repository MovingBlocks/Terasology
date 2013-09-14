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

package org.terasology.logic.characters;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.AttackRequest;
import org.terasology.logic.characters.events.DeathEvent;
import org.terasology.logic.characters.events.DropItemRequest;
import org.terasology.logic.characters.events.FrobRequest;
import org.terasology.logic.characters.events.UseItemRequest;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.health.NoHealthEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.logic.location.LocationComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterSystem
public class CharacterSystem implements ComponentSystem {

    @In
    private Physics physics;

    @In
    private WorldProvider worldProvider;

    @In
    private NetworkSystem networkSystem;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    private PickupBuilder pickupBuilder;

    private CollisionGroup[] filter = {StandardCollisionGroup.DEFAULT, StandardCollisionGroup.WORLD};

    @Override
    public void initialise() {
        pickupBuilder = new PickupBuilder();
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterComponent.class})
    public void onDeath(NoHealthEvent event, EntityRef entity) {
        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        character.controller.send(new DeathEvent());
        // TODO: Don't just destroy, ragdoll or create particle effect or something (possible allow another system to handle)
        //entity.removeComponent(CharacterComponent.class);
        //entity.removeComponent(CharacterMovementComponent.class);
        entity.destroy();
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
            if (!character.equals(event.getItem().getOwner())) {
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
            Prefab damageType = EngineDamageTypes.PHYSICAL.get();
            // Calculate damage from item
            ItemComponent item = event.getItem().getComponent(ItemComponent.class);
            if (item != null) {
                damage = item.baseDamage;
                if (item.damageType != null) {
                    damageType = item.damageType;
                }
            }

            result.getEntity().send(new DoDamageEvent(damage, damageType, character));
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

    @ReceiveEvent(components = {CharacterComponent.class, LocationComponent.class}, netFilter = RegisterMode.AUTHORITY)
    public void onDropItemRequest(DropItemRequest event, EntityRef character) {
        //make sure we own the item and it exists
        if (!event.getItem().exists() || !networkSystem.getOwnerEntity(event.getItem()).equals(networkSystem.getOwnerEntity(character))) {
            return;
        }

        EntityRef pickup = pickupBuilder.createPickupFor(event.getItem(), event.getNewPosition(), 200);
        pickup.send(new ImpulseEvent(event.getImpulse()));
    }

}
