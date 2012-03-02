package org.terasology.components;

import org.terasology.entitySystem.Component;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class CharacterMovementComponent implements Component {

    // Speed settings
    // TODO: consider making these floats
    public float maxGroundSpeed = 5.0f;
    public float maxWaterSpeed = 2.0f;
    public float walkSpeed = 1.5f;
    public float runFactor = 1.5f;
    public float jumpSpeed = 10.0f;

    public boolean isSwimming = false;
    public boolean isGrounded = false;
    public boolean isRunning = false;

    public Vector3f velocity = new Vector3f();

    // Movement inputs - desired direction, etc
    public boolean jump = false;
    // The direction and strength of movement desired
    // Should have a length between 0 and 1
    public Vector3f drive = new Vector3f();
    public boolean faceMovementDirection = false;
    

    public void store(StorageWriter writer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void retrieve(StorageReader reader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
