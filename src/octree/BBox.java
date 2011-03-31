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

import org.lwjgl.util.vector.Vector3f;

/**
 * Bounding box as shown in the book "Physically Based Rendering".
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BBox {

    Vector3f pMin, pMax;

    BBox() {
        pMin = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        pMax = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    }

    BBox(Vector3f p1, Vector3f p2) {
        pMin = new Vector3f(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.min(p1.z, p2.z));
        pMax = new Vector3f(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y), Math.max(p1.z, p2.z));
    }

    public BBox union(BBox b, Vector3f p) {
        BBox ret = b;
        ret.pMin.x = Math.min(b.pMin.x, p.x);
        ret.pMin.y = Math.min(b.pMin.y, p.y);
        ret.pMin.z = Math.min(b.pMin.z, p.z);
        ret.pMax.x = Math.max(b.pMax.x, p.x);
        ret.pMax.y = Math.max(b.pMax.y, p.y);
        ret.pMax.z = Math.max(b.pMax.z, p.z);
        return ret;
    }

    public boolean overlaps(BBox b) {
        boolean x = (pMax.x >= b.pMin.x) && (pMin.x <= b.pMax.x);
        boolean y = (pMax.y >= b.pMin.y) && (pMin.y <= b.pMax.y);
        boolean z = (pMax.z >= b.pMin.z) && (pMin.z <= b.pMax.z);
        return (x && y && z);
    }

    public boolean inside(Vector3f pt) {
        return (pt.x >= pMin.x && pt.x <= pMax.x && pt.y >= pMin.y && pt.y <= pMax.y && pt.z >= pMin.z && pt.z <= pMax.z);
    }
}
