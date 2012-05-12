package org.terasology.components;

import org.terasology.entitySystem.Component;

import javax.vecmath.Vector3f;

/**
 * @author Overdhose
 * copied from SimpleAIComponent, only movementtarget is really used
 */
public final class SimpleMinionAIComponent implements Component {

    public long lastChangeOfDirectionAt = 0;
    public Vector3f movementTarget = new Vector3f();
    public boolean followingPlayer = true;

}
