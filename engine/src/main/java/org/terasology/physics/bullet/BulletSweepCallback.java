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

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.linearmath.Transform;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import org.terasology.physics.engine.SweepCallback;

/**
 * The bullet implementation of SweepCallback, that holds the results of a collision sweep. (detect what
 * collisions would occur if something moved from a to b)
 *
 */
public class BulletSweepCallback extends CollisionWorld.ClosestConvexResultCallback implements SweepCallback {
    protected CollisionObject me;
    protected final Vector3f up;
    protected float minSlopeDot;

    public BulletSweepCallback(CollisionObject me, org.terasology.math.geom.Vector3f up, float minSlopeDot) {
        super(new Vector3f(), new Vector3f());
        this.me = me;
        this.up = new Vector3f(up.x, up.y, up.z);
        this.minSlopeDot = minSlopeDot;
    }

    @Override
    public float addSingleResult(CollisionWorld.LocalConvexResult convexResult, boolean normalInWorldSpace) {
        if (convexResult.hitCollisionObject == me) {
            return 1.0f;
        }
        return super.addSingleResult(convexResult, normalInWorldSpace);
    }

    @Override
    public float calculateAverageSlope(float originalSlope, float checkingOffset) {
        Vector3f contactPoint = this.hitPointWorld;
        float slope = 1f;
        boolean foundSlope = false;
        Vector3f fromWorld = new Vector3f(contactPoint);
        fromWorld.y += 0.2f;
        Vector3f toWorld = new Vector3f(contactPoint);
        toWorld.y -= 0.2f;
        CollisionWorld.ClosestRayResultCallback rayResult = new CollisionWorld.ClosestRayResultCallback(fromWorld, toWorld);
        Transform from = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), fromWorld, 1.0f));
        Transform to = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), toWorld, 1.0f));
        Transform targetTransform = this.hitCollisionObject.getWorldTransform(new Transform());
        CollisionWorld.rayTestSingle(from, to, this.hitCollisionObject, this.hitCollisionObject.getCollisionShape(), targetTransform, rayResult);
        if (rayResult.hasHit()) {
            foundSlope = true;
            slope = Math.min(slope, rayResult.hitNormalWorld.dot(new Vector3f(0, 1, 0)));
        }
        Vector3f secondTraceOffset = new Vector3f(this.hitNormalWorld);
        secondTraceOffset.y = 0;
        secondTraceOffset.normalize();
        secondTraceOffset.scale(checkingOffset);
        fromWorld.add(secondTraceOffset);
        toWorld.add(secondTraceOffset);
        rayResult = new CollisionWorld.ClosestRayResultCallback(fromWorld, toWorld);
        from = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), fromWorld, 1.0f));
        to = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), toWorld, 1.0f));
        targetTransform = this.hitCollisionObject.getWorldTransform(new Transform());
        CollisionWorld.rayTestSingle(from, to, this.hitCollisionObject, this.hitCollisionObject.getCollisionShape(), targetTransform, rayResult);
        if (rayResult.hasHit()) {
            foundSlope = true;
            slope = Math.min(slope, rayResult.hitNormalWorld.dot(new Vector3f(0, 1, 0)));
        }
        if (!foundSlope) {
            slope = originalSlope;
        }
        return slope;
    }

    @Override
    public org.terasology.math.geom.Vector3f getHitNormalWorld() {
        return new org.terasology.math.geom.Vector3f(hitNormalWorld.x, hitNormalWorld.y, hitNormalWorld.z);
    }

    @Override
    public org.terasology.math.geom.Vector3f getHitPointWorld() {
        return new org.terasology.math.geom.Vector3f(hitPointWorld.x, hitPointWorld.y, hitPointWorld.z);
    }

    @Override
    public float getClosestHitFraction() {
        return closestHitFraction;
    }

    @Override
    public boolean checkForStep(org.terasology.math.geom.Vector3f direction, float stepHeight, float slopeFactor, float checkForwardDistance) {
        boolean moveUpStep;
        boolean hitStep = false;
        float stepSlope = 1f;
        Vector3f lookAheadOffset = new Vector3f(direction.x, direction.y, direction.z);
        lookAheadOffset.y = 0;
        lookAheadOffset.normalize();
        lookAheadOffset.scale(checkForwardDistance);
        Vector3f fromWorld = new Vector3f(hitPointWorld);
        fromWorld.y += stepHeight + 0.05f;
        fromWorld.add(lookAheadOffset);
        Vector3f toWorld = new Vector3f(hitPointWorld);
        toWorld.y -= 0.05f;
        toWorld.add(lookAheadOffset);
        CollisionWorld.ClosestRayResultCallback rayResult = new CollisionWorld.ClosestRayResultCallback(fromWorld, toWorld);
        Transform transformFrom = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), fromWorld, 1.0f));
        Transform transformTo = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), toWorld, 1.0f));
        Transform targetTransform = this.hitCollisionObject.getWorldTransform(new Transform());
        CollisionWorld.rayTestSingle(transformFrom, transformTo, this.hitCollisionObject, this.hitCollisionObject.getCollisionShape(), targetTransform, rayResult);
        if (rayResult.hasHit()) {
            hitStep = true;
            stepSlope = rayResult.hitNormalWorld.dot(new Vector3f(0, 1, 0));
        }
        fromWorld.add(lookAheadOffset);
        toWorld.add(lookAheadOffset);
        rayResult = new CollisionWorld.ClosestRayResultCallback(fromWorld, toWorld);
        transformFrom = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), fromWorld, 1.0f));
        transformTo = new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), toWorld, 1.0f));
        targetTransform = this.hitCollisionObject.getWorldTransform(new Transform());
        CollisionWorld.rayTestSingle(transformFrom, transformTo, this.hitCollisionObject, this.hitCollisionObject.getCollisionShape(), targetTransform, rayResult);
        if (rayResult.hasHit()) {
            hitStep = true;
            stepSlope = Math.min(stepSlope, rayResult.hitNormalWorld.dot(new Vector3f(0, 1, 0)));
        }
        moveUpStep = hitStep && stepSlope >= slopeFactor;
        return moveUpStep;
    }
}
