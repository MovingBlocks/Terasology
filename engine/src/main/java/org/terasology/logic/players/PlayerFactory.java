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
package org.terasology.logic.players;

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.SelectedItemChangedEvent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlayerFactory {

    private EntityManager entityManager;
    private SlotBasedInventoryManager inventoryManager;

    public PlayerFactory(EntityManager entityManager, SlotBasedInventoryManager inventoryManager) {
        this.entityManager = entityManager;
        this.inventoryManager = inventoryManager;
    }

    public EntityRef newInstance(Vector3f spawnPosition, EntityRef controller) {
        EntityBuilder builder = entityManager.newBuilder("engine:player");
        builder.getComponent(LocationComponent.class).setWorldPosition(spawnPosition);
        builder.setOwner(controller);
        EntityRef transferSlot = entityManager.create("engine:transferSlot");

        CharacterComponent playerComponent = builder.getComponent(CharacterComponent.class);
        playerComponent.spawnPosition.set(spawnPosition);
        playerComponent.movingItem = transferSlot;
        playerComponent.controller = controller;

        EntityRef player = builder.build();

        player.send(new SelectedItemChangedEvent(EntityRef.NULL, inventoryManager.getItemInSlot(player, 0)));

        return player;
    }

}
