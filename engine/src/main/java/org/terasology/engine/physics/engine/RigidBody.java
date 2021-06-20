// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.engine;


import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * A rigid body is a physics object whose movement and location is controlled by the physics engine.
 * Rigid bodies move under gravity and bounce off each other and the world.
 * <br><br>
 * After being removed from the physics engine this object is no longer valid and should not be used anymore.
 * <br><br>
 * TODO: add the methods to apply forces
 *
 */
public interface RigidBody {

    /**
     * Applies an impulse to this rigid body. The impulse is applied to the centre of mass. Impulse is stored as
     * reference, not by value.
     *
     * @param impulse the impulse to apply.
     */
    void applyImpulse(Vector3f impulse);

    /**
     * Applies an force to this rigid body. The force is applied to the centre of mass. Force is stored as reference,
     * not by value.
     *
     * @param force the force to apply.
     */
    void applyForce(Vector3f force);

    /**
     * Changes to location of this rigid body by the given translation. Note that velocities and orientation remain the
     * same.
     *
     * @param translation the translation to apply.
     */
    void translate(Vector3f translation);

    /**
     * Returns the orientation of this body.
     *
     * @param out output parameter to put the orientation in.
     * @return out, for easy of use.
     */
    Quaternionf getOrientation(Quaternionf out);

    /**
     * Returns the non interpolated location of this body. Note that the location of en entity should also be available
     * from its LocationComponent. However, the location component contains the interpolated location. This getter
     * retrieves the non interpolated location as calculated in the last simulation step.
     *
     * @param out output parameter to put the location in.
     * @return out, for easy of use.
     */
    Vector3f getLocation(Vector3f out);

    /**
     * Returns the linear velocity of this body. The linear velocity alters the position of the body each time step.
     *
     * @param out output parameter to put the location in.
     * @return out, for easy of use.
     */
    Vector3f getLinearVelocity(Vector3f out);

    /**
     * Returns the angular velocity of this body. The angular velocity alters the orientation of this body each time
     * step.
     *
     * @param out output parameter to put the location in.
     * @return out, for easy of use.
     */
    Vector3f getAngularVelocity(Vector3f out);

    /**
     * Sets the linear velocity
     *
     * @param value new linear velocity
     */
    void setLinearVelocity(Vector3f value);

    /**
     * Sets the angular velocity
     *
     * @param value new angular velocity
     */
    void setAngularVelocity(Vector3f value);

    /**
     * Sets both linear and angular velocity in a slightly more efficient way than calling both the individual methods.
     *
     * @param linear new linear velocity
     * @param angular new angular velocity
     */
    void setVelocity(Vector3f linear, Vector3f angular);

    /**
     * Sets the orientation or rotation.
     *
     * @param orientation the new rotation.
     */
    void setOrientation(Quaternionf orientation);

    Matrix4f getWorldTransform();

    Matrix4f setWorldTransform(Matrix4f trans);

    /**
     * Sets the world location or position.
     *
     * @param location new world position
     */
    void setLocation(Vector3f location);

    /**
     * Sets both rotation and position in a more efficient way than calling both the individual methods. A transform is
     * simply an expensive word for the combination of the location and orientation of an object.
     *
     * @param location
     * @param orientation
     */
    void setTransform(Vector3f location, Quaternionf orientation);

    /**
     * Active means that the entity is not sleeping, or requesting to sleep.
     *
     * @return True if this entity is active, false otherwise.
     */
    boolean isActive();
}
