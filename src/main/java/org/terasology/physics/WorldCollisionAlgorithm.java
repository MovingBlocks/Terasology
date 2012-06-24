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

import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.broadphase.CollisionAlgorithm;
import com.bulletphysics.collision.broadphase.CollisionAlgorithmConstructionInfo;
import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.dispatch.CollisionAlgorithmCreateFunc;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.TransformUtil;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.util.ObjectPool;
import com.google.common.collect.Maps;
import cz.advel.stack.Stack;
import org.terasology.logic.world.WorldProvider;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.performanceMonitor.PerformanceMonitor;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius
 */
public class WorldCollisionAlgorithm extends CollisionAlgorithm {

    private static Logger logger = Logger.getLogger(WorldCollisionAlgorithm.class.getName());

    private Map<Vector3i, CollisionAlgorithm> collisionAlgMap = Maps.newHashMap();
    private boolean isSwapped;
    // TODO: Temporary, blocks should store their shape(s)
    private BoxShape defaultBox;

    public void init(CollisionAlgorithmConstructionInfo ci, CollisionObject body0, CollisionObject body1, boolean isSwapped) {
        super.init(ci);

        this.isSwapped = isSwapped;

        defaultBox = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
    }

    @Override
    public void destroy() {
        for (CollisionAlgorithm alg : collisionAlgMap.values()) {
            dispatcher.freeCollisionAlgorithm(alg);
        }
        collisionAlgMap.clear();
    }

    @Override
    public void processCollision(CollisionObject body0, CollisionObject body1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
        PerformanceMonitor.startActivity("World Process Collision");
        CollisionObject colObj = isSwapped ? body1 : body0;
        CollisionObject otherObj = isSwapped ? body0 : body1;
        assert (colObj.getCollisionShape().getShapeType() == BroadphaseNativeType.INVALID_SHAPE_PROXYTYPE);

        WorldShape worldShape = (WorldShape) colObj.getCollisionShape();

        Transform otherObjTransform = new Transform();
        otherObj.getWorldTransform(otherObjTransform);
        Vector3f aabbMin = Stack.alloc(Vector3f.class);
        Vector3f aabbMax = Stack.alloc(Vector3f.class);
        otherObj.getCollisionShape().getAabb(otherObjTransform, aabbMin, aabbMax);
        Matrix4f otherObjMatrix = new Matrix4f();
        otherObjTransform.getMatrix(otherObjMatrix);
        Vector3f otherObjPos = new Vector3f();
        otherObjMatrix.get(otherObjPos);

        Region3i region = Region3i.createFromMinMax(new Vector3i(aabbMin, 0.f), new Vector3i(aabbMax, 1.f));

        Transform orgTrans = Stack.alloc(Transform.class);
        colObj.getWorldTransform(orgTrans);

        Transform newChildWorldTrans = Stack.alloc(Transform.class);
        Matrix4f childMat = Stack.alloc(Matrix4f.class);

        Matrix3f rot = Stack.alloc(Matrix3f.class);
        rot.setIdentity();

        for (Vector3i blockPos : region) {
            Block block = worldShape.getWorld().getBlock(blockPos);
            if (block.isPenetrable()) {
                CollisionAlgorithm alg = collisionAlgMap.remove(blockPos);
                if (alg != null) {
                    dispatcher.freeCollisionAlgorithm(alg);
                }
            } else {

                // TODO: use each shape within the block.
                CollisionShape childShape = defaultBox;
                colObj.internalSetTemporaryCollisionShape(childShape);

                CollisionAlgorithm alg = collisionAlgMap.get(blockPos);
                if (alg == null) {
                    alg = dispatcher.findAlgorithm(colObj, otherObj);
                    collisionAlgMap.put(blockPos, alg);
                }

                childMat.set(rot, blockPos.toVector3f(), 1.0f);
                newChildWorldTrans.set(childMat);
                colObj.setWorldTransform(newChildWorldTrans);
                colObj.setInterpolationWorldTransform(newChildWorldTrans);
                colObj.setUserPointer(blockPos);

                alg.processCollision(colObj, otherObj, dispatchInfo, resultOut);
            }
        }
        colObj.internalSetTemporaryCollisionShape(worldShape);
        colObj.setWorldTransform(orgTrans);
        colObj.setInterpolationWorldTransform(orgTrans);
        PerformanceMonitor.endActivity();
    }

