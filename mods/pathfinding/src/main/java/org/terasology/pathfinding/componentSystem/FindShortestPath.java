package org.terasology.pathfinding.componentSystem;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.model.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class helps finding and maintaining the shortest path between a start position and a list of target positions.
 *
 * A path is requested for each target. Once the shortest of all possible paths is found, the callback is informed.
 * Also, if (and only if) the current path is invalidated, the callback is informed, too.
 *
 * @author synopia
 */
public class FindShortestPath {
    private static final Logger logger = LoggerFactory.getLogger(FindShortestPath.class);

    private Map<Vector3i, PathfinderSystem.FindPathTask> targets = new HashMap<Vector3i, PathfinderSystem.FindPathTask>();
    private Map<Vector3i, Path> results = new HashMap<Vector3i, Path>();
    private Vector3i start;
    private Path currentPath;
    private PathfinderSystem pathfinder;
    private PathfinderSystem.PathRequest pathRequest;

    public FindShortestPath(PathfinderSystem pathfinder, PathfinderSystem.PathRequest pathRequest) {
        this.pathfinder = pathfinder;
        this.pathRequest = pathRequest;
    }

    public void updateRequests( Vector3i newStart, List<Vector3i> newTargets ) {
        if( !newStart.equals(start) ) {
            start = newStart;
            kill();
        }
        Set<Vector3i> currentTasks = Sets.newHashSet(targets.keySet());
        if( newTargets!=null && newTargets.size()>0 ) {
            for (final Vector3i newTarget : newTargets) {
                if( !currentTasks.remove(newTarget) ) {
                    logger.info("Request path "+newStart+" -> "+newTarget);
                    targets.put(newTarget, pathfinder.requestPath(start, newTarget, new PathfinderSystem.PathRequest() {
                        @Override
                        public void onPathReady(Path path) {
                            results.put(newTarget, path);

                            currentPath = null;
                            for (Map.Entry<Vector3i, Path> entry : results.entrySet()) {
                                Path p = entry.getValue();
                                if( currentPath==null || currentPath==Path.INVALID || (p!=Path.INVALID && currentPath.size()> p.size()) ) {
                                    currentPath = p;
                                }
                            }
                            pathRequest.onPathReady(currentPath);
                        }

                        @Override
                        public void invalidate() {
                            if( newTarget.equals(newTarget) ) {
                                currentPath = null;
                                pathRequest.invalidate();
                            }
                            results.remove(newTarget);
                        }
                    }));
                }
            }
        }
        for (Vector3i currentTask : currentTasks) {
            targets.remove(currentTask).kill();
            if( results.remove(currentTask)==currentPath) {
                currentPath = null;
                pathRequest.invalidate();
            }
        }
    }

    public void kill() {
        for (Map.Entry<Vector3i, PathfinderSystem.FindPathTask> entry : targets.entrySet()) {
            entry.getValue().kill();
        }
        targets.clear();
        results.clear();
        currentPath = null;
        pathRequest.invalidate();
    }

}
