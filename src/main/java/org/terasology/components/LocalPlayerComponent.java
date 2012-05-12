package org.terasology.components;

import org.terasology.entitySystem.Component;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class LocalPlayerComponent implements Component {
    // View Direction should be in another component, possible Creature?
    public float viewYaw = 0;
    public float viewPitch = 0;


    // Should this be in another component? Player probably.
    public boolean isDead = false;
    public float respawnWait = 0;
    public boolean minionMode = false;
    public boolean minionSelect = false;

    // Should be here I think (only the local player needs to know the slot),
    // but equipped item will need to be reflected elsewhere so it can
    // be replicated to all players
    public int selectedTool = 0;
    public int selectedMinion = 0;
    public float handAnimation = 0;
}
