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
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.physics.bullet.BulletPhysics;
import org.terasology.utilities.collection.CircularBuffer;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import org.terasology.logic.characters.bullet.KinematicCharacterMover;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.REMOTE_CLIENT)
public class ClientCharacterPredictionSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(ClientCharacterPredictionSystem.class);
    private static final int BUFFER_SIZE = 128;

    @In
    private Time time;

    @In
    private BulletPhysics physics;

    @In
    private WorldProvider worldProvider;

    @In
    private LocalPlayer localPlayer;

    private CharacterMover characterMover;
    private Map<EntityRef, CircularBuffer<CharacterStateEvent>> playerStates = Maps.newHashMap();
    private Deque<CharacterMoveInputEvent> inputs = Queues.newArrayDeque();
    private CharacterStateEvent predictedState;
    private CharacterStateEvent authoritiveState;

    @Override
    public void initialise() {
        characterMover = new KinematicCharacterMover(worldProvider, physics);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onCreate(final OnActivatedComponent event, final EntityRef entity) {
        physics.createCollider(entity);
        CircularBuffer<CharacterStateEvent> stateBuffer = CircularBuffer.create(BUFFER_SIZE);
        stateBuffer.add(createInitialState(entity));
        playerStates.put(entity, stateBuffer);
    }

    @ReceiveEvent(components = {CharacterComponent.class, CharacterMovementComponent.class, LocationComponent.class})
    public void onDestroy(final BeforeDeactivateComponent event, final EntityRef entity) {
        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        ClientComponent controller = character.controller.getComponent(ClientComponent.class);
        if (controller != null && controller.local) {
            predictedState = null;
            authoritiveState = null;
            inputs.clear();
        }
        physics.removeCollider(entity);
        playerStates.remove(entity);
    }

    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onCharacterStateReceived(CharacterStateEvent state, EntityRef entity) {
        if (entity.equals(localPlayer.getCharacterEntity())) {
            logger.trace("Received new state, sequence number: {}, buffered input size {}", state.getSequenceNumber(), inputs.size());

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
            logger.trace("Resultant input size {}", inputs.size());
            CharacterStateEvent.setToState(entity, newState);
            // TODO: soft correct predicted state
            predictedState = newState;
        } else {
            playerStates.get(entity).add(state);
        }
    }


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

    private CharacterStateEvent createInitialState(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        return new CharacterStateEvent(time.getGameTimeInMs(), 0, location.getWorldPosition(), location.getWorldRotation(), new Vector3f(), 0, 0, MovementMode.WALKING, false);
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
                    CharacterStateEvent.setToInterpolateState(entry.getKey(), previous, next, renderTime);
                } else {
                    CharacterStateEvent.setToExtrapolateState(entry.getKey(), previous, renderTime);
                }
            }
        }
    }
}
