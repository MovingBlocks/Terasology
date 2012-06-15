package org.terasology.mods.miniions.logic.pathfinder;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 10/05/12
 * Time: 23:00
 * To change this template use File | Settings | File Templates.
 */

import org.terasology.logic.world.WorldProvider;
import org.terasology.model.blocks.Block;

import javax.vecmath.Vector3f;
import java.util.List;

public abstract class Pathfinder {

    protected WorldProvider world;

    protected boolean pathSmoothing = false;


    public Pathfinder(WorldProvider provider) {
        this.world = provider;
    }

    public boolean isPathSmoothing() {
        return pathSmoothing;
    }

    public void setPathSmoothing(boolean pathSmoothing) {
        this.pathSmoothing = pathSmoothing;
    }

    protected List<Vector3f> smoothPath(List<Vector3f> path) {
        // @TODO Path smoothing algorithm
        return path;
    }

    protected boolean isPassable(Block block) {
        // @todo add more complicated check
        return block.isPenetrable();
    }

    protected boolean isPassable(int x, int y, int z) {
        // @todo add more complicated check
        return isPassable(world.getBlock(x, y, z));
    }

    protected boolean isPassable(Vector3f vec) {
        // @todo add more complicated check
        return isPassable(world.getBlock(vec));
    }


    protected boolean isWalkable(int x, int y, int z) {
        // @todo consider creature height
        return (isPassable(x, y + 1, z)) && (!isPassable(x, y, z));
    }

    protected boolean isWalkable(Vector3f vec) {
        return (isPassable(new Vector3f(vec.x, vec.y + 1, vec.z))) && (!isPassable(vec));
    }

    public abstract List<Vector3f> findPath(Vector3f from, Vector3f to);
}
