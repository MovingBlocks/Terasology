// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet.shapes;

import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.util.ObjectArrayList;
import org.terasology.engine.math.VecMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.engine.physics.shapes.CollisionShape;

import java.util.List;
import java.util.stream.Collectors;

public class BulletConvexHullShape extends BulletCollisionShape implements org.terasology.engine.physics.shapes.ConvexHullShape {
    // TODO: Handle scale
    private final ConvexHullShape convexHullShape;

    public BulletConvexHullShape(List<Vector3f> vertices) {
        ObjectArrayList<javax.vecmath.Vector3f> vertexList = new ObjectArrayList<>();

        vertexList.addAll(vertices.stream().map(VecMath::to).collect(Collectors.toList()));

        convexHullShape = new ConvexHullShape(vertexList);
        underlyingShape = convexHullShape;
    }

    private BulletConvexHullShape(ObjectArrayList<javax.vecmath.Vector3f> vertexList) {
        convexHullShape = new ConvexHullShape(vertexList);
        underlyingShape = convexHullShape;
    }

    @Override
    public CollisionShape rotate(Quat4f rot) {
        ObjectArrayList<javax.vecmath.Vector3f> transformedVerts = new ObjectArrayList<>();
        for (javax.vecmath.Vector3f vert : convexHullShape.getPoints()) {
            transformedVerts.add(com.bulletphysics.linearmath.QuaternionUtil.quatRotate(VecMath.to(rot), vert, new javax.vecmath.Vector3f()));
        }
        return new BulletConvexHullShape(transformedVerts);
    }

    @Override
    public Vector3f[] getVertices() {
        return convexHullShape.getPoints().stream()
                .map(VecMath::from)
                .toArray(Vector3f[]::new);
    }
}
