package org.terasology.componentSystem.action;

import org.terasology.components.actions.MiniaturizerComponent;
import org.terasology.components.world.BlockComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.world.BlockEntityRegistry;
import org.terasology.logic.world.MiniatureChunk;
import org.terasology.logic.world.WorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.BlockPosition;
import org.terasology.rendering.physics.BulletPhysicsRenderer;
import org.terasology.rendering.world.BlockGrid;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3f;
import java.util.HashSet;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
@RegisterComponentSystem
public class AddMiniatureBlockAction implements EventHandlerSystem {

    private WorldProvider worldProvider;

    @Override
    public void initialise() {
        worldProvider = CoreRegistry.get(WorldProvider.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = MiniaturizerComponent.class)
    public void onActivate(ActivateEvent event, EntityRef entity) {
        MiniaturizerComponent comp = entity.getComponent(MiniaturizerComponent.class);
        BlockGrid grid = comp.blockGrid;


        HashSet<BlockGrid.GridPosition> gridPositions = grid.getGridPositions();

        if (gridPositions.size() < 2) {
            comp.reset();

            Vector3i targetPos = new Vector3i(event.getTargetLocation());

            entity.getComponent(MiniaturizerComponent.class).blockGrid.addGridPosition(targetPos, worldProvider.getBlock(targetPos).getId());
        }
        else {

            MiniatureChunk chunk = new MiniatureChunk();

            for (int x=grid.getMinBounds().x; x<=grid.getMaxBounds().x; ++x) {
                for (int y=grid.getMinBounds().y; y<=grid.getMaxBounds().y; ++y) {
                    for (int z=grid.getMinBounds().z; z<=grid.getMaxBounds().z; ++z) {
                        Vector3i localPos = new Vector3i();
                        Vector3i globalPos = new Vector3i(x,y,z);
                        localPos.sub(globalPos, grid.getMinBounds());

                        if (localPos.x >= MiniatureChunk.SIZE_X || localPos.y >= MiniatureChunk.SIZE_Y || localPos.z >= MiniatureChunk.SIZE_Z || localPos.x < 0 || localPos.y < 0 || localPos.z < 0)
                            continue;

                        chunk.setSunlight(localPos, (byte) 0x0F);
                        chunk.setLight(localPos, (byte) 0x0F);
                        chunk.setBlock(localPos, worldProvider.getBlock(globalPos).getId());
                    }
                }
            }

            Vector3f renderLocation = new Vector3f(event.getTargetLocation());
            renderLocation.y += 0.5f + (MiniaturizerComponent.SCALE / 2f);

            comp.miniatureChunk = chunk;
            comp.renderPosition = new Vector3f(renderLocation);

            grid.clear();
        }

    }
}
