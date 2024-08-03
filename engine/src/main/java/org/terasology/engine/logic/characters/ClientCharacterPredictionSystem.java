// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.characters;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.physics.engine.PhysicsEngine;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.collection.CircularBuffer;
import org.terasology.engine.world.WorldProvider;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

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
    private CharacterMovementSystemUtility characterMovementSystemUtility;

    @Override
    public void initialise() {
        characterMover = new KinematicCharacterMover(worldProvider, physics);
        characterMovementSystemUtility = new CharacterMovementSystemUtility(physics);
    }

    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class, AliveCharacterComponent.class})
    public void onCreate(final OnActivatedComponent event, final EntityRef entity) {
        physics.getCharacterCollider(entity);
        CircularBuffer<CharacterStateEvent> stateBuffer = CircularBuffer.create(BUFFER_SIZE);
        stateBuffer.add(createInitialState(entity));
        playerStates.put(entity, stateBuffer);
    }

    @ReceiveEvent(components = {CharacterComponent.class, CharacterMovementComponent.class, LocationComponent.class, AliveCharacterComponent.class})
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

    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class, AliveCharacterComponent.class})
    public void onCharacterStateReceived(CharacterStateEvent state, EntityRef entity) {
        if (entity.equals(localPlayer.getCharacterEntity())) {
            logger.trace("Received new state, sequence number: {}, buffered input size {}", state.getSequenceNumber(), inputs.size()); //NOPMD

            playerStates.remove(entity);
            authoritiveState = state;
            Iterator<CharacterMoveInputEvent> inputIterator = inputs.iterator();
            CharacterStateEvent newState = authoritiveState;
            while (inputIterator.hasNext()) {
                CharacterMoveInputEvent input = inputIterator.next();
                if (input.getSequenceNumber() <= state.getSequenceNumber()) {
                    inputIterator.remove();
                } else {
                    newState = stepState(input, newState, entity);
                }
            }
            logger.trace("Resultant input size {}", inputs.size()); //NOPMD
            characterMovementSystemUtility.setToState(entity, newState);
            // TODO: soft correct predicted state
            predictedState = newState;
        } else {
            playerStates.get(entity).add(state);
        }
    }


    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class, AliveCharacterComponent.class})
    public void onPlayerInput(CharacterMoveInputEvent input, EntityRef entity) {
        if (predictedState == null) {
            predictedState = createInitialState(entity);
            authoritiveState = predictedState;
            playerStates.remove(entity);
        }

        inputs.add(input);
        CharacterStateEvent newState = stepState(input, predictedState, entity);
        predictedState = newState;

        characterMovementSystemUtility.setToState(entity, newState);
    }

    private CharacterStateEvent createInitialState(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        return new CharacterStateEvent(time.getGameTimeInMs(), 0,
                location.getWorldPosition(new Vector3f()), location.getWorldRotation(new Quaternionf()),
                new Vector3f(), 0, 0, MovementMode.WALKING, false);
    }

    private CharacterStateEvent stepState(CharacterMoveInputEvent input, CharacterStateEvent lastState, EntityRef entity) {
        return characterMover.step(lastState, input, entity);
    }

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
            if (previous != null) {
                if (next != null) {
                    characterMovementSystemUtility.setToInterpolateState(entry.getKey(), previous, next, renderTime);
                } else {
                    characterMovementSystemUtility.setToExtrapolateState(entry.getKey(), previous, renderTime);
                }
            }
        }
    }
}
