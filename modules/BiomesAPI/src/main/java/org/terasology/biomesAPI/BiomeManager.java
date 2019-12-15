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
package org.terasology.biomesAPI;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.physics.events.MovedEvent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.chunks.blockdata.ExtraDataSystem;
import org.terasology.world.chunks.blockdata.RegisterExtraData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Share(BiomeRegistry.class)
@RegisterSystem
@ExtraDataSystem
public class BiomeManager extends BaseComponentSystem implements BiomeRegistry {
    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    private final Map<Short, Biome> biomeMap = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(BiomeManager.class);

    @Override
    public Optional<Biome> getBiome(Vector3i pos) {
        return getBiome(pos.x, pos.y, pos.z);
    }

    @Override
    public Optional<Biome> getBiome(int x, int y, int z) {
        final short biomeHash = (short) worldProvider.getExtraData("BiomesAPI.biomeHash", x, y, z);
        if (biomeHash == 0) {
            return Optional.empty();
        }
        Preconditions.checkArgument(biomeMap.containsKey(biomeHash), "Trying to use non-registered biome!");
        return Optional.of(biomeMap.get(biomeHash));
    }

    @Override
    public void setBiome(Biome biome, int x, int y, int z) {
        Preconditions.checkArgument(biomeMap.containsKey(biome.biomeHash()), "Trying to use non-registered biome!");
        worldProvider.setExtraData("BiomesAPI.biomeHash", x, y, z, biome.biomeHash());
    }

    @Override
    public void setBiome(Biome biome, CoreChunk chunk, int relX, int relY, int relZ) {
        setBiome(biome, chunk.chunkToWorldPosition(relX, relY, relZ));
    }

    @Override
    public void setBiome(Biome biome, Vector3i pos) {
        setBiome(biome, pos.x, pos.y, pos.z);
    }

    @Override
    public void setBiome(Biome biome, CoreChunk chunk, Vector3i pos) {
        setBiome(biome, chunk.chunkToWorldPosition(pos));
    }

    @Override
    public <T extends Biome> List<T> getRegisteredBiomes(Class<T> biomeClass) {
        return biomeMap.values().stream().filter(biomeClass::isInstance).map(biomeClass::cast).collect(Collectors.toList());
    }

    /**
     * Blocks have id, no matter what kind of blocks they are.
     */
    @RegisterExtraData(name = "BiomesAPI.biomeHash", bitSize = 16)
    public static boolean hasBiome(Block block) {
        return true;
    }

    @Override
    public void registerBiome(Biome biome) {
        Preconditions.checkArgument(!biomeMap.containsKey(biome.biomeHash()), "Registering biome with same hash as one of previously registered biomes!");
        biomeMap.put(biome.biomeHash(), biome);
        LOGGER.info("Registered biome " + biome.getId() + " with id " + biome.biomeHash());
    }

    /**
     * Responsible for sending {@link OnBiomeChangedEvent} to the player entity.
     */
    @ReceiveEvent(components = PlayerCharacterComponent.class)
    public void checkBiomeChangeEvent(MovedEvent event, EntityRef entity) {
        final Vector3i newPosition = new Vector3i(event.getPosition());
        final Vector3i oldPosition = new Vector3i(new Vector3f(event.getPosition()).sub(event.getDelta()));
        if (!newPosition.equals(oldPosition)) {
            final Optional<Biome> newBiomeOptional = getBiome(newPosition);
            final Optional<Biome> oldBiomeOptional = getBiome(oldPosition);
            if (oldBiomeOptional.isPresent() != newBiomeOptional.isPresent()) {
                throw new RuntimeException("Either all blocks in world must have biome, or none.");
            }
            if (!oldBiomeOptional.isPresent()) {
                return;
            }
            Biome newBiome = newBiomeOptional.get();
            Biome oldBiome = oldBiomeOptional.get();
            if (oldBiome != newBiome) {
                entity.send(new OnBiomeChangedEvent(oldBiome, newBiome));
            }
        }
    }
}
