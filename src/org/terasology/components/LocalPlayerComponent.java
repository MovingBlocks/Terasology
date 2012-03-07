package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.Component;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LocalPlayerComponent extends AbstractComponent {

    // These aren't really needed - yaw would be applied the LocationComponent and
    // pitch would be applied to a the Camera's location component (which would parent
    // off of this entity). However not up to doing the camera stuff yet and taking
    // an easy route out for the moment re: applying yaw.
    // TODO: Don't take an easy route out
    public float yaw = 0;
    public float pitch = 0;
    public boolean isDead = false;

}
