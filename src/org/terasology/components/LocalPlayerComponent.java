package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class LocalPlayerComponent extends AbstractComponent {
    public float viewYaw = 0;
    public float viewPitch = 0;
    public boolean isDead = false;
}
