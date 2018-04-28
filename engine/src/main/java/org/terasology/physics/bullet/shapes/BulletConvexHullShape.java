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

import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.util.ObjectArrayList;
import org.terasology.math.AABB;
import org.terasology.math.Transform;
import org.terasology.math.VecMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.shapes.CollisionShape;

import java.util.List;
import java.util.stream.Collectors;

public class BulletConvexHullShape extends BulletCollisionShape implements org.terasology.physics.shapes.ConvexHullShape {
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
}
