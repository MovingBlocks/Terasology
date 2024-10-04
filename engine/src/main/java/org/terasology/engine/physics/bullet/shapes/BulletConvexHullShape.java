// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet.shapes;

import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.google.common.collect.Lists;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.terasology.engine.physics.shapes.CollisionShape;
import org.terasology.engine.physics.shapes.ConvexHullShape;

import java.nio.FloatBuffer;
import java.util.List;


public class BulletConvexHullShape extends BulletCollisionShape implements ConvexHullShape {
    // TODO: Handle scale
    private final btConvexHullShape convexHullShape;

    public BulletConvexHullShape(List<Vector3f> vertices) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.size() * 3);
        for (Vector3f vertex : vertices) {
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
