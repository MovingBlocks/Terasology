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



import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import org.terasology.math.Transform;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.physics.shapes.CollisionShape;

import java.util.ArrayList;
import java.util.List;

public class BulletCompoundShape extends BulletCollisionShape implements org.terasology.physics.shapes.CompoundShape {
    private final btCompoundShape compoundShape;
    private List<BulletCompoundShapeChild> childList;

    public BulletCompoundShape() {
        this(new btCompoundShape());
    }

    private BulletCompoundShape(btCompoundShape compoundShape) {
        this.compoundShape = compoundShape;
        underlyingShape = compoundShape;

        childList = new ArrayList<>();
    }

    @Override
    public void addChildShape(org.terasology.math.Transform transform, CollisionShape collisionShape) {
        BulletCollisionShape bulletCollisionShape = (BulletCollisionShape) collisionShape;
        compoundShape.addChildShape(new Matrix4f(transform.rotation,transform.origin,transform.scale),((BulletCollisionShape) collisionShape).underlyingShape);
        childList.add(new BulletCompoundShapeChild(new Transform(transform.origin,transform.rotation,transform.scale), bulletCollisionShape,compoundShape.getChildShape(compoundShape.getNumChildShapes() -1 )));
    }

    // TODO: Add removeChildShape if needed

    @Override
    public CollisionShape rotate(Quat4f rot) {
        btCompoundShape newShape = new btCompoundShape();
        for (BulletCompoundShapeChild child : childList) {
            CollisionShape rotatedChild = child.childShape.rotate(rot);

//            rot.rotate(child.transform.origin)
//            Vector3f offset = com.bulletphysics.linearmath.QuaternionUtil.quatRotate(VecMath.to(rot), child.transform.origin, new javax.vecmath.Vector3f());
//            newShape.addChildShape(new Transform(new javax.vecmath.Matrix4f(VecMath.to(Rotation.none().getQuat4f()), offset, 1.0f)), ((BulletCollisionShape) rotatedChild).underlyingShape);
        }
        return new BulletCompoundShape(newShape);
    }

    private static class BulletCompoundShapeChild {
        public Transform transform;
        public BulletCollisionShape childShape;

        public btCollisionShape compoundShapeChild;

        private BulletCompoundShapeChild(Transform transform, BulletCollisionShape childShape, btCollisionShape compoundShapeChild) {
            this.transform = transform;
            this.childShape = childShape;
            this.compoundShapeChild = compoundShapeChild;
        }
    }
}
