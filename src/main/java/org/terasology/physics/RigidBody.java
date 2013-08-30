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

package org.terasology.physics;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * This interface provides a simplified an generic view on rigid bodies of physics engines. This may is not a direct implementation, so the
 * behaviour and names of method may differ from what is used in specific physics implementations. Read the documentation of individual
 * method when uncertain.
 * <p/>
 * After removing this body from the physics engine (by using rigidBody methods of the physics engine) this object is no longer valid and
 * should not be used anymore.
 *
 * TODO: add the methods to apply forces
 *
 * @author Xanhou
 */
public interface RigidBody {

    /**
     * Applies an impulse to this rigid body. The impulse is applied to the
     * centre of mass. Impulse is stored as reference, not by value.
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
    
    /**
     * Sets the linear velocity
     * @param lin_vel new linear velocity
     */
    public void setLinearVelocity(Vector3f lin_vel);
    
    /**
     * Sets the angular velocity
     * @param lin_vel new angular velocity
     */
    public void setAngularVelocity(Vector3f ang_vel);
    
    /**
     * Sets both linear and angular velocity in a slightly more efficient way than calling both the individual methods.
     * @param lin_vel new linear velocity
     * @param ang_vel new angular velocity
     */
    public void setVelocity(Vector3f lin_vel, Vector3f ang_vel);

    /**
     * Sets the orientation or rotation.
     * @param orientation the new rotation.
     */
    public void setOrientation(Quat4f orientation);

    /**
     * Sets the world location or position.
     * @param location new world position
     */
    public void setLocation(Vector3f location);
    
    /**
     * Sets both rotation and position in a more efficient way than calling both the individual methods.
     * A transform is simply an expensive word for the combination of the location and orientation of an object.
     * @param location
     * @param orientation 
     */
    public void setTransform(Vector3f location, Quat4f orientation);
    
    /**
     * Active means that the entity is not sleeping, or requesting to sleep.
     * @return True if this entity is active, false otherwise.
     */
    public boolean isActive();
}
