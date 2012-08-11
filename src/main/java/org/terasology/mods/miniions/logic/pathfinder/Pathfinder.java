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
package org.terasology.mods.miniions.logic.pathfinder;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 10/05/12
 * Time: 23:00
 * To change this template use File | Settings | File Templates.
 */

import java.util.List;

import javax.vecmath.Vector3f;

import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

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
