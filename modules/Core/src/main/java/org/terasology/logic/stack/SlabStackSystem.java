/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.logic.stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.logic.characters.events.AttackEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.SelectedInventorySlotComponent;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.inventory.events.InventorySlotStackSizeChangedEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.ChunkMath;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.family.BlockFamily;

@RegisterSystem(RegisterMode.AUTHORITY)
public class SlabStackSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(SlabStackSystem.class);

    private static final int SLAB_PER_LAYER = 1;
    private static final int MAX_LAYERS = 3;
    private static final int MAX_SLABS = MAX_LAYERS * SLAB_PER_LAYER;
    private static final String LAYER_1_URI = "Core:Snowslabeight";
    private static final String LAYER_2_URI = "Core:Snowslabhalf";
    private static final String LAYER_3_URI = "Core:Snowslabquarter";

    @In
    private WorldProvider worldProvider;
    @In
    private BlockManager blockManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private InventoryManager inventoryManager;
    @In
    private LocalPlayer localPlayer;

    //  @ReceiveEvent(components = {SlabComponent.class, SlabStackComponent.class, BlockComponent.class})
    //  public void onActivate(ActivateEvent event, EntityRef entity){
    //     BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
    //     Block block = blockComponent.getBlock();
    //     EntityRef instigator = event.getInstigator();
    //     BlockComponent targetBlockComponent = event.getTarget().getComponent(BlockComponent.class);
    //     if (targetBlockComponent == null) {
    //         event.consume();
    //     }

    //     Side surfaceSide = Side.inDirection(event.getHitNormal());
    //     Side secondaryDirection = ChunkMath.getSecondaryPlacementDirection(event.getDirection(), event.getHitNormal());
    //     Vector3i blockPos = new Vector3i(targetBlockComponent.getPosition());
    //     Vector3i targetPos = new Vector3i(blockPos).add(surfaceSide.getVector3i());
    //     SlabStackComponent stackComponent = event.getTarget().getComponent(SlabStackComponent.class);

    //     if (stackComponent != null && stackComponent.slabs < MAX_SLABS) {
    //         EntityRef stackEntity = event.getTarget();
    //     } else if (canPlaceBlock(blockPos, targetPos)) {
    //     }
    //     event.consume();

    // }
    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onRightClick(ActivateEvent event, EntityRef entity, SlabComponent slabComponent) {
        logger.info("right click is working?");
        EntityRef instigator = event.getInstigator();
        BlockComponent targetBlockComponent = event.getTarget().getComponent(BlockComponent.class);
        if (targetBlockComponent == null) {
            event.consume();
            return;
        }

        Side surfaceSide = Side.inDirection(event.getHitNormal());
        Side secondaryDirection = ChunkMath.getSecondaryPlacementDirection(event.getDirection(), event.getHitNormal());
        Vector3i blockPos = new Vector3i(targetBlockComponent.getPosition());
        Vector3i targetPos = new Vector3i(blockPos).add(surfaceSide.getVector3i());
        SlabStackComponent stackComponent = event.getTarget().getComponent(SlabStackComponent.class);

//        if (stackComponent != null && stackComponent.ingots < MAX_INGOTS) {
//            EntityRef stackEntity = event.getTarget();
//            instigator.send(new PlaySoundEvent(Assets.getSound("engine:PlaceBlock").get(), 0.5f));
//            SelectedInventorySlotComponent selectedSlot = instigator.getComponent(SelectedInventorySlotComponent.class);
//            inventoryManager.moveItem(instigator, instigator, selectedSlot.slot, stackEntity, 0, 1);
//
//        } else if (canPlaceBlock(blockPos, targetPos)) {
//            Block newStackBlock = blockManager.getBlockFamily(LAYER_1_URI)
//                    .getBlockForPlacement(worldProvider, blockEntityRegistry, targetPos, surfaceSide, secondaryDirection);
//            PlaceBlocks placeNewIngotStack = new PlaceBlocks(targetPos, newStackBlock, instigator);
//            worldProvider.getWorldEntity().send(placeNewIngotStack);
//            instigator.send(new PlaySoundEvent(Assets.getSound("engine:PlaceBlock").get(), 0.5f));
//            inventoryManager.moveItem(instigator, instigator, findSlot(instigator), blockEntityRegistry.getBlockEntityAt(targetPos), 0, 1);
//            updateIngotStack(targetPos, 1, instigator);
//        }
        event.consume();
    }

