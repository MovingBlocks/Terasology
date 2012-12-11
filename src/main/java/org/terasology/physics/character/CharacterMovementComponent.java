/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.physics.character;

import java.util.List;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.Component;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.StandardCollisionGroup;

import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.google.common.collect.Lists;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class CharacterMovementComponent implements Component {

    // Collision settings
    public float height = 1.6f;
    public float radius = 0.3f;
    public CollisionGroup collisionGroup = StandardCollisionGroup.CHARACTER;
    public List<CollisionGroup> collidesWith = Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.WORLD, StandardCollisionGroup.SENSOR);

    // Speed settings
    public float maxGroundSpeed = 5.0f;
    public float maxWaterSpeed = 2.0f;
    public float maxGhostSpeed = 5.0f;
    public float runFactor = 1.5f;
    public float jumpSpeed = 10.0f;

    // Movement settings
    public float stepHeight = 0.35f;
    public float slopeFactor = 0.6f; // Cosine of the maximum slope traversable. 1 is no slope, 0 is any slope

    // Determines how easily the play can change direction
    // TODO: Separate player agiliy from environmental friction, and ground from air control
    public float groundFriction = 8.0f;
    public float distanceBetweenFootsteps = 1f;
    public boolean faceMovementDirection = false;

    // Current movement mode
    // TODO: Use enum?
    public boolean isGhosting = false;
    /**
	 * @return the isGhosting
	 */
	public boolean isGhosting() {
		return isGhosting;
	}

	/**
	 * @param isGhosting the isGhosting to set
	 */
	public void setGhosting(boolean isGhosting) {
		this.isGhosting = isGhosting;
	}

	public boolean isSwimming = false;
    public boolean isGrounded = false;
    public boolean isRunning = false;

    private Vector3f velocity = new Vector3f();

    // Movement inputs - desired direction, etc
    public boolean jump = false;

    // The direction and strength of movement desired
    // Should have a length between 0 and 1
    private Vector3f drive = new Vector3f();

    // Distance since last footstep
    public float footstepDelta = 0.0f;

    public transient PairCachingGhostObject collider;

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f newVelocity) {
        velocity.set(newVelocity);
    }

    public Vector3f getDrive() {
        return drive;
    }

    public void setDrive(Vector3f newDrive) {
        drive.set(newDrive);
    }

	/**
	 * @return the height
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(float height) {
		this.height = height;
	}

	/**
	 * @return the radius
	 */
	public float getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(float radius) {
		this.radius = radius;
	}

	/**
	 * @return the maxGroundSpeed
	 */
	public float getMaxGroundSpeed() {
		return maxGroundSpeed;
	}

	/**
	 * @param maxGroundSpeed the maxGroundSpeed to set
	 */
	public void setMaxGroundSpeed(float maxGroundSpeed) {
		this.maxGroundSpeed = maxGroundSpeed;
	}

	/**
	 * @return the maxWaterSpeed
	 */
	public float getMaxWaterSpeed() {
		return maxWaterSpeed;
	}

	/**
	 * @param maxWaterSpeed the maxWaterSpeed to set
	 */
	public void setMaxWaterSpeed(float maxWaterSpeed) {
		this.maxWaterSpeed = maxWaterSpeed;
	}

	/**
	 * @return the maxGhostSpeed
	 */
	public float getMaxGhostSpeed() {
		return maxGhostSpeed;
	}

	/**
	 * @param maxGhostSpeed the maxGhostSpeed to set
	 */
	public void setMaxGhostSpeed(float maxGhostSpeed) {
		this.maxGhostSpeed = maxGhostSpeed;
	}

	/**
	 * @return the runFactor
	 */
	public float getRunFactor() {
		return runFactor;
	}

	/**
	 * @param runFactor the runFactor to set
	 */
	public void setRunFactor(float runFactor) {
		this.runFactor = runFactor;
	}

	/**
	 * @return the jumpSpeed
	 */
	public float getJumpSpeed() {
		return jumpSpeed;
	}

	/**
	 * @param jumpSpeed the jumpSpeed to set
	 */
	public void setJumpSpeed(float jumpSpeed) {
		this.jumpSpeed = jumpSpeed;
	}

	/**
	 * @return the stepHeight
	 */
	public float getStepHeight() {
		return stepHeight;
	}

	/**
	 * @param stepHeight the stepHeight to set
	 */
	public void setStepHeight(float stepHeight) {
		this.stepHeight = stepHeight;
	}

	/**
	 * @return the slopeFactor
	 */
	public float getSlopeFactor() {
		return slopeFactor;
	}

	/**
	 * @param slopeFactor the slopeFactor to set
	 */
	public void setSlopeFactor(float slopeFactor) {
		this.slopeFactor = slopeFactor;
	}

	/**
	 * @return the groundFriction
	 */
	public float getGroundFriction() {
		return groundFriction;
	}

	/**
	 * @param groundFriction the groundFriction to set
	 */
	public void setGroundFriction(float groundFriction) {
		this.groundFriction = groundFriction;
	}

	/**
	 * @return the distanceBetweenFootsteps
	 */
	public float getDistanceBetweenFootsteps() {
		return distanceBetweenFootsteps;
	}

	/**
	 * @param distanceBetweenFootsteps the distanceBetweenFootsteps to set
	 */
	public void setDistanceBetweenFootsteps(float distanceBetweenFootsteps) {
		this.distanceBetweenFootsteps = distanceBetweenFootsteps;
	}

}
