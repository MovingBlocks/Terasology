/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.physics.bullet;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestConvexResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.LocalConvexResult;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import org.terasology.math.VecMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.engine.SweepCallback;


/**
 * The bullet implementation of SweepCallback, that holds the results of a collision sweep. (detect what
 * collisions would occur if something moved from a to b)
 *
 */
public class BulletSweepCallback extends ClosestConvexResultCallback implements SweepCallback {
    protected btCollisionObject me;
    protected final Vector3 up;
    protected float minSlopeDot;

    public BulletSweepCallback(btCollisionObject me, Vector3f up, float minSlopeDot) {
        super(Vector3.Zero,Vector3.Zero);
        this.me = me;
        this.up = new Vector3(up.x, up.y, up.z);
        this.minSlopeDot = minSlopeDot;
    }

    @Override
    public float addSingleResult(LocalConvexResult convexResult, boolean normalInWorldSpace) {
        if (convexResult.getHitCollisionObject() == me) {
            return 1.0f;
        }
        return super.addSingleResult(convexResult, normalInWorldSpace);
    }

    @Override
    public float calculateAverageSlope(float originalSlope, float checkingOffset) {
        Vector3 contactPoint = Vector3.Zero;
        this.getHitPointWorld(contactPoint);
        float slope = 1f;
        boolean foundSlope = false;
        Vector3 fromWorld = new Vector3(contactPoint);
        fromWorld.y += 0.2f;
        Vector3 toWorld = new Vector3(contactPoint);
        toWorld.y -= 0.2f;
        ClosestRayResultCallback rayResult = new ClosestRayResultCallback(fromWorld,toWorld);

        Matrix4 from = new Matrix4(fromWorld,new Quaternion(0, 0, 0, 1), new Vector3(1,1,1));
        Matrix4 to = new Matrix4(toWorld,new Quaternion(0, 0, 0, 1), new Vector3(1,1,1));
        Matrix4 targetTransform = this.getHitCollisionObject().getWorldTransform();
        btDiscreteDynamicsWorld.rayTestSingle(from,to,this.getHitCollisionObject(),this.getHitCollisionObject().getCollisionShape(),targetTransform,rayResult);
        if (rayResult.hasHit()) {
            foundSlope = true;
            Vector3 result = new Vector3();
            rayResult.getHitNormalWorld(result);
            slope = Math.min(slope,result.dot(Vector3.Y));
        }

        Vector3 secondTraceOffset = new Vector3();
        this.getHitNormalWorld(secondTraceOffset);
        secondTraceOffset.y = 0;
        secondTraceOffset.nor();
        secondTraceOffset.scl(checkingOffset);
        fromWorld.add(secondTraceOffset);
        toWorld.add(secondTraceOffset);
        rayResult = new ClosestRayResultCallback(fromWorld, toWorld);
        from = new Matrix4(fromWorld,new Quaternion(0, 0, 0, 1), new Vector3(1,1,1));
        to = new Matrix4(toWorld,new Quaternion(0, 0, 0, 1), new Vector3(1,1,1));
        targetTransform = this.getHitCollisionObject().getWorldTransform();
        btDiscreteDynamicsWorld.rayTestSingle(from, to, this.getHitCollisionObject(), this.getHitCollisionObject().getCollisionShape(), targetTransform, rayResult);
        if (rayResult.hasHit()) {
            foundSlope = true;
            Vector3 hitNormal = Vector3.Zero;
            rayResult.getHitNormalWorld(hitNormal);
            slope = Math.min(slope, hitNormal.dot(Vector3.Y));
        }
        if (!foundSlope) {
            slope = originalSlope;
        }
        return slope;
    }

    @Override
    public Vector3f getHitNormalWorld() {
        Vector3 result = Vector3.Zero;
        this.getHitNormalWorld(result);
        return VecMath.from(result);
    }

    @Override
    public Vector3f getHitPointWorld() {
        Vector3 result = Vector3.Zero;
        this.getHitPointWorld(result);
        return VecMath.from(result);
    }


    @Override
    public boolean checkForStep(org.terasology.math.geom.Vector3f direction, float stepHeight, float slopeFactor, float checkForwardDistance) {
        boolean moveUpStep;
        boolean hitStep = false;
        float stepSlope = 1f;
        Vector3 lookAheadOffset = new Vector3(direction.x, direction.y, direction.z);
        lookAheadOffset.y = 0;
        lookAheadOffset.nor();
        lookAheadOffset.scl(checkForwardDistance);
        Vector3 fromWorld = Vector3.Zero;
        this.getHitPointWorld(fromWorld);

        fromWorld.y += stepHeight + 0.05f;
        fromWorld.add(lookAheadOffset);
        Vector3 toWorld = Vector3.Zero;
        this.getHitPointWorld(toWorld);
        toWorld.y -= 0.05f;
        toWorld.add(lookAheadOffset);
        ClosestRayResultCallback rayResult = new ClosestRayResultCallback(fromWorld, toWorld);
        Matrix4 transformFrom = new Matrix4(fromWorld,new Quaternion(0, 0, 0, 1), new Vector3(1,1,1));
        Matrix4 transformTo = new Matrix4(toWorld, new Quaternion(0, 0, 0, 1), new Vector3(1,1,1));
        Matrix4 targetTransform = this.getHitCollisionObject().getWorldTransform();
        btDiscreteDynamicsWorld.rayTestSingle(transformFrom, transformTo, this.getHitCollisionObject(), this.getHitCollisionObject().getCollisionShape(), targetTransform, rayResult);
        if (rayResult.hasHit()) {
            hitStep = true;
            Vector3 hitNormal = Vector3.Zero;
            rayResult.getHitNormalWorld(hitNormal);
            stepSlope = hitNormal.dot(new Vector3(0, 1, 0));
        }
        fromWorld.add(lookAheadOffset);
        toWorld.add(lookAheadOffset);
        rayResult = new ClosestRayResultCallback(fromWorld, toWorld);
        transformFrom = new Matrix4(fromWorld,new Quaternion(0, 0, 0, 1), new Vector3(1,1,1));
        transformTo = new Matrix4(toWorld,new Quaternion(0, 0, 0, 1), new Vector3(1,1,1));
        targetTransform = this.getHitCollisionObject().getWorldTransform();
        btDiscreteDynamicsWorld.rayTestSingle(transformFrom, transformTo, this.getHitCollisionObject(), this.getHitCollisionObject().getCollisionShape(), targetTransform, rayResult);
        if (rayResult.hasHit()) {
            hitStep = true;
            Vector3 hitNormal = Vector3.Zero;
            rayResult.getHitNormalWorld(hitNormal);
            stepSlope = Math.min(stepSlope, hitNormal.dot(new Vector3(0, 1, 0)));
        }
        moveUpStep = hitStep && stepSlope >= slopeFactor;
        return moveUpStep;
    }
}
