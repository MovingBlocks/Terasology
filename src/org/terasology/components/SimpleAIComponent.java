package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class SimpleAIComponent extends AbstractComponent {

    public long lastChangeOfDirectionAt = 0;
    public Vector3f movementTarget = new Vector3f();
    public boolean followingPlayer = false;

}
