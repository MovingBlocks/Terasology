package org.terasology.FlightModeHUDToggle.systems;

import org.terasology.HUDToggleButtons.systems.HUDToggleButtonsClientSystem;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.MovementMode;
import org.terasology.logic.characters.events.SetMovementModeEvent;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.World;
import org.terasology.world.generator.WorldGenerator;

/**
 * Class that toggles the Flying mode in Terasology when clicking a button
 */
@RegisterSystem(RegisterMode.CLIENT)
public class FlightModeHUDToggleButton extends BaseComponentSystem implements HUDToggleButtonsClientSystem.HUDToggleButtonState {
    @In
    HUDToggleButtonsClientSystem toggleButtonsClientSystem;
    @In
    EntityManager entityManager;
    @In
    Console console;
    @In
    private WorldGenerator worldGenerator;
    
    EntityRef localClientEntity;
    Vector3f oldLocation;

    @Override
    public void initialise() {
        toggleButtonsClientSystem.registerToggleButton(this);
    }

    /**
     * @return localClientEntity A reference to the local client Entity
     * Get the current client Entity (The local one)
     */
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

    /**
     * @return EntityRef A reference to the local character Entity
     * Get the current character Entity (The local one)
     */
    private EntityRef getLocalCharacterEntity() {
        EntityRef clientEntity = getLocalClientEntity();
        ClientComponent clientComponent = clientEntity.getComponent(ClientComponent.class);
        return clientComponent.character;
    }

    /* 
     * Overrides the toggle method for the flying button
     */
    @Override
    public void toggle() {
        MovementMode nextMode = MovementMode.WALKING;
        if (getMovementMode() != MovementMode.FLYING) {
            nextMode = MovementMode.FLYING;
        }
        getLocalCharacterEntity().send(new SetMovementModeEvent(nextMode));
        setNewSpeed();
    }
    
    
    /**
     * @return A string with a console message, showing the new speed for flying mode
     * This private method sets the new speed when the flying mode is toggled by clicking the button
     */
    private void setNewSpeed(){
    	MovementMode move = getMovementMode();
        ClientComponent clientComp = getLocalClientEntity().getComponent(ClientComponent.class);
        CharacterMovementComponent newMove = clientComp.character.getComponent(CharacterMovementComponent.class);
        if (move == MovementMode.FLYING) {
        	// We must save the position before the fly mode was toggled
        	saveOldPosition(clientComp);
            newMove.speedMultiplier = calculateSpeed();
            clientComp.character.saveComponent(newMove);
            console.addMessage("Speed multiplier set to " + newMove.speedMultiplier + " (was 1.0f)");
        }
        else{
        	// In this case we're toggling the walking mode. We need to get back to the old position.
        	goBack(clientComp);
        	newMove.speedMultiplier = 1.0f;
        	clientComp.character.saveComponent(newMove);
        	console.addMessage("Setting the speed multiplier to the original value (1.0f)");
        }
    }
    
	/**
	 * @param clientComp Instance of the current ClientComponent
	 * Helper method that changes back the position of the character to the old position (Before flying).
	 */
	private void goBack(ClientComponent clientComp) {
        // Deactivate the character to reset the CharacterPredictionSystem,
        // which would overwrite the character location
        clientComp.character.send(BeforeDeactivateComponent.newInstance());
		LocationComponent newLocation = clientComp.character.getComponent(LocationComponent.class);
		console.addMessage("Current position is: " + newLocation.getWorldPosition().toString());
        newLocation.setWorldPosition(this.oldLocation);
        clientComp.character.saveComponent(newLocation);

        // We must reactive the character
        clientComp.character.send(OnActivatedComponent.newInstance());
        console.addMessage("You're back to the initial position. You're in: "+ this.oldLocation.toString());
	}

	/**
	 * @param clientComp Instance of the current ClientComponent
	 * Helper method that saves the position of the character before flying.
	 */
	private void saveOldPosition(ClientComponent clientComp) {
		LocationComponent oldLocation = clientComp.character.getComponent(LocationComponent.class);
		this.oldLocation = oldLocation.getWorldPosition();
		console.addMessage("Saved the initial position. It was: "+ this.oldLocation.toString());
	}
	
	/**
	 * @return A float value of the calculated Velocity.
	 * Method that calculates the velocity of the character according to the size of the map.
	 */
	private float calculateSpeed() {
		World world = worldGenerator.getWorld();
		if (world != null) {
			Region worldRegion = world.getWorldData(Region3i.createFromMinAndSize(new Vector3i(0, 0, 0),ChunkConstants.CHUNK_SIZE));
			// We get the mean of the Max and Min values of each coordenate
			float meanX = (float) (( worldRegion.getRegion().maxX() - worldRegion.getRegion().minX() ) / 2.0);
			float meanY = (float) (( worldRegion.getRegion().maxY() - worldRegion.getRegion().minY() ) / 2.0);
			float meanZ = (float) (( worldRegion.getRegion().maxZ() - worldRegion.getRegion().minZ() ) / 2.0);
			// We take the log2 of the module of the resultant vector, for a better scaling
			float calculatedVelocity = (float) ((float) Math.log(Math.sqrt(meanX*meanX + meanY*meanY + meanZ*meanZ)) / Math.log(2));
			console.addMessage("The calculated Velocity according to the map is :" + calculatedVelocity);
			return calculatedVelocity;
		}
		return 0;
	}

    @Override
    public boolean isValid() {
        return true;
    }

    /**
     * @return An instance of the current movement mode of the character
     * Method that returns the current movement mode of the character
     */
    private MovementMode getMovementMode() {
        EntityRef character = getLocalCharacterEntity();
        CharacterMovementComponent movementComponent = character.getComponent(CharacterMovementComponent.class);
        return movementComponent.mode;
    }

/*
 * Set the right text into the button label
 */
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