//    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
//    public void onRightClick(ActivateEvent event, EntityRef entity, SlabComponent slabComponent) {
//        logger.info("Works when pressed");
//        EntityRef instigator = event.getInstigator();
//        logger.info("Instigator: "+instigator);
//        BlockComponent targetBlockComponent = event.getTarget().getComponent(BlockComponent.class);
//        logger.info("Target Block: "+targetBlockComponent);
//        if (targetBlockComponent == null) {
//            event.consume();
//            return;
//        }
//
//        Side surfaceSide = Side.inDirection(event.getHitNormal());
//        Side secondaryDirection = ChunkMath.getSecondaryPlacementDirection(event.getDirection(), event.getHitNormal());
//        Vector3i blockPos = new Vector3i(targetBlockComponent.getPosition());
//        Vector3i targetPos = new Vector3i(blockPos).add(surfaceSide.getVector3i());
//        SlabStackComponent stackComponent = event.getTarget().getComponent(SlabStackComponent.class);
//
//        logger.info("StackComponent: " + stackComponent);
//        if (stackComponent != null && stackComponent.slabs < MAX_SLABS) {
//            logger.info("No: " + stackComponent.slabs + " " + MAX_SLABS);
//            EntityRef stackEntity = event.getTarget();
//            instigator.send(new PlaySoundEvent(Assets.getSound("engine:PlaceBlock").get(), 0.5f));
//            SelectedInventorySlotComponent selectedSlot = instigator.getComponent(SelectedInventorySlotComponent.class);
//            //inventoryManager.moveItem(instigator, instigator, selectedSlot.slot, stackEntity, 0, 1);
//
//        }
//        else if (canPlaceBlock(blockPos, targetPos)) {
//            logger.info("Yes: " + stackComponent.slabs);
//            Block newStackBlock = blockManager.getBlockFamily(LAYER_1_URI)
//                    .getBlockForPlacement(worldProvider, blockEntityRegistry, targetPos, surfaceSide, secondaryDirection);
//            PlaceBlocks placeNewSlabStack = new PlaceBlocks(targetPos, newStackBlock, instigator);
//            worldProvider.getWorldEntity().send(placeNewSlabStack);
//            instigator.send(new PlaySoundEvent(Assets.getSound("engine:PlaceBlock").get(), 0.5f));
//            inventoryManager.moveItem(instigator, instigator, findSlot(instigator), blockEntityRegistry.getBlockEntityAt(targetPos), 0, 1);
//            updateSlabStack(targetPos, 1, instigator);
//        }
//        event.consume();
//    }

