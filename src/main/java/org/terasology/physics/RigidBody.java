package org.terasology.physics;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * This interface provides a simplified an generic view on rigid bodies of
 * physics engines. Note that this is no longer a Bullet rigid body.
 * <p/>
 * After removing this body from the physics engine (by using rigidBody methods
 * of the physics engine) this object is no longer valid and should not be used
 * anymore.
 * 
 * TODO: add the methods to apply forces
 *
 * @author Xanhou
 */
public interface RigidBody {

    /**
     * Applies an impulse to this rigid body. The impulse is applied to the
     * centre of mass. The implementation of this method is allowed to assume
     * the impulse is not changed after being given to this method as parameter.
     *
     * @param impulse the impulse to apply.
     */
    public void applyImpulse(Vector3f impulse);

    /**
     * Changes to location of this rigid body by the given translation. Note
     * that velocities and orientation remain the same.
     *
     * @param translation the translation to apply.
     */
    public void translate(Vector3f translation);

    /**
     * Returns the orientation of this body.
     *
     * @param out output parameter to put the orientation in.
     * @return out, for easy of use.
     */
    public Quat4f getOrientation(Quat4f out);

    /**
     * Returns the non interpolated location of this body. Note that the
     * location of en entity should also be available from its
     * LocationComponent. However, the location component contains the
     * interpolated location. This getter retrieves the non interpolated
     * location as calculated in the last simulation step.
     *
     * @param out output parameter to put the location in.
     * @return out, for easy of use.
     */
    public Vector3f getLocation(Vector3f out);

    /**
     * Returns the linear velocity of this body. The linear velocity alters the
     * position of the body each time step.
     *
     * @param out output parameter to put the location in.
     * @return out, for easy of use.
     */
    public Vector3f getLinearVelocity(Vector3f out);

    /**
     * Returns the angular velocity of this body. The angular velocity alters
     * the orientation of this body each time step.
     *
     * @param out output parameter to put the location in.
     * @return out, for easy of use.
     */
    public Vector3f getAngularVelocity(Vector3f out);
    
    public void setLinearVelocity(Vector3f lin_vel);
    
    public void setAngularVelocity(Vector3f ang_vel);
    
    public void setVelocity(Vector3f lin_vel, Vector3f ang_vel);

    public void setOrientation(Quat4f orientation);

    public void setLocation(Vector3f location);
    
    public void setTransform(Vector3f location, Quat4f orientation);
}
