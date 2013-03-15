/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.componentSystem.action;

import org.terasology.components.actions.MiniaturizerComponent;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.math.Vector3i;
import org.terasology.rendering.world.BlockGrid;
import org.terasology.world.MiniatureChunk;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;
import java.util.Set;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
// TODO: Network
@RegisterSystem(RegisterMode.AUTHORITY)
public class AddMiniatureBlockAction implements ComponentSystem {

    @In
    private WorldProvider worldProvider;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = MiniaturizerComponent.class)
    public void onActivate(ActivateEvent event, EntityRef entity) {
        if (!event.getTarget().exists()) {
            return;
        }
        MiniaturizerComponent comp = entity.getComponent(MiniaturizerComponent.class);
        BlockGrid grid = comp.blockGrid;

        Set<BlockGrid.GridPosition> gridPositions = grid.getGridPositions();

        if (gridPositions.size() < 2) {
            comp.reset();

            Vector3i targetPos = new Vector3i(event.getTargetLocation());

            comp.blockGrid.addGridPosition(targetPos, worldProvider.getBlock(targetPos).getId());
            entity.saveComponent(comp);
        } else {

            MiniatureChunk chunk = new MiniatureChunk();

            for (int x = grid.getMinBounds().x; x <= grid.getMaxBounds().x; ++x) {
                for (int y = grid.getMinBounds().y; y <= grid.getMaxBounds().y; ++y) {
                    for (int z = grid.getMinBounds().z; z <= grid.getMaxBounds().z; ++z) {
                        Vector3i localPos = new Vector3i();
                        Vector3i globalPos = new Vector3i(x, y, z);
                        localPos.sub(globalPos, grid.getMinBounds());

                        if (localPos.x >= MiniatureChunk.SIZE_X || localPos.y >= MiniatureChunk.SIZE_Y || localPos.z >= MiniatureChunk.SIZE_Z || localPos.x < 0 || localPos.y < 0 || localPos.z < 0)
                            continue;

                        chunk.setSunlight(localPos, worldProvider.getSunlight(globalPos));
                        chunk.setLight(localPos, worldProvider.getLight(globalPos));
                        chunk.setBlock(localPos, worldProvider.getBlock(globalPos));
                    }
                }
            }

            Vector3f renderLocation = new Vector3f(event.getTargetLocation());
            renderLocation.y += 0.5f + (MiniaturizerComponent.SCALE / 2f);

            comp.miniatureChunk = chunk;
            comp.renderPosition = new Vector3f(renderLocation);
            grid.clear();
            entity.saveComponent(comp);
        }

    }
}
