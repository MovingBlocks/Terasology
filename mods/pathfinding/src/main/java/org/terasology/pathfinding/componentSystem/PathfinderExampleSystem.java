package org.terasology.pathfinding.componentSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.components.StartBlockComponent;
import org.terasology.pathfinding.components.TargetBlockComponent;
import org.terasology.pathfinding.model.HeightMap;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.pathfinding.rendering.InfoGrid;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.management.BlockManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author synopia
 */
@RegisterComponentSystem
public class PathfinderExampleSystem implements EventHandlerSystem, UpdateSubscriberSystem, RenderSystem {
    private static final Logger logger = LoggerFactory.getLogger(PathfinderExampleSystem.class);
    private WorldProvider world;
    private Block startBlock;

    private class PathState {
        public Vector3i start;
        public Map<Vector3i, PathfinderSystem.FindPathTask> targets = new HashMap<Vector3i, PathfinderSystem.FindPathTask>();
        public Map<Vector3i, Path> results = new HashMap<Vector3i, Path>();
        public Path path;

        private PathState(Vector3i start) {
            this.start = start;
        }

        public boolean removeTask( Vector3i target ) {
            PathfinderSystem.FindPathTask old = targets.remove(target);
            if( old!=null ) {
                old.kill();
                return true;
            }
            return false;
        }

        public void addTask( final Vector3i target ) {
            if( targets.containsKey(target) ) {
                return;
            }
            PathfinderSystem.FindPathTask current = system.requestPath(start, target, new PathfinderSystem.Callback() {
                @Override
                public void onPathReady(Path path) {
                    results.put(target, path);
                    PathState.this.path = null;
                    for (Map.Entry<Vector3i, Path> entry : results.entrySet()) {
                        Path p = entry.getValue();
                        if( PathState.this.path==null || (p!=Path.INVALID && PathState.this.path.size()> p.size()) ) {
                            PathState.this.path = p;
                        }
                    }
                }
            });
            targets.put(target, current);
        }
    }

    private Map<Vector3i, PathState> startBlocks = new HashMap<Vector3i, PathState>();
    private List<Vector3i> targetBlocks = new ArrayList<Vector3i>();
    private InfoGrid infoGrid;
    private PathfinderSystem system;

    @Override
    public void initialise() {
        system = CoreRegistry.get(PathfinderSystem.class);
        infoGrid = new InfoGrid();
        world = CoreRegistry.get(WorldProvider.class);
        startBlock = BlockManager.getInstance().getBlock("pathfinding:StartBlock");
    }

    @Override
    public void update(float delta) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        Vector3i currentBlock = new Vector3i(localPlayer.getPosition());
        Vector3i chunk = TeraMath.calcChunkPos(currentBlock.x, currentBlock.y, currentBlock.z);

        HeightMap map = system.getHeightMap(chunk);
        infoGrid.removeCategory("info");
        if( map!=null ) {
            for (WalkableBlock block : map.walkableBlocks) {
                boolean contour = block.floor.isContour(block);
                String text = block.floor.heightMap.worldPos.toString();
                text += "\nfid: " + block.floor.id;
                if( contour ) {
                    text+="\ncontour";
                    infoGrid.addInfo(block.getBlockPosition(), "info", text);
                }
            }
        }

        infoGrid.removeCategory("path");
        for (Map.Entry<Vector3i, PathState> entry : startBlocks.entrySet()) {
            PathState component = entry.getValue();

            if( component.path != null && component.path!=Path.INVALID ) {
                int no = 1;
                for (WalkableBlock block : component.path) {
                    infoGrid.addInfo(block.getBlockPosition(), "path", "step: "+no);
                    no++;
                }
            }
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, StartBlockComponent.class})
    public void startBlockAdded( final AddComponentEvent event, final EntityRef block ) {
        Vector3i blockPos = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
        PathState pathState = startBlocks.get(blockPos);
        if( pathState==null ) {
            pathState = new PathState(blockPos);
            startBlocks.put(blockPos, pathState);
        }
        for (Vector3i targetBlock : targetBlocks) {
            pathState.addTask(targetBlock);
        }
        startBlocks.put(blockPos, pathState);
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, StartBlockComponent.class})
    public void startBlockUpdated( ChangedComponentEvent event, EntityRef block ) {
    }
    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, StartBlockComponent.class})
    public void startBlockRemoved( RemovedComponentEvent event, EntityRef block ) {
        Vector3i blockPos = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
        PathState pathState = startBlocks.get(blockPos);
        if( pathState==null ) {
            return;
        }
        for (PathfinderSystem.FindPathTask task : pathState.targets.values()) {
            task.kill();
        }
        startBlocks.remove(blockPos);
    }
    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, TargetBlockComponent.class})
    public void targetBlockAdded( final AddComponentEvent event, final EntityRef block ) {
        Vector3i blockPos = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
        if( targetBlocks.contains(blockPos) ) {
            return;
        }
        for (Map.Entry<Vector3i, PathState> entry : startBlocks.entrySet()) {
            final PathState pathState = entry.getValue();
            pathState.addTask(blockPos);
        }
        targetBlocks.add(blockPos);

//        for (int z = -3; z <=3; z++) {
//            for (int x = -3; x <= 3; x++) {
//                if( x==0 && z==0 ) {
//                    continue;
//                }
//                Vector3i pos = new Vector3i(blockPos.x+x* Chunk.SIZE_X, blockPos.y, blockPos.z+z*Chunk.SIZE_Z);
//                Block old = world.getBlock(pos);
//                world.setBlock(pos, startBlock, old);
//            }
//        }
    }
    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, TargetBlockComponent.class})
    public void targetBlockUpdated( ChangedComponentEvent event, EntityRef block ) {
    }
    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, TargetBlockComponent.class})
    public void targetBlockRemoved( RemovedComponentEvent event, EntityRef block ) {
        Vector3i blockPos = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
        if( !targetBlocks.contains(blockPos) ) {
            return;
        }
        for (Map.Entry<Vector3i, PathState> entry : startBlocks.entrySet()) {
            PathState pathState = entry.getValue();
            pathState.removeTask(blockPos);

        }
        targetBlocks.remove(blockPos);
    }


    @Override
    public void shutdown() {
    }

    @Override
    public void renderOpaque() {
    }

    @Override
    public void renderTransparent() {
    }

    @Override
    public void renderOverlay() {
        infoGrid.render();
    }

    @Override
    public void renderFirstPerson() {
    }

    @Override
    public void renderShadows() {
    }

}
