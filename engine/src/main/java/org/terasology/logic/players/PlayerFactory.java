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
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.SelectedItemChangedEvent;
import org.terasology.network.ClientComponent;
import org.terasology.network.ColorComponent;
import org.terasology.rendering.logic.MeshComponent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlayerFactory {

    private EntityManager entityManager;

    public PlayerFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityRef newInstance(Vector3f spawnPosition, EntityRef controller) {
        EntityBuilder builder = entityManager.newBuilder("engine:player");
        builder.getComponent(LocationComponent.class).setWorldPosition(spawnPosition);
        builder.setOwner(controller);
        EntityRef transferSlot = entityManager.create("engine:transferSlot");

        ClientComponent clientComp = controller.getComponent(ClientComponent.class);
        if (clientComp != null) {
            ColorComponent colorComp = clientComp.clientInfo.getComponent(ColorComponent.class);
            
            MeshComponent meshComp = builder.getComponent(MeshComponent.class);
            meshComp.color = colorComp.color;
        }
        
        CharacterComponent playerComponent = builder.getComponent(CharacterComponent.class);
        playerComponent.spawnPosition.set(spawnPosition);
        playerComponent.movingItem = transferSlot;
        playerComponent.controller = controller;

        EntityRef player = builder.build();

        player.send(new SelectedItemChangedEvent(EntityRef.NULL, InventoryUtils.getItemAt(player, 0)));

        return player;
    }

}
