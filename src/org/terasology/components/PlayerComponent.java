package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;

import javax.vecmath.Vector3f;

/**
 * Player information that is shared across the network
 * @author Immortius <immortius@gmail.com>
 */
public final class PlayerComponent extends AbstractComponent {
    public Vector3f spawnPosition = new Vector3f();
}
