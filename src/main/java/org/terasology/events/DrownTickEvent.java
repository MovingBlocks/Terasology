package org.terasology.events;

import javax.vecmath.Vector3f;
import org.terasology.entitySystem.AbstractEvent;
import org.terasology.world.block.Block;

/**
 * @author Nick "SleekoNiko" Caplinger <sleekoniko@gmail.com>
 */
public class DrownTickEvent extends AbstractEvent {
    private Block liquid;
    private Vector3f position;

    public DrownTickEvent(Block liquidBlock, Vector3f position){
        this.liquid = liquidBlock;
        this.position = position;
    }

    /**
     * @return The liquid that the player is drowning in
     */
    public Block getLiquid() {
        return liquid;
    }

    /**
     * @return The location of the drowning
     */
    public Vector3f getPosition() {
        return position;
    }
}
