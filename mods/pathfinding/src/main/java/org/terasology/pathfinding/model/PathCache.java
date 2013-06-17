package org.terasology.pathfinding.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author synopia
 */
public class PathCache {
    private Map<WalkableBlock, Map<WalkableBlock, Path>> paths = new HashMap<WalkableBlock, Map<WalkableBlock, Path>>();

    public interface Callback {
        Path run( WalkableBlock from, WalkableBlock to );
    }

    public Path getCachedPath( WalkableBlock from, WalkableBlock to ) {
        Map<WalkableBlock, Path> fromMap = paths.get(from);
        if(fromMap!=null) {
            return fromMap.get(to);
        }
        return null;
    }

    private void insert( WalkableBlock from, WalkableBlock to, Path path ) {
        Map<WalkableBlock, Path> fromMap = paths.get(from);
        if(fromMap==null) {
            fromMap = new HashMap<WalkableBlock, Path>();
            paths.put(from, fromMap);
        }
        fromMap.put(to, path );
    }

    public boolean hasPath( WalkableBlock from, WalkableBlock to ) {
        return getCachedPath(from, to)!=null;
    }
    public Path findPath( WalkableBlock from, WalkableBlock to, Callback callback ) {
        Path path = null; //getCachedPath(from, to);
//        if( path==null ) {
        path = callback.run(from, to);
        insert(from, to, path);
        insert(to, from, path);
//        }
        return path;
    }

    public void clear() {
        for (Map.Entry<WalkableBlock, Map<WalkableBlock, Path>> entry : paths.entrySet()) {
            entry.getValue().clear();
        }
        paths.clear();
    }
}
