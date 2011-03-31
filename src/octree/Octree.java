/*
 *  Copyright 2011 Benjamin Glatzel.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package octree;

import blockmania.Chunk;
import org.lwjgl.util.vector.Vector3f;

/**
 * Octree as shown in the book "Physically Based Rendering".
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Octree {

    BBox bound;
    int maxDepth = 16;
    OctNode root = new OctNode();

    Octree(BBox b) {
        this.bound = b;
    }

    public void add(Chunk dataItem, BBox dataBound) {
        addPrivate(root, bound, dataItem, dataBound, distanceSquared(dataBound.pMin, dataBound.pMax), 0);
    }

    private void addPrivate(OctNode node, BBox nodeBound, Chunk dataItem, BBox dataBound, float diag2, int depth) {
        if (depth == maxDepth || distanceSquared(nodeBound.pMin, nodeBound.pMax) < diag2) {
            node.data.add(dataItem);
            return;
        }

        Vector3f pMid = Vector3f.add(Vector3f.cross(new Vector3f(0.5f, 0.5f, 0.5f), nodeBound.pMin, null), Vector3f.cross(new Vector3f(0.5f, 0.5f, 0.5f), nodeBound.pMax, null), null);

        boolean x[] = {dataBound.pMin.x <= pMid.x, dataBound.pMax.x > pMid.x};
        boolean y[] = {dataBound.pMin.y <= pMid.y, dataBound.pMax.y > pMid.y};
        boolean z[] = {dataBound.pMin.z <= pMid.z, dataBound.pMax.z > pMid.z};

        boolean over[] = {x[0] & y[0] & z[0], x[0] & y[0] & z[1],
            x[0] & y[1] & z[0], x[0] & y[1] & z[1],
            x[1] & y[0] & z[0], x[1] & y[0] & z[1],
            x[1] & y[1] & z[0], x[1] & y[1] & z[1]
        };

        for (int child = 0; child < 8; ++child) {
            if (!over[child]) {
                continue;
            }

            if (node.children[child] == null) {
                node.children[child] = new OctNode();
            }

            BBox childBound = octreeChildBound(child, nodeBound, pMid);
            addPrivate(node.children[child], childBound, dataItem, dataBound, diag2, depth + 1);
        }
    }

    private float distanceSquared(Vector3f p1, Vector3f p2) {
        float result = Vector3f.sub(p1, p2, null).lengthSquared();
        return result;
    }

    private BBox octreeChildBound(int child, BBox nodeBound, Vector3f pMid) {
        BBox childBound = new BBox();
        childBound.pMin.x = (child & 4) != 0 ? pMid.x : nodeBound.pMin.x;
        childBound.pMax.x = (child & 4) != 0 ? nodeBound.pMax.x : pMid.x;
        childBound.pMin.y = (child & 2) != 0 ? pMid.y : nodeBound.pMin.y;
        childBound.pMax.y = (child & 2) != 0 ? nodeBound.pMax.y : pMid.y;
        childBound.pMin.z = (child & 1) != 0 ? pMid.z : nodeBound.pMin.z;
        childBound.pMax.z = (child & 1) != 0 ? nodeBound.pMax.z : pMid.z;
        return childBound;
    }

    public void lookup(Vector3f p, OctLookup lookup) {
        if (!bound.inside(p)) {
            return;
        }
        lookupPrivate(root, bound, p, lookup);
    }

    private boolean lookupPrivate(OctNode node, BBox nodeBound, Vector3f p, OctLookup lookup) {
        for (Chunk c : node.data) {
            if (!lookup.lookup(c))
                return false;
        }

        Vector3f pMid = Vector3f.add(Vector3f.cross(new Vector3f(0.5f, 0.5f, 0.5f), nodeBound.pMin, null), Vector3f.cross(new Vector3f(0.5f, 0.5f, 0.5f), nodeBound.pMax, null), null);
        int child = (p.x > pMid.x ? 4 : 0) + (p.y > pMid.y ? 2 : 0) + (p.z > pMid.z ? 1 : 0);

        if (node.children[child] == null) {
            return true;
        }

        BBox childBound = octreeChildBound(child, nodeBound, pMid);

        return lookupPrivate(node.children[child], childBound, p, lookup);

    }
}
