package org.terasology.components;

import org.terasology.entitySystem.Component;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class SimpleAIComponent implements Component {

    public long lastChangeOfDirectionAt = 0;
    public Vector3f movementTarget = new Vector3f();
    public boolean followingPlayer = false;

}
