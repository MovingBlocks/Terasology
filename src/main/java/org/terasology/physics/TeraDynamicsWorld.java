/*
 * Copyright 2012
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

package org.terasology.physics;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.ConvexCast;
import com.bulletphysics.collision.narrowphase.GjkConvexCast;
import com.bulletphysics.collision.narrowphase.GjkEpaPenetrationDepthSolver;
import com.bulletphysics.collision.narrowphase.VoronoiSimplexSolver;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.TransformUtil;
import com.bulletphysics.linearmath.VectorUtil;
import cz.advel.stack.Stack;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class TeraDynamicsWorld extends DiscreteDynamicsWorld {
    public TeraDynamicsWorld(Dispatcher dispatcher, BroadphaseInterface pairCache, ConstraintSolver constraintSolver, CollisionConfiguration collisionConfiguration) {
        super(dispatcher, pairCache, constraintSolver, collisionConfiguration);
    }

    /**
     * convexTest performs a swept convex cast on all objects in the {@link com.bulletphysics.collision.dispatch.CollisionWorld}, and calls the resultCallback
     * This allows for several queries: first hit, all hits, any hit, dependent on the value return by the callback.
     */
    public void convexSweepTest(ConvexShape castShape, Transform convexFromWorld, Transform convexToWorld, ConvexResultCallback resultCallback) {
        Transform convexFromTrans = Stack.alloc(Transform.class);
        Transform convexToTrans = Stack.alloc(Transform.class);

        convexFromTrans.set(convexFromWorld);
        convexToTrans.set(convexToWorld);

        Vector3f castShapeAabbMin = Stack.alloc(Vector3f.class);
        Vector3f castShapeAabbMax = Stack.alloc(Vector3f.class);

        // Compute AABB that encompasses angular movement
        {
            Vector3f linVel = Stack.alloc(Vector3f.class);
            Vector3f angVel = Stack.alloc(Vector3f.class);
            TransformUtil.calculateVelocity(convexFromTrans, convexToTrans, 1f, linVel, angVel);
            Transform R = Stack.alloc(Transform.class);
            R.setIdentity();
            R.setRotation(convexFromTrans.getRotation(Stack.alloc(Quat4f.class)));
            castShape.calculateTemporalAabb(R, linVel, angVel, 1f, castShapeAabbMin, castShapeAabbMax);
        }

        Transform tmpTrans = Stack.alloc(Transform.class);
        Vector3f collisionObjectAabbMin = Stack.alloc(Vector3f.class);
        Vector3f collisionObjectAabbMax = Stack.alloc(Vector3f.class);
        float[] hitLambda = new float[1];

        // go over all objects, and if the ray intersects their aabb + cast shape aabb,
        // do a ray-shape query using convexCaster (CCD)
        for (int i = 0; i < collisionObjects.size(); i++) {
            CollisionObject collisionObject = collisionObjects.getQuick(i);

            // only perform raycast if filterMask matches
            if (resultCallback.needsCollision(collisionObject.getBroadphaseHandle())) {
                //RigidcollisionObject* collisionObject = ctrl->GetRigidcollisionObject();
                collisionObject.getWorldTransform(tmpTrans);
                collisionObject.getCollisionShape().getAabb(tmpTrans, collisionObjectAabbMin, collisionObjectAabbMax);
                AabbUtil2.aabbExpand(collisionObjectAabbMin, collisionObjectAabbMax, castShapeAabbMin, castShapeAabbMax);
                hitLambda[0] = 1f; // could use resultCallback.closestHitFraction, but needs testing
                Vector3f hitNormal = Stack.alloc(Vector3f.class);
                if (AabbUtil2.rayAabb(convexFromWorld.origin, convexToWorld.origin, collisionObjectAabbMin, collisionObjectAabbMax, hitLambda, hitNormal)) {
                    objectQuerySingleExtended(castShape, convexFromTrans, convexToTrans,
                            collisionObject,
                            collisionObject.getCollisionShape(),
                            tmpTrans,
                            resultCallback,
                            getDispatchInfo().allowedCcdPenetration);
                }
            }
        }
    }

    private BoxShape defaultBox = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));

    /**
     * objectQuerySingle performs a collision detection query and calls the resultCallback. It is used internally by rayTest.
     */
    public void objectQuerySingleExtended(ConvexShape castShape, Transform convexFromTrans, Transform convexToTrans, CollisionObject collisionObject, CollisionShape collisionShape, Transform colObjWorldTransform, ConvexResultCallback resultCallback, float allowedPenetration) {
        if (collisionShape.getShapeType() == BroadphaseNativeType.INVALID_SHAPE_PROXYTYPE) {
            WorldShape worldShape = (WorldShape) collisionShape;
            Vector3f minAABB1 = new Vector3f();
            Vector3f maxAABB1 = new Vector3f();
            Vector3f minAABB2 = new Vector3f();
            Vector3f maxAABB2 = new Vector3f();
            castShape.getAabb(convexFromTrans, minAABB1, maxAABB1);
            castShape.getAabb(convexToTrans, minAABB2, maxAABB2);
            minAABB1.x = Math.min(minAABB1.x, minAABB2.x);
            minAABB1.y = Math.min(minAABB1.y, minAABB2.y);
            minAABB1.z = Math.min(minAABB1.z, minAABB2.z);
            maxAABB1.x = Math.max(maxAABB1.x, maxAABB2.x);
            maxAABB1.y = Math.max(maxAABB1.y, maxAABB2.y);
            maxAABB1.z = Math.max(maxAABB1.z, maxAABB2.z);
            Region3i region = Region3i.createFromMinMax(new Vector3i(minAABB1, 0.5f), new Vector3i(maxAABB1, 0.5f));
            Matrix3f rot = new Matrix3f();
            rot.setIdentity();

            for (Vector3i pos : region) {
                if (worldShape.getWorld().getBlock(pos).isPenetrable())
                    continue;

                Transform childTrans = new Transform(new Matrix4f(rot, pos.toVector3f(), 1.0f));
                CollisionShape childCollisionShape = defaultBox;
                // replace collision shape so that callback can determine the triangle
                CollisionShape saveCollisionShape = collisionObject.getCollisionShape();
                collisionObject.internalSetTemporaryCollisionShape(childCollisionShape);
                objectQuerySingle(castShape, convexFromTrans, convexToTrans,
                        collisionObject,
                        childCollisionShape,
                        childTrans,
                        resultCallback, allowedPenetration);
                // restore
                collisionObject.internalSetTemporaryCollisionShape(saveCollisionShape);
            }
        } else {
            objectQuerySingle(castShape, convexFromTrans, convexToTrans, collisionObject, collisionShape, colObjWorldTransform, resultCallback, allowedPenetration);
        }
    }
}
