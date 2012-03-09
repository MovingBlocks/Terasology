package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class CharacterMovementComponent extends AbstractComponent {

    // Speed settings
    public float maxGroundSpeed = 5.0f;
    public float maxWaterSpeed = 2.0f;
    public float maxGhostSpeed = 5.0f;
    public float runFactor = 1.5f;
    public float jumpSpeed = 10.0f;

    // Determines how easily the play can change direction
    // TODO: Separate player agiliy from environmental friction, and ground from air control
    public float groundFriction = 8.0f;

    public boolean isGhosting = false;
    public boolean isSwimming = false;
    public boolean isGrounded = false;
    public boolean isRunning = false;

    private Vector3f velocity = new Vector3f();

    // Movement inputs - desired direction, etc
    public boolean jump = false;

    // The direction and strength of movement desired
    // Should have a length between 0 and 1
    private Vector3f drive = new Vector3f();
    public boolean faceMovementDirection = false;

    public float distanceBetweenFootsteps = 1f;
    public float footstepDelta = 0.0f;
    
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

}
