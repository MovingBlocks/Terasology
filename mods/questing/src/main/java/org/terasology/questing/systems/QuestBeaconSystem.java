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

package org.terasology.questing.systems;

import com.google.common.collect.Lists;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.PlayerFactory;
import org.terasology.questing.components.QuestBeaconComponent;
import org.terasology.questing.events.ReachedBeaconEvent;

import javax.vecmath.Vector3f;
import java.util.List;

@RegisterSystem
public class QuestBeaconSystem implements ComponentSystem, UpdateSubscriberSystem {

    @In
    private EntityManager manager;

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void update(float delta) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);

        List<EntityRef> beaconEntities = Lists.newArrayList();

        //Put all of the entities with the beacon component into a list
        if (beaconEntities != null) {
            for (EntityRef beaconEntity : manager.getEntitiesWith(QuestBeaconComponent.class)) {
                beaconEntities.add(beaconEntity);
            }
        }

        if (beaconEntities != null) {
            for (EntityRef beacon : beaconEntities) {
                if (localPlayer != null && beacon != null) {
                    LocationComponent location = beacon.getComponent(LocationComponent.class);
                    if (location != null) {
                        Vector3f beaconPos = location.getWorldPosition();
                        beaconPos.sub(localPlayer.getPosition());
                        double distanceToPlayer = beaconPos.lengthSquared(); //Checking the distance between the beacon and the player

                        if (distanceToPlayer < 7) {
                            localPlayer.getCharacterEntity().send(new ReachedBeaconEvent(localPlayer.getCharacterEntity(), beacon)); //Send the event
                        }
                    }
                }
            }
        }
    }
}
