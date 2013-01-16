/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.componentSystem.controllers;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.terasology.componentSystem.RenderSystem;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.*;
import org.terasology.components.actions.CraftingActionComponent;
import org.terasology.components.utility.DroppedItemTypeComponent;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entityFactory.DroppedItemFactory;
import org.terasology.entitySystem.*;
import org.terasology.events.crafting.AddItemEvent;
import org.terasology.events.crafting.ChangeLevelEvent;
import org.terasology.events.crafting.CheckRefinementEvent;
import org.terasology.events.crafting.DeleteItemEvent;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.rendering.CraftingGrid;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.layout.ChooseRowLayout;
import org.terasology.rendering.gui.widgets.UICompositeScrollable;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIItemContainer;
import org.terasology.world.block.*;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.DroppedBlockFactory;
import org.terasology.events.ActivateEvent;
import org.terasology.events.DamageEvent;
import org.terasology.events.HealthChangedEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.events.RespawnEvent;
import org.terasology.input.events.MouseXAxisEvent;
import org.terasology.input.events.MouseYAxisEvent;
import org.terasology.input.binds.*;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.input.ButtonState;
import org.terasology.input.CameraTargetSystem;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.world.WorldProvider;
import org.terasology.math.TeraMath;
import org.terasology.math.AABB;
import org.terasology.physics.ImpulseEvent;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.rendering.AABBRenderer;
import org.terasology.rendering.cameras.DefaultCamera;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector2f;

/**
 * @author Immortius <immortius@gmail.com>
 */
// TODO: This needs a really good cleanup
// TODO: Move more input stuff to a specific input system?
// TODO: Camera should become an entity/component, so it can follow the player naturally
public class LocalPlayerSystem implements UpdateSubscriberSystem, RenderSystem, EventHandlerSystem {
    private LocalPlayer localPlayer;
    private CameraTargetSystem cameraTargetSystem;
    private Timer timer;

    private WorldProvider worldProvider;
    private DefaultCamera playerCamera;

    private long lastTimeSpacePressed;
    private long lastInteraction, lastTimeThrowInteraction, lastTimeShowInventory;

    private boolean cameraBobbing = Config.getInstance().isCameraBobbing();
    private float bobFactor = 0;
    private float lastStepDelta = 0;

    private Vector3f relativeMovement = new Vector3f();

    private AABBRenderer aabbRenderer = new AABBRenderer(AABB.createEmpty());

    //Crafting
    private boolean useButtonPushed = false;
    private boolean toolbarChanged     = false;
    private boolean itsCraftActionTime = false;

    private CraftingGrid craftingGridRenderer = new CraftingGrid();

    private int verticalStep = 0;
    private final int maxVerticalStep = 2;
    private int craftIncreaseDecreaseSpeed = 1;
    private boolean dontDrop = false;