    @Override
    public float calculateTimeOfImpact(CollisionObject body0, CollisionObject body1, DispatcherInfo dispatchInfo, ManifoldResult resultOut) {
        /*PerformanceMonitor.startActivity("World Calculate Time Of Impact");
        CollisionObject colObj = isSwapped ? body1 : body0;
        CollisionObject otherObj = isSwapped ? body0 : body1;

        assert (colObj.getCollisionShape().getShapeType() == BroadphaseNativeType.INVALID_SHAPE_PROXYTYPE);

        WorldShape worldShape = (WorldShape) colObj.getCollisionShape();

        Transform otherObjTransform = new Transform();
        Vector3f otherLinearVelocity = new Vector3f();
        Vector3f otherAngularVelocity = new Vector3f();
        otherObj.getInterpolationWorldTransform(otherObjTransform);
        otherObj.getInterpolationLinearVelocity(otherLinearVelocity);
        otherObj.getInterpolationAngularVelocity(otherAngularVelocity);
        Vector3f aabbMin = Stack.alloc(Vector3f.class);
        Vector3f aabbMax = Stack.alloc(Vector3f.class);
        otherObj.getCollisionShape().getAabb(otherObjTransform, aabbMin, aabbMax);

        Region3i region = Region3i.createFromMinMax(new Vector3i(aabbMin, 0.5f), new Vector3i(aabbMax, 0.5f));

        Transform orgTrans = Stack.alloc(Transform.class);
        Transform childTrans = Stack.alloc(Transform.class);
        float hitFraction = 1f;

        Matrix3f rot = new Matrix3f();
        rot.setIdentity();

        for (Vector3i blockPos : region) {
            Block block = worldShape.getWorld().getBlock(blockPos);
            if (block.isPenetrable()) continue;

            // recurse, using each shape within the block.
            CollisionShape childShape = defaultBox;

            // backup
            colObj.getWorldTransform(orgTrans);

            childTrans.set(new Matrix4f(rot, blockPos.toVector3f(), 1.0f));
            colObj.setWorldTransform(childTrans);

            // the contactpoint is still projected back using the original inverted worldtrans
            CollisionShape tmpShape = colObj.getCollisionShape();
            colObj.internalSetTemporaryCollisionShape(childShape);
            colObj.setUserPointer(blockPos);

            CollisionAlgorithm collisionAlg = collisionAlgorithmFactory.dispatcher1.findAlgorithm(colObj, otherObj);
            usedCollisionAlgorithms.add(collisionAlg);
            float frac = collisionAlg.calculateTimeOfImpact(colObj, otherObj, dispatchInfo, resultOut);
            if (frac < hitFraction) {
                hitFraction = frac;
            }

            // revert back
            colObj.internalSetTemporaryCollisionShape(tmpShape);
            colObj.setWorldTransform(orgTrans);
        }
        PerformanceMonitor.endActivity();
        return hitFraction;        */
        return 1.0f;
    }

    @Override
    public void getAllContactManifolds(ObjectArrayList<PersistentManifold> manifoldArray) {
        for (CollisionAlgorithm alg : collisionAlgMap.values()) {
            alg.getAllContactManifolds(manifoldArray);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    public static class CreateFunc extends CollisionAlgorithmCreateFunc {
        private final ObjectPool<WorldCollisionAlgorithm> pool = ObjectPool.get(WorldCollisionAlgorithm.class);

        @Override
        public CollisionAlgorithm createCollisionAlgorithm(CollisionAlgorithmConstructionInfo ci, CollisionObject body0, CollisionObject body1) {
            WorldCollisionAlgorithm algo = pool.get();
            algo.init(ci, body0, body1, false);
            return algo;
        }

        @Override
        public void releaseCollisionAlgorithm(CollisionAlgorithm algo) {
            pool.release((WorldCollisionAlgorithm)algo);
        }
    };

    public static class SwappedCreateFunc extends CollisionAlgorithmCreateFunc {
        private final ObjectPool<WorldCollisionAlgorithm> pool = ObjectPool.get(WorldCollisionAlgorithm.class);

        @Override
        public CollisionAlgorithm createCollisionAlgorithm(CollisionAlgorithmConstructionInfo ci, CollisionObject body0, CollisionObject body1) {
            WorldCollisionAlgorithm algo = pool.get();
            algo.init(ci, body0, body1, true);
            return algo;
        }

        @Override
        public void releaseCollisionAlgorithm(CollisionAlgorithm algo) {
            pool.release((WorldCollisionAlgorithm)algo);
        }
    };

}