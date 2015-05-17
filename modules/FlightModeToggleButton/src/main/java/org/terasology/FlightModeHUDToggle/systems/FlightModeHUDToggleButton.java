package org.terasology.FlightModeHUDToggle.systems;

import org.terasology.HUDToggleButtons.systems.HUDToggleButtonsClientSystem;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.events.SetMovementModeEvent;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.World;
import org.terasology.world.generator.WorldGenerator;

@RegisterSystem(RegisterMode.CLIENT)
public class FlightModeHUDToggleButton extends BaseComponentSystem implements HUDToggleButtonsClientSystem.HUDToggleButtonState {
    @In
    HUDToggleButtonsClientSystem toggleButtonsClientSystem;
    @In
    EntityManager entityManager;

    EntityRef localClientEntity;

    @Override
    public void initialise() {
        toggleButtonsClientSystem.registerToggleButton(this);
    }


    private EntityRef getLocalClientEntity() {
        if (localClientEntity == null) {
            for (EntityRef entityRef : entityManager.getEntitiesWith(ClientComponent.class)) {
                ClientComponent clientComponent = entityRef.getComponent(ClientComponent.class);
                if (clientComponent.local) {
                    localClientEntity = entityRef;
                    break;
                }
            }
        }

        return localClientEntity;
    }

    private EntityRef getLocalCharacterEntity() {
        EntityRef clientEntity = getLocalClientEntity();
        ClientComponent clientComponent = clientEntity.getComponent(ClientComponent.class);
        return clientComponent.character;
    }

    @Override
    public void toggle() {
        MovementMode nextMode = MovementMode.WALKING;
        if (getMovementMode() != MovementMode.FLYING) {
            nextMode = MovementMode.FLYING;
        }
        getLocalCharacterEntity().send(new SetMovementModeEvent(nextMode));
        setNewSpeed();
    }
    
    private String setNewSpeed(){
    	MovementMode move = getMovementMode();
        ClientComponent clientComp = getLocalClientEntity().getComponent(ClientComponent.class);
        CharacterMovementComponent newMove = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move == MovementMode.FLYING) {
            newMove.speedMultiplier = 8.0f;
//            float d = calculateSpeed();
            clientComp.character.saveComponent(newMove);
            //console.addMessage("Speed multiplier set to " + 8f + " (was " + oldSpeedMultipler + ")");
        }
        else{
        	newMove.speedMultiplier = 1.0f;
        	clientComp.character.saveComponent(newMove);
        }
        return "";
    }
    
//    private float calculateSpeed(){
//    	World world = getLocalCharacterEntity().getComponent(new WorldComponent());
//        if (world != null) {
//            Region worldRegion = world.getWorldData(Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), ChunkConstants.CHUNK_SIZE));
//        }
//        return 1.0f;
//    }

    @Override
    public boolean isValid() {
        return true;
    }

    private MovementMode getMovementMode() {
        EntityRef character = getLocalCharacterEntity();
        CharacterMovementComponent movementComponent = character.getComponent(CharacterMovementComponent.class);
        return movementComponent.mode;
    }

    @Override
    public String getText() {
        switch (getMovementMode()) {
            case CLIMBING:
                return "Climbing";
            case FLYING:
                return "Flying";
            case SWIMMING:
                return "Swimming";
            case WALKING:
                return "Walking";
            case GHOSTING:
                return "Ghosting";
            default:
                return "Unknown";
        }
    }
}
