package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class LocalPlayerComponent extends AbstractComponent {
    // View Direction should be in another component, possible Creature?
    public float viewYaw = 0;
    public float viewPitch = 0;

    // This should be in another component too
    public boolean isDead = false;

    // Should be here I think(only the local player needs to know the slot),
    // but equipped item will need to be reflected elsewhere so it can
    // be replicated to all players
    public int selectedTool = 0;
}
