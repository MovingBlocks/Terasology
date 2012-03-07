package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.Component;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlayerComponent extends AbstractComponent {
    public Vector3f spawnPosition = new Vector3f();

    // TODO: This should be handled via the inventory/equipment system
    public boolean isCarryingTorch = false;
}
