/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.physics.bullet.shapes;

import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.google.common.collect.Lists;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.terasology.physics.shapes.CollisionShape;
import org.terasology.physics.shapes.ConvexHullShape;

import java.nio.FloatBuffer;
import java.util.List;


public class BulletConvexHullShape extends BulletCollisionShape implements ConvexHullShape {
    // TODO: Handle scale
    private final btConvexHullShape convexHullShape;

    public BulletConvexHullShape(List<Vector3f> vertices) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.size() * 3);
        for (int i = 0; i < vertices.size(); i++) {
            Vector3f vertex = vertices.get(i);
            buffer.put(vertex.x);
            buffer.put(vertex.y);
            buffer.put(vertex.z);
        }
        buffer.rewind();
        this.convexHullShape = new btConvexHullShape(buffer, vertices.size(), 3 * Float.BYTES);
        this.underlyingShape = convexHullShape;
    }

    public BulletConvexHullShape(FloatBuffer buffer, int numPoints, int stride) {
        this.convexHullShape = new btConvexHullShape(buffer, numPoints, stride);
        this.underlyingShape = convexHullShape;
    }

    @Override
    public CollisionShape rotate(Quaternionf rot) {
        List<Vector3f> verts = Lists.newArrayList();
        for (int x = 0; x < convexHullShape.getNumPoints(); x++) {
            Vector3f p = new Vector3f(convexHullShape.getScaledPoint(x));
            rot.transform(p);
            verts.add(p);
        }
        return new BulletConvexHullShape(verts);
    }

    @Override
    public Vector3f[] getVertices() {
        Vector3f[] verts = new Vector3f[convexHullShape.getNumPoints()];
        for (int x = 0; x < convexHullShape.getNumPoints(); x++) {
            Vector3f p = new Vector3f(convexHullShape.getScaledPoint(x));
            verts[x] = p;
        }
        return verts;
    }
}
