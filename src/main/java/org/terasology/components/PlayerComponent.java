package org.terasology.components;

import org.terasology.entitySystem.Component;

import javax.vecmath.Vector3f;

/**
 * Player information that is shared across the network
 * @author Immortius <immortius@gmail.com>
 */
public final class PlayerComponent implements Component {
    public Vector3f spawnPosition = new Vector3f();
}
