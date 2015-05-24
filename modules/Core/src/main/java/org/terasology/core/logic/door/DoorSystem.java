/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.core.logic.door;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.audio.StaticSound;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Immortius
 */
@RegisterSystem
public class DoorSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(DoorSystem.class);

    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;
    @In
    private AudioManager audioManager;
    @In
    private InventoryManager inventoryManager;

    /**
     * Place a new door when the player activates the event for building doors.
     * @param event Event received
     * @param entity Reference entity
     */
    @ReceiveEvent(components = {DoorComponent.class, ItemComponent.class})
    public void placeDoor(ActivateEvent event, EntityRef entity) {
        DoorComponent door = entity.getComponent(DoorComponent.class);
        BlockComponent targetBlockComp = event.getTarget().getComponent(BlockComponent.class);
        
        //Marks the event as consumed if there is not a target block
        if (targetBlockComp == null){
            event.consume();
            return;
        }
        
        //Check if the event is facing in the XZ plane.
        Side facingDir = getEventFacingDirection(event);
        if (!facingDir.isHorizontal()) {
            event.consume();
            return;
        }
        
        //Calculates the interaction direction between the player and the event
        Side offsetDir = getOffsetDirection(event, targetBlockComp);

        //Finds the block pushed by the target.
        Vector3i primePos = new Vector3i(targetBlockComp.getPosition());
        primePos.add(offsetDir.getVector3i());
        Block primeBlock = worldProvider.getBlock(primePos);
        
        //Test if this block can be replaced
        if (!primeBlock.isReplacementAllowed()) {
            event.consume();
            return;
        }

        // Determine top and bottom blocks
        Vector3i bottomBlockPos = new Vector3i(primePos.x, primePos.y - 1, primePos.z);
        Vector3i topBlockPos = new Vector3i(primePos.x, primePos.y + 1, primePos.z);
        if (isReplaceAllowed(bottomBlockPos)) {
            topBlockPos = primePos;
        } else if (isReplaceAllowed(topBlockPos)) {
            bottomBlockPos = primePos;
        } else {
            event.consume();
            return;
        }
        
        //Determine what side of the door would attached
        Side attachSide = determineAttachSide(facingDir, offsetDir, bottomBlockPos, topBlockPos);
        
        //Checks is that side is a correct side
        if (attachSide == null) {
            event.consume();
            return;
        }

        //Gets the closed side of the door
        Side closedSide = getClosedSide(facingDir, attachSide);

        //Builds a map using the bottom block and the top one. Those blocks determines the door to be placed.
        Map<Vector3i, Block> blockMap = buildBlockMap(door, bottomBlockPos,
                topBlockPos, closedSide);
        
        //Create an event to place the two blocks of the previous map as the door.
        PlaceBlocks blockEvent = new PlaceBlocks(blockMap, event.getInstigator());
        worldProvider.getWorldEntity().send(blockEvent);

        //If the event was successful, then a new door is created.
        if (!blockEvent.isConsumed()) {
            buildDoor(entity, bottomBlockPos, topBlockPos, attachSide,
                    closedSide);
        }
    }

    /**
     * Builds a new door.
     * @param entity
     * @param bottomBlockPos Bottom block of the door
     * @param topBlockPos Top block of the door
     * @param attachSide Attach side of the door
     * @param closedSide Closed side of the door
     */
    private void buildDoor(EntityRef entity, Vector3i bottomBlockPos,
            Vector3i topBlockPos, Side attachSide, Side closedSide) {
        EntityRef newDoor = entityManager.copy(entity);
        newDoor.addComponent(new BlockRegionComponent(Region3i.createBounded(bottomBlockPos, topBlockPos)));
        Vector3f doorCenter = bottomBlockPos.toVector3f();
        doorCenter.y += 0.5f;
        newDoor.addComponent(new LocationComponent(doorCenter));
        DoorComponent newDoorComp = newDoor.getComponent(DoorComponent.class);
        newDoorComp.closedSide = closedSide;
        newDoorComp.openSide = attachSide.reverse();
        newDoorComp.isOpen = false;
        newDoor.saveComponent(newDoorComp);
        newDoor.removeComponent(ItemComponent.class);
        audioManager.playSound(Assets.getSound("engine:PlaceBlock"), 0.5f);
        logger.info("Closed Side: {}", newDoorComp.closedSide);
        logger.info("Open Side: {}", newDoorComp.openSide);
    }

    /**
     * Creates a hashmap that contains two blocks of a door.
     * @param door Door component
     * @param bottomBlockPos Bottom position of the door.
     * @param topBlockPos Top position of the door.
     * @param closedSide Closed side of the door.
     * @return
     */
    private Map<Vector3i, Block> buildBlockMap(DoorComponent door,
            Vector3i bottomBlockPos, Vector3i topBlockPos, Side closedSide) {
        Block newBottomBlock = door.bottomBlockFamily.getBlockForPlacement(worldProvider, blockEntityRegistry, bottomBlockPos, closedSide, Side.TOP);
        Block newTopBlock = door.topBlockFamily.getBlockForPlacement(worldProvider, blockEntityRegistry, bottomBlockPos, closedSide, Side.TOP);

        Map<Vector3i, Block> blockMap = new HashMap<>();
        blockMap.put(bottomBlockPos, newBottomBlock);
        blockMap.put(topBlockPos, newTopBlock);
        return blockMap;
    }

    /**
     * Get the closed side of a door given its facing side and the attach one.
     * @param facingDir Facing side of the door
     * @param attachSide Attach side of the door
     * @return
     */
    private Side getClosedSide(Side facingDir, Side attachSide) {
        Side closedSide = facingDir.reverse();
        if (closedSide == attachSide || closedSide.reverse() == attachSide) {
            closedSide = attachSide.yawClockwise(1);
        }
        return closedSide;
    }

    /**
     * @param bottomBlockPos Block position
     * @return true if can be replaced
     */
    private boolean isReplaceAllowed(Vector3i bottomBlockPos) {
        Block belowBlock = worldProvider.getBlock(bottomBlockPos.x, bottomBlockPos.y, bottomBlockPos.z);
        return belowBlock.isReplacementAllowed();
    }

    /**
     * Gets the direction between the target and the event source point
     * @param event Event
     * @param targetBlockComp Target block
     * @return Direction from target to event
     */
    private Side getOffsetDirection(ActivateEvent event,
            BlockComponent targetBlockComp) {
        Vector3f offset = new Vector3f(event.getHitPosition());
        offset.sub(targetBlockComp.getPosition().toVector3f());
        return Side.inDirection(offset);
    }

    /**
     * @param event Event
     * @return Event facing direction
     */
    private Side getEventFacingDirection(ActivateEvent event) {
        Vector3f horizDir = new Vector3f(event.getDirection());
        horizDir.y = 0;
        return Side.inDirection(horizDir);
    }

    /**
     * Determines the attached side of the door. 
     * @param facingDir Facing side of the door.
     * @param offsetDir Side of direction where the player look.
     * @param bottomBlockPos Bottom position of the door
     * @param topBlockPos Top position of the door.
     * @return
     */
    private Side determineAttachSide(Side facingDir, Side offsetDir, Vector3i bottomBlockPos, Vector3i topBlockPos) {
        Side attachSide = null;
        if (offsetDir.isHorizontal()) {
            Side reversed = offsetDir.reverse();
            if (canBeAttachedSide(bottomBlockPos, topBlockPos, reversed)) {
                attachSide = offsetDir.reverse();
            }
        }
        
        if(attachSide != null)
            return attachSide;
        
        Side clockwise = facingDir.yawClockwise(1);
        if (canBeAttachedSide(bottomBlockPos, topBlockPos, clockwise)) {
            attachSide = clockwise;
        }
        
        if(attachSide != null)
            return attachSide;
        
        Side anticlockwise = facingDir.yawClockwise(-1);
        if (canBeAttachedSide(bottomBlockPos, topBlockPos, anticlockwise)) {
            attachSide = anticlockwise;
        }
        return attachSide;
    }

    /**
     * @param bottomBlockPos Door bottom position
     * @param topBlockPos Door top position
     * @param side
     * @return true if side can be attached from both positions
     */
    private boolean canBeAttachedSide(Vector3i bottomBlockPos,
            Vector3i topBlockPos, Side side) {
        return canAttachTo(topBlockPos, side) && canAttachTo(bottomBlockPos, side);
    }
    

    /**
     * Verifies if the side can be the attached one from a position.
     * @param doorPos Door position
     * @param side Side of the door.
     * @return true if the indicated side of the door can be its attached side.
     */
    private boolean canAttachTo(Vector3i doorPos, Side side) {
        Vector3i adjacentBlockPos = new Vector3i(doorPos);
        adjacentBlockPos.add(side.getVector3i());
        Block adjacentBlock = worldProvider.getBlock(adjacentBlockPos);
        return adjacentBlock.isAttachmentAllowed();
    }

    @ReceiveEvent(components = {DoorComponent.class, BlockRegionComponent.class, LocationComponent.class})
    public void onFrob(ActivateEvent event, EntityRef entity) {
        DoorComponent door = entity.getComponent(DoorComponent.class);
        Side newSide = (door.isOpen) ? door.closedSide : door.openSide;
        BlockRegionComponent regionComp = entity.getComponent(BlockRegionComponent.class);
        Block bottomBlock = door.bottomBlockFamily.getBlockForPlacement(worldProvider, blockEntityRegistry, regionComp.region.min(), newSide, Side.TOP);
        worldProvider.setBlock(regionComp.region.min(), bottomBlock);
        Block topBlock = door.topBlockFamily.getBlockForPlacement(worldProvider, blockEntityRegistry, regionComp.region.max(), newSide, Side.TOP);
        worldProvider.setBlock(regionComp.region.max(), topBlock);
        StaticSound sound = (door.isOpen) ? door.closeSound : door.openSound;
        if (sound != null) {
            LocationComponent loc = entity.getComponent(LocationComponent.class);
            audioManager.playSound(sound, loc.getWorldPosition(), 10, 1);
        }

        door.isOpen = !door.isOpen;
        entity.saveComponent(door);
    }
}
