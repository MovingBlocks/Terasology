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
package org.terasology.miniion.pathfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.vecmath.Vector3f;

import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.world.WorldProvider;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 17/05/12
 * Time: 10:08
 * To change this template use File | Settings | File Templates.
 */
public class AStarPathing extends Pathfinder {

    private static int PATH_COST_LIMIT_MULTIPLIER = 5;
    protected Map<Integer, PathNode> pathNodeCache = new HashMap<Integer, PathNode>(20); // storing objects in hash map by object hash code
    static byte[][] defaultNeighborMatrix = new byte[][]{
            {-1, 0, 0}, {1, 0, 0}, {0, 0, -1}, {0, 0, 1} // horizontal X and Z
    };

    public AStarPathing(WorldProvider provider) {
        super(provider);
    }

    public List<Vector3f> findPath(Vector3f from, Vector3f to) {
        pathNodeCache.clear();
        Queue<PathNode> openSet = new PriorityQueue<PathNode>(20);
        PathNode target = new PathNode((int) to.x / 1, (int) to.y / 1, (int) to.z / 1);
        PathNode start = new PathNode((int) from.x / 1, (int) from.y / 1, (int) from.z / 1);
        Vector3f tempvec = to;
        tempvec.sub(from);
        if (tempvec.length() < 0.5) {
            return null;
        }
        openSet.add(start);
        start.update(0, null, target);
        while (!openSet.isEmpty()) {
            PathNode node = openSet.poll();
            if (target.equals(node)) { // we found destination
                //PerformanceMonitor.endActivity();
                System.out.println("Path found!");
                return rebuildPath(node);
            }
            byte[][] neighborsMatrix = defaultNeighborMatrix;
            for (byte[] matrix : neighborsMatrix) {
                int nx = node.x + matrix[0];
                int ny = node.y + matrix[1] + 1; //// added 1, start checking neighbor with y+1, as y-1 might be unreachable in 3D if you think about it, but still passable
                int nz = node.z + matrix[2];

                //loop through possible neighbours from top to bottom, return the first walkable one if it exists
                boolean canskip = true;
                for (int j = 0; j > -3; j--) {
                    if (isWalkable(nx, ny + j, nz)) {
                        ny = ny + j;
                        canskip = false;
                        break;
                    }
                }
                if (canskip) {
                    continue;
                }

                PathNode neighbor = new PathNode(nx, ny, nz);
                //filter out parent
                if (node.parent != null) {
                    if (node.parent.equals(neighbor)) {
                        continue;
                    }
                }

                int pathCost = node.getCost(neighbor);
                // block is forbidden skip to next
                if (pathCost == 0) {
                    continue;
                }
                // pathfinder limiter exceeded => skip to next
                pathCost += node.pathCost;
                if (pathCost > start.totalCost * PATH_COST_LIMIT_MULTIPLIER) {
                    continue;
                }
                neighbor.update(pathCost, node, target);

                if (!openSet.contains(neighbor)) {
                    neighbor.store();
                    openSet.add(neighbor);
                }
            }
            /*
            if(openSet.size() > 3)
            {
                Iterator<PathNode> it = openSet.iterator();
                PathNode first = it.next();
                PathNode[] closedSet = new PathNode[8];
                int loopCounter = 0;
                try{
                while(it.hasNext()){
                    PathNode tmpNode = it.next();                 wwwwwwwwwwwwwwww
                    if(tmpNode.totalCost > first.totalCost) {
                        closedSet[loopCounter] = tmpNode;
                        loopCounter++;
                    }
                }

                for(int i = 0;i<loopCounter; i++){
                    openSet.remove(closedSet[i]);
                }}catch (ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();}
            }*/
        }
        System.out.println("Path not found!");
        PerformanceMonitor.endActivity();
        return null; // path not found
    }

    protected List<Vector3f> rebuildPath(PathNode targetNode) {
        List<Vector3f> path = new ArrayList<Vector3f>();

        PathNode node = targetNode;


        while (node != null) {
            if (node.parent == null) { // skip starting node
                node = node.parent; //skipping the first point in the list
                break;
            }

            //world.setBlock(node.x, node.y, node.z, (byte) 22, true, true);

            path.add(0, node.getCenterVector());
            node = node.parent;
        }

        return path;
    }

    public class PathNode implements Comparable<PathNode> {

        public int x, y, z;

        public int pathCost = 0; // path cost from start point
        public int heuristicCost = 0; // heuristic cost to end
        public int totalCost = 0; // total cost (pathCost + heuristicCost)

        public PathNode parent = null;

        public PathNode(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getPathNodeHashCode(int x, int y, int z, int total) {
            // @todo more reliable hashing algorithm
            return total * (31 * x + y) + z;
        }

        public void store() {
            int hashCode = getPathNodeHashCode(x, y, z, totalCost);
            if (!pathNodeCache.containsKey(hashCode)) {
                pathNodeCache.put(hashCode, new PathNode(x, y, z));
            }
        }

        public Vector3f getCenterVector() {
            return new Vector3f(x, y + 0.5f, z);
        }

        public int getHeuristicCost(PathNode to) {
            return 10 * (Math.abs(to.x - this.x) + Math.abs(to.y - this.y) + Math.abs(to.z - this.z));
        }

        public int getCost(PathNode to) {
            // @todo add fetching path cost from block configuration

            if (!isWalkable(to.x, to.y, to.z)) { // check if block is passable
                return 0;
            }

            int pathCost = 10; // basic path cost

            return pathCost; //basic cost
        }

        public void update(int g, PathNode parent, PathNode target) {
            this.parent = parent;
            this.pathCost = g;
            this.heuristicCost = this.getHeuristicCost(target);
            this.totalCost = g + heuristicCost;
        }

        @Override
        public String toString() {
            return "PathNode{x=" + x + ", y=" + y + ", z=" + z + ", g=" + pathCost + ", h=" + heuristicCost + ", f=" + totalCost + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if ((o == null) || ((getClass() != o.getClass()))) {
                return false;
            }

            PathNode pathNode = (PathNode) o;

            if (x != pathNode.x) {
                return false;
            }
            if (y != pathNode.y) {
                return false;
            }
            if (z != pathNode.z) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(PathNode o) {
            if ((this.totalCost > o.totalCost) || (this.totalCost == 0)) {
                return 1;
            } else if (this.totalCost < o.totalCost) {
                return -1;
            }
            return 0;
        }
    }
}
