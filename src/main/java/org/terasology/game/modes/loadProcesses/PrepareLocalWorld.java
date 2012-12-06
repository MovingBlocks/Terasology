/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.game.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.PlayerFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.logic.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;

import javax.vecmath.Vector3f;
import java.util.Iterator;

/**
 * @author Immortius
 */
public class PrepareLocalWorld implements LoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(PrepareLocalWorld.class);

    private EntityManager entityManager;
    private WorldRenderer worldRenderer;
    private ChunkProvider chunkProvider;
    private boolean spawningPlayer;
    private Vector3i targetPos;
    private EntityRef spawnZoneEntity;

    @Override
    public String getMessage() {
        return "Preparing immediate area...";
    }

    @Override
    public boolean step() {
        while (chunkProvider.getChunk(targetPos) == null || chunkProvider.getChunk(targetPos).getChunkState() != Chunk.State.COMPLETE) {
            return false;
        }

        if (spawningPlayer) {
            spawnPlayer();
        }
        return true;
    }

    @Override
    public int begin() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
        chunkProvider = worldRenderer.getChunkProvider();

        Iterator<EntityRef> iterator = entityManager.iteratorEntities(LocalPlayerComponent.class).iterator();
        spawningPlayer = !iterator.hasNext();
        if (spawningPlayer) {
            spawnZoneEntity = entityManager.create();
            spawnZoneEntity.addComponent(new LocationComponent(new Vector3f(Chunk.SIZE_X / 2, Chunk.SIZE_Y / 2, Chunk.SIZE_Z / 2)));
            worldRenderer.getChunkProvider().addRegionEntity(spawnZoneEntity, 1);
            targetPos = Vector3i.zero();
        } else {
            CoreRegistry.get(LocalPlayer.class).setEntity(iterator.next());
            worldRenderer.setPlayer(CoreRegistry.get(LocalPlayer.class));
            targetPos = TeraMath.calcChunkPos(new Vector3i(worldRenderer.getPlayer().getPosition(), 0.5f));
        }
        return UNKNOWN_STEPS;
    }

    private void spawnPlayer() {
        Vector3i spawnPoint = new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y, Chunk.SIZE_Z / 2);
        while (worldRenderer.getWorldProvider().getBlock(spawnPoint) == BlockManager.getInstance().getAir() && spawnPoint.y > 0) {
            spawnPoint.y--;
        }

        PlayerFactory playerFactory = new PlayerFactory(entityManager);
        CoreRegistry.get(LocalPlayer.class).setEntity(playerFactory.newInstance(new Vector3f(spawnPoint.x, spawnPoint.y + 1.5f, spawnPoint.z)));
        worldRenderer.setPlayer(CoreRegistry.get(LocalPlayer.class));

        worldRenderer.getChunkProvider().removeRegionEntity(spawnZoneEntity);
        spawnZoneEntity.destroy();
    }
}
