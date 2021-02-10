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

import com.badlogic.gdx.physics.bullet.collision.ClosestConvexResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.LocalConvexResult;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.terasology.physics.engine.SweepCallback;

/**
 * The bullet implementation of SweepCallback, that holds the results of a collision sweep. (detect what
 * collisions would occur if something moved from a to b)
 *
 */
public class BulletSweepCallback extends ClosestConvexResultCallback implements SweepCallback {
    protected btCollisionObject me;
    protected final Vector3f up;
    protected float minSlopeDot;

    public BulletSweepCallback(btCollisionObject me, Vector3f up, float minSlopeDot) {
        super(new Vector3f(), new Vector3f());
        this.me = me;
        this.up = new Vector3f(up.x, up.y, up.z);
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
        Vector3f contactPoint = this.getHitPointWorld();
        float slope = 1f;
        boolean foundSlope = false;
        Vector3f fromWorld = new Vector3f(contactPoint);
        fromWorld.y += 0.2f;
        Vector3f toWorld = new Vector3f(contactPoint);
        toWorld.y -= 0.2f;
        ClosestRayResultCallback resultCallback = new ClosestRayResultCallback(fromWorld, toWorld);
        Matrix4f from = new Matrix4f().setTranslation(fromWorld);
        Matrix4f to = new Matrix4f().setTranslation(toWorld);
        Matrix4f targetTransform = this.getHitCollisionObject().getWorldTransform();
        btDiscreteDynamicsWorld.rayTestSingle(from, to, this.getHitCollisionObject(), this.getHitCollisionObject().getCollisionShape(), targetTransform, resultCallback);
        if (resultCallback.hasHit()) {
            foundSlope = true;
            Vector3f result = new Vector3f();
            resultCallback.getHitNormalWorld(result);
            slope = Math.min(slope, result.dot(0, 1, 0));
        }
        Vector3f secondTraceOffset = new Vector3f();
        this.getHitNormalWorld(secondTraceOffset);
        secondTraceOffset.y = 0;
        secondTraceOffset.normalize();
        secondTraceOffset.mul(checkingOffset);
        fromWorld.add(secondTraceOffset);
        toWorld.add(secondTraceOffset);
        resultCallback = new ClosestRayResultCallback(fromWorld, toWorld);
        from = new Matrix4f().setTranslation(fromWorld);
        to = new Matrix4f().setTranslation(toWorld);
        targetTransform = this.getHitCollisionObject().getWorldTransform();
        btDiscreteDynamicsWorld.rayTestSingle(from, to, this.getHitCollisionObject(), this.getHitCollisionObject().getCollisionShape(), targetTransform, resultCallback);
        if (resultCallback.hasHit()) {
            foundSlope = true;
            Vector3f hitNormal = new Vector3f();
            resultCallback.getHitNormalWorld(hitNormal);
            slope = Math.min(slope, hitNormal.dot(0, 1, 0));
        }
        if (!foundSlope) {
            slope = originalSlope;
        }
        resultCallback.dispose();
        return slope;
    }

    @Override
    public Vector3f getHitNormalWorld() {
        Vector3f result = new Vector3f();
        this.getHitNormalWorld(result);
        return result;
    }

    @Override
    public Vector3f getHitPointWorld() {
        Vector3f result = new Vector3f();
        this.getHitPointWorld(result);
        return result;
    }

    @Override
    public boolean checkForStep(Vector3f direction, float stepHeight, float slopeFactor, float checkForwardDistance) {
        boolean moveUpStep;
        boolean hitStep = false;
        float stepSlope = 1f;
        Vector3f lookAheadOffset = new Vector3f(direction.x, direction.y, direction.z);
        lookAheadOffset.y = 0;
        lookAheadOffset.normalize();
        lookAheadOffset.mul(checkForwardDistance);
        Vector3f fromWorld = new Vector3f();
        this.getHitPointWorld(fromWorld);

        fromWorld.y += stepHeight + 0.05f;
        fromWorld.add(lookAheadOffset);
        Vector3f toWorld = new Vector3f();
        this.getHitPointWorld(toWorld);
        toWorld.y -= 0.05f;
        toWorld.add(lookAheadOffset);
        ClosestRayResultCallback rayResult = new ClosestRayResultCallback(fromWorld, toWorld);
        Matrix4f transformFrom = new Matrix4f().setTranslation(fromWorld);
        Matrix4f transformTo = new Matrix4f().setTranslation(toWorld);
        Matrix4f targetTransform = this.getHitCollisionObject().getWorldTransform();
        btDiscreteDynamicsWorld.rayTestSingle(transformFrom, transformTo, this.getHitCollisionObject(), this.getHitCollisionObject().getCollisionShape(), targetTransform, rayResult);
        if (rayResult.hasHit()) {
            hitStep = true;
            Vector3f hitNormal = new Vector3f();
            rayResult.getHitNormalWorld(hitNormal);
            stepSlope = hitNormal.dot(0, 1, 0);
        }
        fromWorld.add(lookAheadOffset);
        toWorld.add(lookAheadOffset);
        rayResult = new ClosestRayResultCallback(fromWorld, toWorld);
        transformFrom = new Matrix4f().setTranslation(fromWorld);
        transformTo = new Matrix4f().setTranslation(toWorld);
        targetTransform = this.getHitCollisionObject().getWorldTransform();
        btDiscreteDynamicsWorld.rayTestSingle(transformFrom, transformTo, this.getHitCollisionObject(), this.getHitCollisionObject().getCollisionShape(), targetTransform, rayResult);
        if (rayResult.hasHit()) {
            hitStep = true;
            Vector3f hitNormal = new Vector3f();
            rayResult.getHitNormalWorld(hitNormal);
            stepSlope = Math.min(stepSlope, hitNormal.dot(0, 1, 0));
        }
        moveUpStep = hitStep && stepSlope >= slopeFactor;
        return moveUpStep;
    }
}
