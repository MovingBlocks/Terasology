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

package org.terasology.logic.characters;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.registry.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.utilities.collection.CircularBuffer;
import org.terasology.world.WorldProvider;

import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

/**
 * Class that handles the behaviour of the prediction system of a Character
 */
@RegisterSystem(RegisterMode.REMOTE_CLIENT)
public class ClientCharacterPredictionSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(ClientCharacterPredictionSystem.class);
    private static final int BUFFER_SIZE = 128;

    @In
    private Time time;

    @In
    private PhysicsEngine physics;

    @In
    private WorldProvider worldProvider;

    @In
    private LocalPlayer localPlayer;

    private CharacterMover characterMover;
    private Map<EntityRef, CircularBuffer<CharacterStateEvent>> playerStates = Maps.newHashMap();
    private Deque<CharacterMoveInputEvent> inputs = Queues.newArrayDeque();
    private CharacterStateEvent predictedState;
    private CharacterStateEvent authoritiveState;

    /* (non-Javadoc)
     * @see org.terasology.entitySystem.systems.BaseComponentSystem#initialise()
     * Initialize the new Character move
     */
    @Override
    public void initialise() {
        characterMover = new KinematicCharacterMover(worldProvider, physics);
    }

    /**
     * @param event Instance of ActivatedComponent event
     * @param entity Instance of EntityRef
     * Places te Initial state and put them into the entity
     */
    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onCreate(final OnActivatedComponent event, final EntityRef entity) {
        physics.getCharacterCollider(entity);
        CircularBuffer<CharacterStateEvent> stateBuffer = CircularBuffer.create(BUFFER_SIZE);
        stateBuffer.add(createInitialState(entity));
        playerStates.put(entity, stateBuffer);
    }

    /**
     * @param event Instace of BeforeDeactivateComponent event
     * @param entity Instance of EntityRef
     * Removes the CharacterCollider and the player states of the entity
     */
    @ReceiveEvent(components = {CharacterComponent.class, CharacterMovementComponent.class, LocationComponent.class})
    public void onDestroy(final BeforeDeactivateComponent event, final EntityRef entity) {
        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        ClientComponent controller = character.controller.getComponent(ClientComponent.class);
        if (controller != null && controller.local) {
            predictedState = null;
            authoritiveState = null;
            inputs.clear();
        }
        physics.removeCharacterCollider(entity);
        playerStates.remove(entity);
    }

    /**
     * @param state CharacterStateEvent received
     * @param entity Instance of EntityRef
     * Adds the new state received to the current Entity
     */
    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onCharacterStateReceived(CharacterStateEvent state, EntityRef entity) {
        if (entity.equals(localPlayer.getCharacterEntity())) {
            logger.trace("Received new state, sequence number: {}, buffered input size {}", state.getSequenceNumber(), inputs.size());
            playerStates.remove(entity);
            authoritiveState = state;
            Iterator<CharacterMoveInputEvent> inputIterator = inputs.iterator();
            CharacterStateEvent newState = authoritiveState;
            interateAndSetPredicted(state, entity, inputIterator, newState);
        } else {
            playerStates.get(entity).add(state);
        }
    }

	/**
	 * @param state Instance of CharacterStateEvent
	 * @param entity Instance of EntityRef
	 * @param inputIterator An interator over all the CharacterMoveInput events
	 * @param newState The new state that will be set to the predicted state
	 * Helper method that iterates over all the input Character move events, and sets the new predicted state
	 */
	private void interateAndSetPredicted(CharacterStateEvent state,
			EntityRef entity, Iterator<CharacterMoveInputEvent> inputIterator,
			CharacterStateEvent newState) {
		while (inputIterator.hasNext()) {
		    CharacterMoveInputEvent input = inputIterator.next();
		    if (input.getSequenceNumber() <= state.getSequenceNumber()) {
		        inputIterator.remove();
		    } else {
		        newState = stepState(input, newState, entity);
		    }
		}
		logger.trace("Resultant input size {}", inputs.size());
		CharacterStateEvent.setToState(entity, newState);
		// TODO: soft correct predicted state
		predictedState = newState;
	}


    /**
     * @param input CharacterMovementComponent instance
     * @param entity Instance of the EntityRef
     * Handle the input event given by a player
     */ 
    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onPlayerInput(CharacterMoveInputEvent input, EntityRef entity) {
        if (predictedState == null) {
            predictedState = createInitialState(entity);
            authoritiveState = predictedState;
            playerStates.remove(entity);
        }

        inputs.add(input);
        CharacterStateEvent newState = stepState(input, predictedState, entity);
        predictedState = newState;

        CharacterStateEvent.setToState(entity, newState);
    }

    /**
     * @param entity Instance of EntityRef
     * @return Create a new instance of a character state (Initial one) with the parameters given
     */
    private CharacterStateEvent createInitialState(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        return new CharacterStateEvent(time.getGameTimeInMs(), 0, location.getWorldPosition(), location.getWorldRotation(), new Vector3f(), 0, 0, MovementMode.WALKING, false);
    }

    /**
     * @param input Character event of Move Input instance
     * @param lastState Last state of the character event
     * @param entity Instance of EntityRef
     * @return New state according to the last state, and the new input of the Entity
     */
    private CharacterStateEvent stepState(CharacterMoveInputEvent input, CharacterStateEvent lastState, EntityRef entity) {
        return characterMover.step(lastState, input, entity);
    }

    /* (non-Javadoc)
     * @see org.terasology.entitySystem.systems.UpdateSubscriberSystem#update(float)
     * Update the dates acording to a renderTime, setting a new state to the previous or next character state event.
     */
    @Override
    public void update(float delta) {
        long renderTime = time.getGameTimeInMs() - ServerCharacterPredictionSystem.RENDER_DELAY;
        for (Map.Entry<EntityRef, CircularBuffer<CharacterStateEvent>> entry : playerStates.entrySet()) {
            CharacterStateEvent previous = null;
            CharacterStateEvent next = null;
            for (CharacterStateEvent state : entry.getValue()) {
                if (state.getTime() <= renderTime) {
                    previous = state;
                } else {
                    next = state;
                    break;
                }
            }
            settingState(renderTime, entry, previous, next);
        }
    }

	/**
	 * @param renderTime Render time used to compare the actual situation and set the proper State.
	 * @param entry A Map containing the EntityRef and the Buffer with the character state events.
	 * @param previous Character state event *previous*
	 * @param next Character state event *next*
	 */
	private void settingState(long renderTime,
			Map.Entry<EntityRef, CircularBuffer<CharacterStateEvent>> entry,
			CharacterStateEvent previous, CharacterStateEvent next) {
		if (previous != null) {
		    if (next != null) {
		        CharacterStateEvent.setToInterpolateState(entry.getKey(), previous, next, renderTime);
		    } else {
		        CharacterStateEvent.setToExtrapolateState(entry.getKey(), previous, renderTime);
		    }
		}
	}
}
