package org.terasology.logic;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.terasology.components.*;
import org.terasology.entitySystem.EntityRef;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LocalPlayer {
    
    private EntityRef entity;
    
    public LocalPlayer(EntityRef playerEntity) {
        this.entity = playerEntity;
    }

    public boolean isValid() {
        return entity.hasComponent(LocationComponent.class) && entity.hasComponent(LocalPlayerComponent.class) && entity.hasComponent(PlayerComponent.class);
    }
    
    public Vector3f getPosition() {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location == null) {
            return new Vector3f();
        }
        return location.getWorldPosition();
    }
    
    public Quat4f getRotation() {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location == null) {
            return new Quat4f(0,0,0,1);
        }
        return location.getWorldRotation();
    }

    public boolean isCarryingTorch() {
        
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        LocalPlayerComponent localPlayer = entity.getComponent(LocalPlayerComponent.class);
        if (inventory == null || localPlayer == null)
            return false;
        
        if (inventory.itemSlots.get(localPlayer.selectedTool) != null) {
            EntityRef item = inventory.itemSlots.get(localPlayer.selectedTool);
            return item.getComponent(LightComponent.class) != null;
        }
        return false;
    }
    
    public EntityRef getEntity() {
        return entity;
    }
    
    public Vector3f getVelocity() {
        CharacterMovementComponent movement = entity.getComponent(CharacterMovementComponent.class);
        if (movement != null) {
            return new Vector3f(movement.getVelocity());
        }
        return new Vector3f();
    }
    
    public Vector3f getViewDirection() {
        LocalPlayerComponent localPlayer = entity.getComponent(LocalPlayerComponent.class);
        if (localPlayer == null) {
            return new Vector3f(0,0,-1);
        }
        Quat4f rot = new Quat4f();
        QuaternionUtil.setEuler(rot, localPlayer.viewYaw, localPlayer.viewPitch, 0);
        // TODO: Put a generator for direction vectors in a util class somewhere
        // And just put quaternion -> vector somewhere too
        Vector3f dir = new Vector3f(0,0,-1);
        return QuaternionUtil.quatRotate(rot, dir, dir);
    }
    
    public String toString() {
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f)", getPosition().x, getPosition().y, getPosition().z, getViewDirection().x, getViewDirection().y, getViewDirection().z);
    }
}