    @Override
    public void initialise() {
        worldProvider = CoreRegistry.get(WorldProvider.class);
        localPlayer = CoreRegistry.get(LocalPlayer.class);
        timer = CoreRegistry.get(Timer.class);
        cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);
    }

    @Override
    public void shutdown() {
    }

    public void setPlayerCamera(DefaultCamera camera) {
        playerCamera = camera;
    }

    @Override
    public void update(float delta) {
        if (!localPlayer.isValid())
            return;

        EntityRef entity = localPlayer.getEntity();
        LocalPlayerComponent localPlayerComponent = entity.getComponent(LocalPlayerComponent.class);
        CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
        LocationComponent location = entity.getComponent(LocationComponent.class);

        if (localPlayerComponent.isDead) {
            return;
        }

        updateMovement(localPlayerComponent, characterMovementComponent, location);

        // TODO: Remove, use component camera, breaks spawn camera anyway
        Quat4f lookRotation = new Quat4f();
        QuaternionUtil.setEuler(lookRotation, TeraMath.DEG_TO_RAD * localPlayerComponent.viewYaw, TeraMath.DEG_TO_RAD * localPlayerComponent.viewPitch, 0);
        updateCamera(characterMovementComponent, location.getWorldPosition(), lookRotation);

        // Hand animation update
        localPlayerComponent.handAnimation = Math.max(0, localPlayerComponent.handAnimation - 2.5f * delta);

        entity.saveComponent(characterMovementComponent);
        entity.saveComponent(localPlayerComponent);

        if(toolbarChanged && cameraTargetSystem.getTarget().getComponent(CraftingActionComponent.class) == null){
            UIItemContainer toolbar = (UIItemContainer)CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("toolbar");
            toolbar.setEntity(localPlayer.getEntity(), 0, 9);
            toolbarChanged = false;
        }

        UIDisplayContainer inventory = (UIDisplayContainer)CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("hud:inventory");

        if( inventory.isVisible() && timer.getTimeInMs() - lastTimeShowInventory >= 1500){
            inventory.setVisible(false);
        }
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onMouseX(MouseXAxisEvent event, EntityRef entity) {
        LocalPlayerComponent localPlayer = entity.getComponent(LocalPlayerComponent.class);
        localPlayer.viewYaw = (localPlayer.viewYaw - event.getValue()) % 360;
        entity.saveComponent(localPlayer);
        LocationComponent loc = entity.getComponent(LocationComponent.class);

        if( event.getTarget().hasComponent(CraftingActionComponent.class) ){
            event.getTarget().send(new CheckRefinementEvent(event.getTarget(), entity) );
        }

        if (loc != null) {
            QuaternionUtil.setEuler(loc.getLocalRotation(), TeraMath.DEG_TO_RAD * localPlayer.viewYaw, 0, 0);
            entity.saveComponent(loc);
        }
        event.consume();
    }

    @ReceiveEvent(components = LocalPlayerComponent.class)
    public void onMouseY(MouseYAxisEvent event, EntityRef entity) {
        LocalPlayerComponent localPlayer = entity.getComponent(LocalPlayerComponent.class);

        if( event.getTarget().hasComponent(CraftingActionComponent.class) ){
            event.getTarget().send(new CheckRefinementEvent(event.getTarget(), entity) );
        }

        if(itsCraftActionTime){
            craftIncreaseDecreaseSpeed += event.getValue();
            if(craftIncreaseDecreaseSpeed > 32){
                craftIncreaseDecreaseSpeed = 32;
            }

            if( craftIncreaseDecreaseSpeed < 1){
                craftIncreaseDecreaseSpeed = 1;
            }
        }else{
            localPlayer.viewPitch = TeraMath.clamp(localPlayer.viewPitch - event.getValue(), -89, 89);
            entity.saveComponent(localPlayer);
            event.consume();
        }
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, CharacterMovementComponent.class})
    public void onJump(JumpButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            CharacterMovementComponent characterMovement = entity.getComponent(CharacterMovementComponent.class);
            characterMovement.jump = true;
            if (timer.getTimeInMs() - lastTimeSpacePressed < 200) {
                characterMovement.isGhosting = !characterMovement.isGhosting;
            }
            lastTimeSpacePressed = timer.getTimeInMs();
            event.consume();
        }
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, CharacterMovementComponent.class})
    public void updateForwardsMovement(ForwardsMovementAxis event, EntityRef entity) {
        relativeMovement.z = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, CharacterMovementComponent.class})
    public void updateStrafeMovement(StrafeMovementAxis event, EntityRef entity) {
        relativeMovement.x = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, CharacterMovementComponent.class})
    public void updateVerticalMovement(VerticalMovementAxis event, EntityRef entity) {
        relativeMovement.y = event.getValue();
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, CharacterMovementComponent.class})
    public void onRun(RunButton event, EntityRef entity) {
        CharacterMovementComponent characterMovement = entity.getComponent(CharacterMovementComponent.class);
        characterMovement.isRunning = event.isDown();
        event.consume();
    }

    @Override
    public void renderOverlay() {
        // TODO: Don't render if not in first person?
        // Display the block the player is aiming at
        if (Config.getInstance().isPlacingBox()) {
            EntityRef target = cameraTargetSystem.getTarget();
            Vector3i blockPos = cameraTargetSystem.getTargetBlockPosition();
            AABB aabb = null;
            BlockComponent blockComp = target.getComponent(BlockComponent.class);
            BlockRegionComponent blockRegion = target.getComponent(BlockRegionComponent.class);
            if (blockComp != null || blockRegion != null) {
                Block block = worldProvider.getBlock(blockPos);
                if (block.isTargetable()) {
                    aabb = block.getBounds(blockPos);
                }
            } else {
                MeshComponent mesh = target.getComponent(MeshComponent.class);
                LocationComponent location = target.getComponent(LocationComponent.class);
                if (mesh != null && mesh.mesh != null && location != null) {
                    aabb = mesh.mesh.getAABB();
                    aabb = aabb.transform(location.getWorldRotation(), location.getWorldPosition(), location.getWorldScale());
                }
            }
            if (aabb != null) {
                if( target.getComponent(CraftingActionComponent.class) != null){
                    craftingGridRenderer.setAABB(aabb, target.getComponent(CraftingActionComponent.class).getCurrentLevel());
                    craftingGridRenderer.render();
                }else{
                    aabbRenderer.setAABB(aabb);
                    aabbRenderer.render(2f);
                }

            }
        }
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onDeath(NoHealthEvent event, EntityRef entity) {
        LocalPlayerComponent localPlayer = entity.getComponent(LocalPlayerComponent.class);
        localPlayer.isDead = true;
        entity.saveComponent(localPlayer);
    }
    
    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onRespawn(RespawnEvent event, EntityRef entity) {
        LocalPlayerComponent localPlayerComponent = entity.getComponent(LocalPlayerComponent.class);
        if (localPlayerComponent.isDead) {
            localPlayerComponent.isDead = false;
            entity.saveComponent(localPlayerComponent);
        }

        LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
        PlayerComponent playerComponent = entity.getComponent(PlayerComponent.class);
        if (playerComponent != null && locationComponent != null) {
            locationComponent.setWorldPosition(playerComponent.spawnPosition);
            entity.saveComponent(locationComponent);
        }

        HealthComponent healthComponent = entity.getComponent(HealthComponent.class);
        if (healthComponent != null) {
            healthComponent.currentHealth = healthComponent.maxHealth;
            entity.send(new HealthChangedEvent(entity, healthComponent.currentHealth, healthComponent.maxHealth));
            entity.saveComponent(healthComponent);
        }
        
        CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
        if (characterMovementComponent != null) {
            characterMovementComponent.setVelocity(new Vector3f(0, 0, 0));
            entity.saveComponent(characterMovementComponent);
        }
    }

    private void updateMovement(LocalPlayerComponent localPlayerComponent, CharacterMovementComponent characterMovementComponent, LocationComponent location) {
        Vector3f relMove = new Vector3f(relativeMovement);
        relMove.y = 0;
        if (characterMovementComponent.isGhosting || characterMovementComponent.isSwimming) {
            Quat4f viewRot = new Quat4f();
            QuaternionUtil.setEuler(viewRot, TeraMath.DEG_TO_RAD * localPlayerComponent.viewYaw, TeraMath.DEG_TO_RAD * localPlayerComponent.viewPitch, 0);
            QuaternionUtil.quatRotate(viewRot, relMove, relMove);
            relMove.y += relativeMovement.y;
        } else {
            QuaternionUtil.quatRotate(location.getLocalRotation(), relMove, relMove);
        }
        float lengthSquared = relMove.lengthSquared();
        if (lengthSquared > 1) relMove.normalize();
        characterMovementComponent.setDrive(relMove);
    }

    private void updateCamera(CharacterMovementComponent charMovementComp, Vector3f position, Quat4f rotation) {
        // The camera position is the player's position plus the eye offset
        Vector3d cameraPosition = new Vector3d();
        // TODO: don't hardset eye position
        cameraPosition.add(new Vector3d(position), new Vector3d(0, 0.6f, 0));

        playerCamera.getPosition().set(cameraPosition);
        Vector3f viewDir = new Vector3f(0, 0, 1);
        QuaternionUtil.quatRotate(rotation, viewDir, playerCamera.getViewingDirection());

        float stepDelta = charMovementComp.footstepDelta - lastStepDelta;
        if (stepDelta < 0) stepDelta += charMovementComp.distanceBetweenFootsteps;
        bobFactor += stepDelta;
        lastStepDelta = charMovementComp.footstepDelta;

        if (cameraBobbing) {
            playerCamera.setBobbingRotationOffsetFactor(calcBobbingOffset(0.0f, 0.01f, 2.5f));
            playerCamera.setBobbingVerticalOffsetFactor(calcBobbingOffset((float) java.lang.Math.PI / 4f, 0.025f, 3f));
        } else {
            playerCamera.setBobbingRotationOffsetFactor(0.0f);
            playerCamera.setBobbingVerticalOffsetFactor(0.0f);
        }

        if (charMovementComp.isGhosting) {
            playerCamera.extendFov(24);
        } else {
            playerCamera.resetFov();
        }
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, InventoryComponent.class})
    public void onAttackRequest(AttackButton event, EntityRef entity) {
        if (!event.isDown() || timer.getTimeInMs() - lastInteraction < 200/craftIncreaseDecreaseSpeed) {
            if(!event.isDown()){
                craftIncreaseDecreaseSpeed = 1;
                itsCraftActionTime = false;
                CraftingActionComponent craftingComponent = event.getTarget().getComponent(CraftingActionComponent.class);
                if(craftingComponent != null && entity.getComponent(CharacterMovementComponent.class).isRunning){
                    event.getTarget().send(new ChangeLevelEvent(-1, entity));
                    return;
                }
            }
            return;
        }

        CraftingActionComponent craftingComponent = event.getTarget().getComponent(CraftingActionComponent.class);

        if (craftingComponent != null) {
             if(!entity.getComponent(CharacterMovementComponent.class).isRunning && timer.getTimeInMs() - lastInteraction > (200/craftIncreaseDecreaseSpeed)){
                 LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
                 if (localPlayerComp.isDead) return;
                 float dropPower  = getDropPower();
                 event.getTarget().send(new DeleteItemEvent( dropPower/6));
                 dontDrop = true;
                 resetDropMark();
                 lastInteraction = timer.getTimeInMs();
                 itsCraftActionTime = true;
             }
            return;
        }

        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        if (localPlayerComp.isDead) return;

        EntityRef selectedItemEntity = inventory.itemSlots.get(localPlayerComp.selectedTool);
        attack(event.getTarget(), entity, selectedItemEntity, event.getTargetBlockPosition());

        lastInteraction = timer.getTimeInMs();
        localPlayerComp.handAnimation = 0.5f;
        entity.saveComponent(localPlayerComp);
        event.consume();
    }

    private void attack(EntityRef target, EntityRef player, EntityRef selectedItemEntity, Vector3i blockTargetPos) {
        // TODO: Should send an attack event to self, and another system common to all creatures should handle this
        int damage = 1;
        ItemComponent item = selectedItemEntity.getComponent(ItemComponent.class);
        if (item != null) {
            damage = item.baseDamage;

            BlockComponent blockComp = target.getComponent(BlockComponent.class);
            BlockRegionComponent blockRegionComponent = target.getComponent(BlockRegionComponent.class);
            if (blockComp != null || blockRegionComponent != null) {
                Block block = worldProvider.getBlock(blockTargetPos);
                if (item.getPerBlockDamageBonus().containsKey(block.getBlockFamily().getURI().toString())) {
                    damage += item.getPerBlockDamageBonus().get(block.getBlockFamily().getURI().toString());
                }
            }
        }
        target.send(new DamageEvent(damage, player));
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onFrobRequest(FrobButton event, EntityRef entity) {
        if (event.getState() != ButtonState.DOWN) {
            return;
        }

        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        if (localPlayerComp.isDead) return;

        EntityRef target = event.getTarget();

        BlockComponent block = target.getComponent(BlockComponent.class);

        if(block != null){
            Block currentBlock = worldProvider.getBlock(block.getPosition());

            EntityManager entityManager = CoreRegistry.get(EntityManager.class);

            if( !currentBlock.isInvisible()  &&
                !currentBlock.isLiquid()     &&
                !currentBlock.isPenetrable() &&
                 currentBlock.isCraftPlace()
              ){
                InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
                if (localPlayerComp.isDead) return;

                EntityRef selectedItemEntity = inventory.itemSlots.get(localPlayerComp.selectedTool);

                ItemComponent item  = selectedItemEntity.getComponent(ItemComponent.class);

                if(item == null){
                    return;
                }

                BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
                EntityRef craftEntity = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("core:craft"));

                BlockItemComponent blockItemComponent = craftEntity.getComponent(BlockItemComponent.class);

                if(!blockItemComponent.placedEntity.hasComponent(CraftingActionComponent.class)){
                    blockItemComponent.placedEntity.addComponent(new CraftingActionComponent());
                }

                EntityRef placedEntity = blockItemComponent.placedEntity;

                if( craftEntity.exists() ){
                    craftEntity.send(new ActivateEvent(target, selectedItemEntity, new Vector3f(playerCamera.getPosition()), new Vector3f(playerCamera.getViewingDirection()), event.getHitPosition(), event.getHitNormal()));
                    placedEntity.send(new ActivateEvent(target, selectedItemEntity, new Vector3f(playerCamera.getPosition()), new Vector3f(playerCamera.getViewingDirection()), event.getHitPosition(), event.getHitNormal()));
                    craftEntity.destroy();

                    if(item.stackCount<=0){
                        selectedItemEntity.destroy();
                    }

                }
                lastInteraction = timer.getTimeInMs();
                localPlayerComp.handAnimation = 0.5f;
                entity.saveComponent(localPlayerComp);
                event.consume();
                return;
            }

         }

        event.getTarget().send(new ActivateEvent(entity, entity, playerCamera.getPosition(), playerCamera.getViewingDirection(), event.getHitPosition(), event.getHitNormal()));
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onDropItem(DropItemButton event, EntityRef entity){
        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        EntityRef selectedItemEntity = inventory.itemSlots.get(localPlayerComp.selectedTool);
        BlockItemComponent block = selectedItemEntity.getComponent(BlockItemComponent.class);
        CraftingActionComponent craftingComponent = event.getTarget().getComponent(CraftingActionComponent.class);

        if( selectedItemEntity.equals(EntityRef.NULL) && craftingComponent == null ){
            return;
        }

        if(event.getState() == ButtonState.DOWN && lastTimeThrowInteraction == 0){
            lastTimeThrowInteraction = timer.getTimeInMs();
            return;
        }

        if (localPlayerComp.isDead) return;

        UIImage crossHair = (UIImage)CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("crosshair");

        crossHair.getTextureSize().set(new Vector2f(22f / 256f, 22f / 256f));

        float dropPower  = getDropPower();

        crossHair.getTextureOrigin().set(new Vector2f((46f + 22f*dropPower) / 256f, 23f / 256f));

        if(event.getState() == ButtonState.UP){

            if(dontDrop){
                resetDropMark();
                dontDrop = false;
                return;
            }

            dropPower *= 25f;

            if( craftingComponent == null ){

                EntityManager entityManager = CoreRegistry.get(EntityManager.class);
                ItemComponent item  = selectedItemEntity.getComponent(ItemComponent.class);

                Vector3f newPosition = new Vector3f(playerCamera.getPosition().x + playerCamera.getViewingDirection().x*1.5f,
                                                    playerCamera.getPosition().y + playerCamera.getViewingDirection().y*1.5f,
                                                    playerCamera.getPosition().z + playerCamera.getViewingDirection().z*1.5f
                                                   );

                boolean changed = false;
                if(!selectedItemEntity.hasComponent(BlockItemComponent.class)){
                    DroppedItemFactory droppedItemFactory = new DroppedItemFactory(entityManager);
                    EntityRef droppedItem = droppedItemFactory.newInstance(new Vector3f(newPosition), item.icon, 200, selectedItemEntity);

                    if(!droppedItem.equals(EntityRef.NULL)){
                        droppedItem.send(new ImpulseEvent(new Vector3f(playerCamera.getViewingDirection().x*dropPower, playerCamera.getViewingDirection().y*dropPower, playerCamera.getViewingDirection().z*dropPower)));
                        changed = true;
                    }
                }else{
                    DroppedBlockFactory droppedBlockFactory = new DroppedBlockFactory(entityManager);
                    EntityRef droppedBlock = droppedBlockFactory.newInstance(new Vector3f(newPosition), block.blockFamily, 20);
                    if(!droppedBlock.equals(EntityRef.NULL)){
                        droppedBlock.send(new ImpulseEvent(new Vector3f(playerCamera.getViewingDirection().x*dropPower, playerCamera.getViewingDirection().y*dropPower, playerCamera.getViewingDirection().z*dropPower)));
                        changed = true;
                    }
                }



                if(changed){
                    item.stackCount--;

                    if(item.stackCount<=0){
                        selectedItemEntity.destroy();
                    }

                    localPlayerComp.handAnimation = 0.5f;
                }
            }
            resetDropMark();
        }

        entity.saveComponent(localPlayerComp);
        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onNextItem(ToolbarNextButton event, EntityRef entity) {
        LocalPlayerComponent localPlayerComp = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
        CharacterMovementComponent characterMovement = localPlayer.getEntity().getComponent(CharacterMovementComponent.class);

        if(characterMovement.isRunning && cameraTargetSystem.getTarget().getComponent(CraftingActionComponent.class) != null){
            InventoryComponent inventory = localPlayer.getEntity().getComponent(InventoryComponent.class);
            UIItemContainer toolbar = (UIItemContainer)CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("toolbar");

            int slotStart = toolbar.getSlotStart();
            int slotEnd   = toolbar.getSlotEnd();

            slotStart += 10;
            slotEnd   += 10;

            if( slotEnd > inventory.itemSlots.size() - 1 ){
                slotStart = 0;
                slotEnd   = 9;
            }

            toolbar.setEntity(localPlayer.getEntity(), slotStart, slotEnd);
            toolbarChanged = true;
            showSmallInventory((slotEnd + 1)/10);
        }else{
            localPlayerComp.selectedTool = (localPlayerComp.selectedTool + 1) % 10;
        }
        localPlayer.getEntity().saveComponent(localPlayerComp);

        if( cameraTargetSystem.getTarget().hasComponent(CraftingActionComponent.class) ){
            cameraTargetSystem.getTarget().send(new CheckRefinementEvent(cameraTargetSystem.getTarget(), entity) );
        }

        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onPrevItem(ToolbarPrevButton event, EntityRef entity) {
        LocalPlayerComponent localPlayerComp = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
        CharacterMovementComponent characterMovement = localPlayer.getEntity().getComponent(CharacterMovementComponent.class);

        if(characterMovement.isRunning && cameraTargetSystem.getTarget().getComponent(CraftingActionComponent.class) != null){
            InventoryComponent inventory = localPlayer.getEntity().getComponent(InventoryComponent.class);
            UIItemContainer toolbar = (UIItemContainer)CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("toolbar");

            int slotStart = toolbar.getSlotStart();
            int slotEnd   = toolbar.getSlotEnd();

            slotStart -= 10;
            slotEnd   -= 10;

            if( slotStart < -1 ){
                slotStart = inventory.itemSlots.size() - 10;
                slotEnd   = inventory.itemSlots.size() - 1;
            }

            if( slotStart < 0){
                slotStart = 0;
            }

            toolbar.setEntity(localPlayer.getEntity(), slotStart, slotEnd);
            toolbarChanged = true;
            showSmallInventory((slotEnd + 1)/10);
        }else{
            localPlayerComp.selectedTool = (localPlayerComp.selectedTool - 1) % 10;
            if (localPlayerComp.selectedTool < 0) {
                localPlayerComp.selectedTool = 10 + localPlayerComp.selectedTool;
            }
        }
        localPlayer.getEntity().saveComponent(localPlayerComp);

        if( cameraTargetSystem.getTarget().hasComponent(CraftingActionComponent.class) ){
            cameraTargetSystem.getTarget().send(new CheckRefinementEvent(cameraTargetSystem.getTarget(), entity) );
        }

        event.consume();
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onSlotButton(ToolbarSlotButton event, EntityRef entity) {
        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        localPlayerComp.selectedTool = event.getSlot();
        localPlayer.getEntity().saveComponent(localPlayerComp);
    }

    @ReceiveEvent(components = {LocalPlayerComponent.class, InventoryComponent.class})
    public void onUseItemRequest(UseItemButton event, EntityRef entity) {
        if (!event.isDown() || timer.getTimeInMs() - lastInteraction < 200/craftIncreaseDecreaseSpeed) {
            if(!event.isDown()){
                useButtonPushed = false;
                itsCraftActionTime = false;
                craftIncreaseDecreaseSpeed = 1;
                CraftingActionComponent craftingComponent = event.getTarget().getComponent(CraftingActionComponent.class);
                if(craftingComponent != null && entity.getComponent(CharacterMovementComponent.class).isRunning){
                    event.getTarget().send(new ChangeLevelEvent( 1, entity));
                    return;
                }
            }
            return;
        }

        CraftingActionComponent craftingComponent = event.getTarget().getComponent(CraftingActionComponent.class);

        if (craftingComponent != null) {
            useButtonPushed = true;
            if( timer.getTimeInMs() - lastInteraction > ( 200/craftIncreaseDecreaseSpeed ) &&
                !entity.getComponent(CharacterMovementComponent.class).isRunning )
            {
                LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
                if (localPlayerComp.isDead) return;
                InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
                UIItemContainer toolbar = (UIItemContainer)CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("toolbar");

                EntityRef selectedItemEntity = inventory.itemSlots.get(localPlayerComp.selectedTool + toolbar.getSlotStart());

                if( !selectedItemEntity.equals(EntityRef.NULL) ){
                    float dropPower  = getDropPower();
                    itsCraftActionTime = true;
                    event.getTarget().send(new AddItemEvent(event.getTarget(), selectedItemEntity, dropPower/6));
                    resetDropMark();
                    lastInteraction = timer.getTimeInMs();
                }
            }
            return;
        }

        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        if (localPlayerComp.isDead) return;

        EntityRef selectedItemEntity = inventory.itemSlots.get(localPlayerComp.selectedTool);

        ItemComponent item = selectedItemEntity.getComponent(ItemComponent.class);
        if (item != null && item.usage != ItemComponent.UsageType.NONE) {
            useItem(event.getTarget(), entity, selectedItemEntity, event.getHitPosition(), event.getHitNormal());
        }


        lastInteraction = timer.getTimeInMs();
        localPlayerComp.handAnimation = 0.5f;
        entity.saveComponent(localPlayerComp);
        event.consume();
    }

    private void useItem(EntityRef target, EntityRef player, EntityRef item, Vector3f hitPosition, Vector3f hitNormal) {
        if (target.exists()) {
            item.send(new ActivateEvent(target, player, new Vector3f(playerCamera.getPosition()), new Vector3f(playerCamera.getViewingDirection()), hitPosition, hitNormal));
        } else {
            item.send(new ActivateEvent(player, new Vector3f(playerCamera.getPosition()), new Vector3f(playerCamera.getViewingDirection())));
        }
    }

    private float calcBobbingOffset(float phaseOffset, float amplitude, float frequency) {
        return (float) java.lang.Math.sin(bobFactor * frequency + phaseOffset) * amplitude;
    }

    private void showSmallInventory(int position){
        UICompositeScrollable inventory = (UICompositeScrollable)CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("hud:inventory");
        inventory.setVisible(true);
        ((ChooseRowLayout)inventory.getLayout()).getPosition().y = (position - 1) * 36f;
        lastTimeShowInventory = timer.getTimeInMs();
    }

    private void resetDropMark(){
        UIImage crossHair = (UIImage)CoreRegistry.get(GUIManager.class).getWindowById("hud").getElementById("crosshair");
        lastTimeThrowInteraction = 0;
        crossHair.getTextureSize().set(new Vector2f(20f / 256f, 20f / 256f));
        crossHair.getTextureOrigin().set(new Vector2f(24f / 256f, 24f / 256f));
    }

    private float getDropPower(){
        if(lastTimeThrowInteraction == 0){
            return 0;
        }
        float dropPower  = (float)Math.floor((timer.getTimeInMs() - lastTimeThrowInteraction)/200);

        if(dropPower>6){
            dropPower = 6;
        }

        return dropPower;
    }

    @Override
    public void renderOpaque() {

    }

    @Override
    public void renderTransparent() {

    }

    @Override
    public void renderFirstPerson() {
    }

}
