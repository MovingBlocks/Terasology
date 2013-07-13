package org.terasology.ligthandshadow.componentsystem.controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.ligthandshadow.components.AnimationComponent;
import org.terasology.ligthandshadow.components.MinionComponent;
import org.terasology.ligthandshadow.components.TargetBlockComponent;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.componentSystem.FindShortestPath;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.Path;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Map;


/**
 * System responsible for requesting and updating paths for minions.
 * Each minion searches for the nearest target block of its side.
 *
 * When found any path, the minion moves along the path until it reaches the target block, where it gets destroyed
 *
 * @author synopia
 */
@RegisterComponentSystem
public class MinionPathSystem implements EventHandlerSystem, UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(MinionPathSystem.class);

    private EntityManager entityManager;
    private WorldProvider worldProvider;
    private PathfinderSystem system;
    private Map<String,List<Vector3i>> sideTargets;
    private Map<EntityRef, MinionPathState> pathStates;

    private class MinionPathState {
        public EntityRef minion;
        public String side;
        public FindShortestPath findShortestPath;
        public Vector3i currentStart;

        private MinionPathState(final EntityRef minion) {
            this.minion = minion;
            findShortestPath = new FindShortestPath(system, new PathfinderSystem.PathRequest() {
                @Override
                public void onPathReady(Path path) {
                    if( path==null ) {
                        return;
                    }
                    logger.info("Minion "+minion+" got path (len="+path.size()+")");
                    MinionComponent component = minion.getComponent(MinionComponent.class);
                    component.path = path;
                    component.receivedNewPath = true;
                    minion.saveComponent(component);
                }

                @Override
                public void invalidate() {
                    MinionComponent component = minion.getComponent(MinionComponent.class);
                    component.path = null;
                    component.receivedNewPath = true;
                    minion.saveComponent(component);
                }
            });
        }

        public void updatePaths(List<Vector3i> newTargets) {
            if( currentStart==null ) {
                LocationComponent component = minion.getComponent(LocationComponent.class);
                Vector3f worldPosition = component.getWorldPosition();
                currentStart = new Vector3i(worldPosition.x+0.5f, worldPosition.y+1, worldPosition.z+0.5f);
                while (currentStart.y>0 && system.getBlock(currentStart)==null ) {
                    currentStart.add(0,-1,0);
                }
                if( currentStart.y==0 ) {
                    currentStart = null;
                }
            }
            if( currentStart!=null ) {
                findShortestPath.updateRequests(currentStart, newTargets);
            } else {
                findShortestPath.kill();
            }
        }

        public void kill() {
            findShortestPath.kill();
        }
    }

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);
        system = CoreRegistry.get(PathfinderSystem.class);
        sideTargets = Maps.newHashMap();
        pathStates = Maps.newHashMap();
    }

    @Override
    public void update(float delta) {
        updateTargets();

        for (EntityRef entity : entityManager.iteratorEntities(MinionComponent.class)) {
            MinionComponent minion = entity.getComponent(MinionComponent.class);
            MinionPathState pathState = pathStates.get(entity);
            if( minion.dead ) {
                continue;
            }
            List<Vector3i> targetList = sideTargets.get(minion.side);
            if( pathState==null ) {
                logger.info("New minion "+entity);
                pathState = new MinionPathState(entity);
                pathState.side = minion.side;
                pathStates.put(entity, pathState);
            }
            pathState.updatePaths(targetList);

            if( minion.targetBlock==null ) {
                if( minion.receivedNewPath ) {
                    minion.pathStep = 0;
                    minion.receivedNewPath = false;
                    pathState.currentStart = null;
                    entity.saveComponent(minion);
                }
                if( minion.path!=null && minion.path!=Path.INVALID ) {
                    if( minion.pathStep>=minion.path.size() ) {
                        minion.dead = true;
                        entity.saveComponent(minion);
                        pathState.kill();
                        pathStates.remove(entity);
                    } else {
                        WalkableBlock target = minion.path.get(minion.pathStep);
                        minion.targetBlock = target.getBlockPosition();
                        minion.pathStep++;
                        entity.saveComponent(minion);
                    }
                }
            }
        }
    }

    private void updateTargets() {
        sideTargets.clear();
        for (EntityRef entity : entityManager.iteratorEntities(TargetBlockComponent.class)) {
            TargetBlockComponent component = entity.getComponent(TargetBlockComponent.class);
            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3i position = new Vector3i(location.getWorldPosition());
            List<Vector3i> targets = sideTargets.get(component.side);
            if( targets==null ) {
                targets = Lists.newArrayList();
                sideTargets.put(component.side, targets);
            }
            targets.add(position);
        }
    }

    @Override
    public void shutdown() {
    }

}
