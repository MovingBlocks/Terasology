package org.terasology.logic.systems;

import com.bulletphysics.linearmath.QuaternionUtil;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.terasology.components.*;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.componentSystem.EventHandlerSystem;
import org.terasology.entitySystem.componentSystem.RenderSystem;
import org.terasology.entitySystem.componentSystem.UpdateSubscriberSystem;
import org.terasology.events.DamageEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Terasology;
import org.terasology.logic.global.LocalPlayer;
import org.terasology.logic.manager.Config;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.model.structures.BlockPosition;
import org.terasology.model.structures.RayBlockIntersection;
import org.terasology.rendering.cameras.DefaultCamera;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LocalPlayerSystem implements UpdateSubscriberSystem, RenderSystem, EventHandlerSystem {

    private static TIntIntMap inventorySlotBindMap = new TIntIntHashMap();
    
    static {
        inventorySlotBindMap.put(Keyboard.KEY_1, 0);
        inventorySlotBindMap.put(Keyboard.KEY_2, 1);
        inventorySlotBindMap.put(Keyboard.KEY_3, 2);
        inventorySlotBindMap.put(Keyboard.KEY_4, 3);
        inventorySlotBindMap.put(Keyboard.KEY_5, 4);
        inventorySlotBindMap.put(Keyboard.KEY_6, 5);
        inventorySlotBindMap.put(Keyboard.KEY_7, 6);
        inventorySlotBindMap.put(Keyboard.KEY_8, 7);
        inventorySlotBindMap.put(Keyboard.KEY_9, 8);
    }

    private LocalPlayer localPlayer;

    private IWorldProvider worldProvider;
    private DefaultCamera playerCamera;
    private BlockEntityLookup blockEntityLookup;
    private ItemSystem itemSystem;
    
    private boolean jump = false;
    private Vector3f movementInput = new Vector3f();
    private Vector2f lookInput = new Vector2f();
    private boolean running = false;

    private double mouseSensititivy = Config.getInstance().getMouseSens();
    private float lastTimeSpacePressed;
    private float lastInteraction;
    private boolean toggleGodMode;

    public void initialise() {
        worldProvider = CoreRegistry.get(IWorldProvider.class);
        localPlayer = CoreRegistry.get(LocalPlayer.class);

        ComponentSystemManager systemManager = CoreRegistry.get(ComponentSystemManager.class);
        blockEntityLookup = systemManager.get(BlockEntityLookup.class);
        itemSystem = systemManager.get(ItemSystem.class);
    }

    public void setPlayerCamera(DefaultCamera camera) {
        this.playerCamera = camera;
    }

    public void update(float delta) {
        float deltaSeconds = delta / 1000;
        EntityRef entity = localPlayer.getEntity();
        LocalPlayerComponent localPlayerComponent = entity.getComponent(LocalPlayerComponent.class);
        CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
        LocationComponent location = entity.getComponent(LocationComponent.class);
        PlayerComponent playerComponent = entity.getComponent(PlayerComponent.class);

        if (localPlayerComponent.isDead) {
            if (!checkRespawn(deltaSeconds, entity, localPlayerComponent, characterMovementComponent, location, playerComponent))
                return;
        }

        updateViewDirection(localPlayerComponent, location);
        updateMovement(localPlayerComponent, characterMovementComponent, location);

        // TODO: Remove, use component camera, breaks spawn camera anyway
        Quat4f lookRotation = new Quat4f();
        QuaternionUtil.setEuler(lookRotation, TeraMath.DEG_TO_RAD * localPlayerComponent.viewYaw, TeraMath.DEG_TO_RAD * localPlayerComponent.viewPitch, 0);
        updateCamera(location.getWorldPosition(), lookRotation);

        // Hand animation update
        localPlayerComponent.handAnimation = Math.max(0, localPlayerComponent.handAnimation - 2.5f * deltaSeconds);
        
        resetInput();
    }

    private void resetInput() {
        jump = false;
        toggleGodMode = false;
        movementInput.set(0,0,0);
        lookInput.set(0,0);
    }

    private void updateMovement(LocalPlayerComponent localPlayerComponent, CharacterMovementComponent characterMovementComponent, LocationComponent location) {
        Vector3f relMove = new Vector3f(movementInput);
        relMove.y = 0;
        if (characterMovementComponent.isGhosting || characterMovementComponent.isSwimming) {
            Quat4f viewRot = new Quat4f();
            QuaternionUtil.setEuler(viewRot, TeraMath.DEG_TO_RAD * localPlayerComponent.viewYaw, TeraMath.DEG_TO_RAD * localPlayerComponent.viewPitch, 0);
            QuaternionUtil.quatRotate(viewRot, relMove, relMove);
            relMove.y += movementInput.y;
        } else {
            QuaternionUtil.quatRotate(location.getLocalRotation(), relMove, relMove);
        }
        float lengthSquared = relMove.lengthSquared();
        if (lengthSquared > 1) relMove.normalize();
        characterMovementComponent.setDrive(relMove);

        characterMovementComponent.jump = jump;
        characterMovementComponent.isRunning = running;
        if (toggleGodMode) {
            characterMovementComponent.isGhosting = !characterMovementComponent.isGhosting;
        }
    }

    private void updateViewDirection(LocalPlayerComponent localPlayerComponent, LocationComponent location) {
        localPlayerComponent.viewPitch = TeraMath.clamp(localPlayerComponent.viewPitch + lookInput.y, -89, 89);
        localPlayerComponent.viewYaw = (localPlayerComponent.viewYaw - lookInput.x) % 360;

        QuaternionUtil.setEuler(location.getLocalRotation(), TeraMath.DEG_TO_RAD * localPlayerComponent.viewYaw, 0, 0);
    }

    private boolean checkRespawn(float deltaSeconds, EntityRef entity, LocalPlayerComponent localPlayerComponent, CharacterMovementComponent characterMovementComponent, LocationComponent location, PlayerComponent playerComponent) {
        localPlayerComponent.respawnWait -= deltaSeconds;
        if (localPlayerComponent.respawnWait > 0) {
            characterMovementComponent.getDrive().set(0,0,0);
            characterMovementComponent.jump = false;
            return false;
        }

        // Respawn
        localPlayerComponent.isDead = false;
        HealthComponent health = entity.getComponent(HealthComponent.class);
        if (health != null) {
            health.currentHealth = health.maxHealth;
        }
        location.setWorldPosition(playerComponent.spawnPosition);
        return true;
    }

    private void updateCamera(Vector3f position, Quat4f rotation) {
        // The camera position is the player's position plus the eye offset
        Vector3d cameraPosition = new Vector3d();
        // TODO: don't hardset eye position
        cameraPosition.add(new Vector3d(position), new Vector3d(0,0.6f,0));

        playerCamera.getPosition().set(cameraPosition);
        Vector3f viewDir = new Vector3f(0,0,-1);
        QuaternionUtil.quatRotate(rotation, viewDir, viewDir);
        playerCamera.getViewingDirection().set(viewDir);

        /*if (CAMERA_BOBBING) {
            _defaultCamera.setBobbingRotationOffsetFactor(calcBobbingOffset(0.0f, 0.01f, 2.5f));
            _defaultCamera.setBobbingVerticalOffsetFactor(calcBobbingOffset((float) java.lang.Math.PI / 4f, 0.025f, 3.0f));
        } else {
            _defaultCamera.setBobbingRotationOffsetFactor(0.0);
            _defaultCamera.setBobbingVerticalOffsetFactor(0.0);
        } */

        /*if (!(DEMO_FLIGHT)) {
            _defaultCamera.getViewingDirection().set(getViewingDirection());
        } else {
            Vector3d viewingTarget = new Vector3d(getPosition().x, 40, getPosition().z - 128);
            _defaultCamera.getViewingDirection().sub(viewingTarget, getPosition());
        } */
    }

    public void renderOverlay() {
        // Display the block the player is aiming at
        if (Config.getInstance().isPlacingBox()) {
            RayBlockIntersection.Intersection selectedBlock = calcSelectedBlock();
            if (selectedBlock != null) {
                Block block = BlockManager.getInstance().getBlock(worldProvider.getBlockAtPosition(selectedBlock.getBlockPosition().toVector3d()));
                if (block.isRenderBoundingBox()) {
                    block.getBounds(selectedBlock.getBlockPosition()).render(2f);
                }
            }
        }

    }

    @ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onDeath(NoHealthEvent event, EntityRef entity) {
        LocalPlayerComponent localPlayer = entity.getComponent(LocalPlayerComponent.class);
        localPlayer.isDead = true;
        localPlayer.respawnWait = 1.0f;
    }

    /**
     * Processes the keyboard input.
     *
     * @param key         Pressed key on the keyboard
     * @param state       The state of the key
     * @param repeatEvent True if repeat event
     */
    public void processKeyboardInput(int key, boolean state, boolean repeatEvent) {
        if (inventorySlotBindMap.containsKey(key)) {
            LocalPlayerComponent localPlayerComp = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
            localPlayerComp.selectedTool = inventorySlotBindMap.get(key);
            return;
        }
        switch (key) {
            case Keyboard.KEY_K:
                if (!repeatEvent && state) {
                    localPlayer.getEntity().send(new DamageEvent(9999, null));
                }
                break;
            case Keyboard.KEY_SPACE:
                if (!repeatEvent && state) {
                    jump = true;

                    // TODO: handle time better
                    if (Terasology.getInstance().getTimeInMs() - lastTimeSpacePressed < 200) {
                        toggleGodMode = true;
                    }

                    lastTimeSpacePressed = Terasology.getInstance().getTimeInMs();
                }
                break;
        }
    }

    /**
     * Processes the mouse input.
     *
     * @param button     Pressed mouse button
     * @param state      State of the mouse button
     * @param wheelMoved Distance the mouse wheel moved since last
     */
    public void processMouseInput(int button, boolean state, int wheelMoved) {
        if (wheelMoved != 0) {
            LocalPlayerComponent localPlayerComp = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
            localPlayerComp.selectedTool = (localPlayerComp.selectedTool + wheelMoved / 120) % 9;
            while (localPlayerComp.selectedTool < 0) {
                localPlayerComp.selectedTool = 9 + localPlayerComp.selectedTool;
            }
        } else if (state && (button == 0 || button == 1)) {
            processInteractions(button);
        }
    }

    /**
     * Processes interactions for the given mouse button.
     *
     * @param button The pressed mouse button
     */
    private void processInteractions(int button) {
        // Throttle interactions
        if (Terasology.getInstance().getTimeInMs() - lastInteraction < 200) {
            return;
        }

        EntityRef entity = localPlayer.getEntity();
        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        InventoryComponent inventory = localPlayer.getEntity().getComponent(InventoryComponent.class);

        if (localPlayerComp.isDead) return;

        EntityRef selectedItemEntity = inventory.itemSlots.get(localPlayerComp.selectedTool);
        if (Mouse.isButtonDown(0) || button == 0) {
            if (selectedItemEntity != null) {
                ItemComponent item = selectedItemEntity.getComponent(ItemComponent.class);
                switch (item.usage) {
                    case OnBlock:
                        useItemOnBlock(entity, selectedItemEntity);
                        break;
                    case OnUser:
                        itemSystem.useItem(selectedItemEntity, entity);
                        break;
                    case InDirection:
                        itemSystem.useItemInDirection(selectedItemEntity, new Vector3f(playerCamera.getPosition()), new Vector3f(playerCamera.getViewingDirection()), entity);
                        break;
                    default:
                        attack(entity, selectedItemEntity);
                        break;
                }
            } else {
                attack(entity, selectedItemEntity);
            }
            lastInteraction = Terasology.getInstance().getTimeInMs();
            localPlayerComp.handAnimation = 0.5f;
        } else if (Mouse.isButtonDown(1) || button == 1) {
            attack(entity, selectedItemEntity);
            lastInteraction = Terasology.getInstance().getTimeInMs();
            localPlayerComp.handAnimation = 0.5f;
        }

    }
    
    private void useItemOnBlock(EntityRef player, EntityRef item) {
        RayBlockIntersection.Intersection blockIntersection = calcSelectedBlock();
        if (blockIntersection != null) {
            Vector3i centerPos = blockIntersection.getBlockPosition();
            Vector3i blockPos = blockIntersection.calcAdjacentBlockPos();

            // Need two things:
            // 1. The Side of attachment
            Side attachmentSide = Side.inDirection(blockPos.x - centerPos.x, blockPos.y - centerPos.y, blockPos.z - centerPos.z);
            // 2. The secondary direction
            Vector3f attachDir = new Vector3f(centerPos.x - blockPos.x, centerPos.y - blockPos.y, centerPos.z - blockPos.z);
            Vector3f rawDirection = new Vector3f(playerCamera.getViewingDirection());
            float dot = rawDirection.dot(attachDir);
            rawDirection.sub(new Vector3f(dot * attachDir.x, dot * attachDir.y, dot * attachDir.z));
            Side direction = Side.inDirection(rawDirection.x, rawDirection.y, rawDirection.z);

            itemSystem.useItemOnBlock(item, player, centerPos, attachmentSide, direction);
        }
    }

    /**
     * Attacks with currently held item
     */
    // TODO: Move this somewhere more central, for use by all creatures.
    private void attack(EntityRef player, EntityRef withItem) {
        RayBlockIntersection.Intersection selectedBlock = calcSelectedBlock();
        ItemComponent item = null;
        if (withItem != null) {
            item = withItem.getComponent(ItemComponent.class);
        }

        if (selectedBlock != null) {

            BlockPosition blockPos = selectedBlock.getBlockPosition();
            byte currentBlockType = worldProvider.getBlock(blockPos.x, blockPos.y, blockPos.z);
            Block block = BlockManager.getInstance().getBlock(currentBlockType);
            
            int damage = 1;
            if (item != null) {
                damage = item.baseDamage;
                if (item.getPerBlockDamageBonus().containsKey(block.getBlockGroup().getTitle())) {
                    damage += item.getPerBlockDamageBonus().get(block.getBlockGroup().getTitle());
                }
            }

            if (block.isDestructible()) {
                EntityRef blockEntity = blockEntityLookup.getOrCreateEntityAt(blockPos);
                blockEntity.send(new DamageEvent(damage, player));
            }
        }


    }

    public void updateInput() {

        // Process interactions even if the mouse button is pressed down
        // and not fired by a repeated event
        processInteractions(-1);

        movementInput.set(0, 0, 0);
        lookInput.set((float)(mouseSensititivy * Mouse.getDX()), (float)(mouseSensititivy * Mouse.getDY()));
        
        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
            movementInput.z -= 1.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
            movementInput.z += 1.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
            movementInput.x -= 1.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
            movementInput.x += 1.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            movementInput.y += 1.0f;
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
            movementInput.y -= 1.0f;
        }

        running = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
    }

    /**
     * Calculates the currently targeted block in front of the player.
     *
     * @return Intersection point of the targeted block
     */
    private RayBlockIntersection.Intersection calcSelectedBlock() {
        // TODO: Proper and centralised ray tracing support though world
        List<RayBlockIntersection.Intersection> inters = new ArrayList<RayBlockIntersection.Intersection>();

        Vector3f pos = new Vector3f(playerCamera.getPosition());
        
        int blockPosX, blockPosY, blockPosZ;

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    // Make sure the correct block positions are calculated relatively to the position of the player
                    blockPosX = (int) (pos.x + (pos.x >= 0 ? 0.5f : -0.5f)) + x;
                    blockPosY = (int) (pos.y + (pos.y >= 0 ? 0.5f : -0.5f)) + y;
                    blockPosZ = (int) (pos.z + (pos.z >= 0 ? 0.5f : -0.5f)) + z;

                    byte blockType = worldProvider.getBlock(blockPosX, blockPosY, blockPosZ);

                    // Ignore special blocks
                    if (BlockManager.getInstance().getBlock(blockType).isSelectionRayThrough()) {
                        continue;
                    }

                    // The ray originates from the "player's eye"
                    List<RayBlockIntersection.Intersection> iss = RayBlockIntersection.executeIntersection(worldProvider, blockPosX, blockPosY, blockPosZ, playerCamera.getPosition(), playerCamera.getViewingDirection());

                    if (iss != null) {
                        inters.addAll(iss);
                    }
                }
            }
        }

        /**
         * Calculated the closest intersection.
         */
        if (inters.size() > 0) {
            Collections.sort(inters);
            return inters.get(0);
        }

        return null;
    }


    public void renderOpaque() {

    }

    public void renderTransparent() {

    }

    public void renderFirstPerson() {
    }
}