//    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
//    public void onLeftClick(AttackEvent event, EntityRef stackEntity, SlabStackComponent stackComponent) {
//        EntityRef instigator = event.getInstigator();
//        if (stackComponent.slabs > 0) {
//            inventoryManager.moveItem(stackEntity, instigator, 0, instigator, findSlot(instigator), 1);
//            instigator.send(new PlaySoundEvent(Assets.getSound("engine:Loot").get(), 0.5f));
//        }
//        event.consume();
//    }

    // for real-time updates to the stack
    @ReceiveEvent
    public void onStackSizeChange(InventorySlotStackSizeChangedEvent event, EntityRef stackEntity, SlabStackComponent stackComponent) {
        logger.info("On Stack Size Changing when pressed");
        EntityRef instigator = localPlayer.getCharacterEntity();
        LocationComponent locationComponent = stackEntity.getComponent(LocationComponent.class);
        Vector3i pos = new Vector3i(locationComponent.getWorldPosition());
        if (event.getNewSize() > MAX_SLABS) {
            inventoryManager.moveItem(stackEntity, instigator, 0, instigator, findSlot(instigator), event.getNewSize() - MAX_SLABS);
        }
        updateSlabStack(pos, event.getNewSize(), instigator);
    }

    @ReceiveEvent
    public void onItemPut(BeforeItemPutInInventory event, EntityRef stackEntity, SlabStackComponent stackComponent) {
        logger.info("Item is being put");
        EntityRef item = event.getItem();
        logger.info("Item: "+ item);
        // only slab items allowed in the slab stack
        if (!item.hasComponent(SlabComponent.class)) {
            event.consume();
            return;
        }
    }

    @ReceiveEvent
    public void onEmpty(InventorySlotChangedEvent event, EntityRef stackEntity, SlabStackComponent stackComponent) {
        LocationComponent locationComponent = stackEntity.getComponent(LocationComponent.class);
        Vector3i pos = new Vector3i(locationComponent.getWorldPosition());
        if (event.getOldItem().hasComponent(SlabComponent.class) && event.getNewItem() == EntityRef.NULL) {
            updateSlabStack(pos, 0, localPlayer.getCharacterEntity());
        }
    }

    private void updateSlabStack(Vector3i stackPos, int slabs, EntityRef instigator) {
        logger.info("Updating slab stack");
        EntityRef stackEntity = blockEntityRegistry.getBlockEntityAt(stackPos);
        Block stackBlock = worldProvider.getBlock(stackPos);
        String blockUriString = stackBlock.getBlockFamily().getURI().toString();

        if (slabs < 0 || slabs > MAX_SLABS) {
            return;
        }
        if (slabs == 0) {
            worldProvider.setBlock(stackPos, blockManager.getBlock(BlockManager.AIR_ID));
            return;
        }
        if (!blockUriString.equalsIgnoreCase(LAYER_1_URI) && !blockUriString.equalsIgnoreCase(LAYER_2_URI) && !blockUriString.equalsIgnoreCase(LAYER_3_URI)) {
            // not an slab block
            return;
        }

        SlabStackComponent stackComponent = stackEntity.getComponent(SlabStackComponent.class);
        int currentLayers = (stackComponent.slabs - 1) / SLAB_PER_LAYER  + 1;
        int newLayers = (slabs - 1) / SLAB_PER_LAYER + 1;

        if (currentLayers != newLayers) {
            BlockFamily blockFamily;
            if (newLayers == 2) {
                blockFamily = blockManager.getBlockFamily(LAYER_2_URI);
            } else if (newLayers == 3) {
                blockFamily = blockManager.getBlockFamily(LAYER_3_URI);
            } else {
                blockFamily = blockManager.getBlockFamily(LAYER_1_URI);
            }
            Block newStackBlock = blockFamily.getBlockForPlacement(worldProvider, blockEntityRegistry, stackPos, Side.TOP, stackBlock.getDirection());
            PlaceBlocks placeNewSlabStack = new PlaceBlocks(stackPos, newStackBlock, instigator);
            worldProvider.getWorldEntity().send(placeNewSlabStack);
            stackEntity = blockEntityRegistry.getBlockEntityAt(stackPos);
        }
        stackComponent.slabs = slabs;
        stackEntity.saveComponent(stackComponent);
    }

    private boolean canPlaceBlock(Vector3i blockPos, Vector3i targetPos) {
        Block block = worldProvider.getBlock(blockPos);
        Block targetBlock = worldProvider.getBlock(targetPos);

        if (!block.isAttachmentAllowed()) {
            return false;
        }
        if (!targetBlock.isReplacementAllowed() || targetBlock.isTargetable()) {
            return false;
        }
        return true;
    }

    private int findSlot(EntityRef entity) {
        SelectedInventorySlotComponent selectedSlot = entity.getComponent(SelectedInventorySlotComponent.class);
        if (inventoryManager.getItemInSlot(entity, selectedSlot.slot) == EntityRef.NULL
                || inventoryManager.getItemInSlot(entity, selectedSlot.slot).hasComponent(SlabComponent.class)) {
            return selectedSlot.slot;
        }
        int emptySlot = -1;
        int slotCount = InventoryUtils.getSlotCount(entity);
        for (int i = 0; i < slotCount; i++) {
            if (InventoryUtils.getItemAt(entity, i).hasComponent(SlabComponent.class)) {
                return i;
            } else if (InventoryUtils.getItemAt(entity, i) == EntityRef.NULL && emptySlot == -1) {
                emptySlot = i;
            }
        }
        return emptySlot;
    }
}


