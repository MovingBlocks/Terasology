/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.world.biomes;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.physics.events.MovedEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

/**
 * This system is responsible for sending {@link BiomeChangeEvent} whenever applicable.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class PlayerBiomeChangeTrackerSystem extends BaseComponentSystem {
    @In
    private WorldProvider world;

    @ReceiveEvent(components = {PlayerCharacterComponent.class})
    public void checkForBiomeChange(MovedEvent event, EntityRef entity) {
        final Vector3i newPos = new Vector3i(event.getPosition());
        final Vector3i oldPos = new Vector3i(new Vector3f(event.getPosition()).sub(event.getDelta()));
        if (newPos.equals(oldPos)) {
            return;
        }
        final Biome oldBiome = world.getBiome(oldPos);
        final Biome newBiome = world.getBiome(newPos);
        if (!oldBiome.equals(newBiome)) {
            entity.send(new BiomeChangeEvent(oldBiome, newBiome));
        }
    }
}
