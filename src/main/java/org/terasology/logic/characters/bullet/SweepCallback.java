package org.terasology.logic.characters.bullet;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import javax.vecmath.Vector3f;

/**
 * A SweepCallback holds the results of a collision sweep. 
 * (detect what collisions would occur if something moved from a to b)
 * @author Immortius
 */
public class SweepCallback extends CollisionWorld.ClosestConvexResultCallback {
    protected CollisionObject me;
    protected final Vector3f up;
    protected float minSlopeDot;

    public SweepCallback(CollisionObject me, final Vector3f up, float minSlopeDot) {
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
    
}
