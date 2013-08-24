package org.terasology.physics.bullet;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.linearmath.Transform;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import org.terasology.physics.SweepCallback;

/**
 * A SweepCallback holds the results of a collision sweep. 
 * (detect what collisions would occur if something moved from a to b)
 * 
 * Note that hasHit() is implemented by ClosestConvexResultCallback, and is
 * required by SweepCallbackInterface.
 * @author Immortius
 */
public class BulletSweepCallback extends CollisionWorld.ClosestConvexResultCallback implements SweepCallback {
    protected CollisionObject me;
    protected final Vector3f up;
    protected float minSlopeDot;

    public BulletSweepCallback(CollisionObject me, final Vector3f up, float minSlopeDot) {
        super(new Vector3f(), new Vector3f());
        this.me = me;
        this.up = up;
        this.minSlopeDot = minSlopeDot;
    }

    @Override
    public float addSingleResult(CollisionWorld.LocalConvexResult convexResult, boolean normalInWorldSpace) {
        if (convexResult.hitCollisionObject == me) {
            return 1.0f;
        }
        return super.addSingleResult(convexResult, normalInWorldSpace);
    }
    
    /**
     * Given a SweepCallback, this method check for a safer slope to go with.
     * The main issue this is is used for is the voxel world. Each voxel is a
     * cube for the physics engine. If a player jumps between those voxels, you
     * get a weird slope, even though the player is moving on a flat area. This
     * method therefore also checks the sides.<bk>
     *
     * @param checkingOffset How far to change position for the different results.
     * @param originalSlope If no different slope can be found, this value is
     * returned, making this method always return a decent number.
     * @return a safer slope to use for character movement calculations.
     */
    @Override
    public float calculateSafeSlope(float originalSlope, float checkingOffset) {
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
    public Vector3f getHitNormalWorld() {
        return hitNormalWorld;
    }

    @Override
    public Vector3f getHitPointWorld() {
        return hitPointWorld;
    }

    @Override
    public float getClosestHitFraction() {
        return closestHitFraction;
    }
    
    @Override
    public boolean checkForStep(Vector3f direction, float stepHeight, float slopeFactor, float checkForwardDistance) {
        boolean moveUpStep;
        boolean hitStep = false;
        float stepSlope = 1f;
        Vector3f lookAheadOffset = new Vector3f(direction);
        lookAheadOffset.y = 0;
        lookAheadOffset.normalize();
        lookAheadOffset.scale(checkForwardDistance);
        Vector3f fromWorld = new Vector3f(this.getHitPointWorld());
        fromWorld.y += stepHeight + 0.05f;
        fromWorld.add(lookAheadOffset);
        Vector3f toWorld = new Vector3f(this.getHitPointWorld());
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
