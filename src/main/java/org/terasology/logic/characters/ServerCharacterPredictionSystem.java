/*
 * Copyright 2013 Moving Blocks
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

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.Share;
import org.terasology.logic.characters.bullet.BulletCharacterMover;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.CollisionGroup;
import org.terasology.utilities.collection.CircularBuffer;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;
import java.util.Map;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(PredictionSystem.class)
public class ServerCharacterPredictionSystem implements UpdateSubscriberSystem, PredictionSystem {
    private static final Logger logger = LoggerFactory.getLogger(ServerCharacterPredictionSystem.class);

    private static final int BUFFER_SIZE = 128;
    private static final int TIME_BETWEEN_STATE_REPLICATE = 50;
    public static final int RENDER_DELAY = 100;

    public static final int MAX_INPUT_OVERFLOW = 100;
    public static final int MAX_INPUT_UNDERFLOW = 100;

    @In
    private Time time;

    @In
    private BulletPhysics physics;

    @In
    private WorldProvider worldProvider;

    @In
    private LocalPlayer localPlayer;

    @In
    private NetworkSystem networkSystem;

    private CharacterMover characterMover;
    private Map<EntityRef, CircularBuffer<CharacterStateEvent>> characterStates = Maps.newHashMap();
    private Map<EntityRef, CharacterMoveInputEvent> lastInputEvent = Maps.newHashMap();
    private long nextSendState;

    @Override
    public void initialise() {
        characterMover = new BulletCharacterMover(worldProvider);
        nextSendState = time.getGameTimeInMs() + TIME_BETWEEN_STATE_REPLICATE;
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onCreate(final OnActivatedComponent event, final EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        CharacterMovementComponent movementComp = entity.getComponent(CharacterMovementComponent.class);
        float height = (movementComp.height - 2 * movementComp.radius) * location.getWorldScale();
        float width = movementComp.radius * location.getWorldScale();
        ConvexShape capsule = new CapsuleShape(width, height);
        capsule.setMargin(0.1f);
        movementComp.collider = physics.createCollider(location.getWorldPosition(), capsule, Lists.<CollisionGroup>newArrayList(movementComp.collisionGroup), movementComp.collidesWith, CollisionFlags.CHARACTER_OBJECT);
        movementComp.collider.setUserPointer(entity);

        CircularBuffer<CharacterStateEvent> stateBuffer = CircularBuffer.create(BUFFER_SIZE);
        stateBuffer.add(createInitialState(entity));
        characterStates.put(entity, stateBuffer);
    }

    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onDestroy(final BeforeDeactivateComponent event, final EntityRef entity) {
        CharacterMovementComponent comp = entity.getComponent(CharacterMovementComponent.class);
        if (comp.collider != null) {
            physics.removeCollider(comp.collider);
        }
        characterStates.remove(entity);
        lastInputEvent.remove(entity);
    }

    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onPlayerInput(CharacterMoveInputEvent input, EntityRef entity) {
        CircularBuffer<CharacterStateEvent> stateBuffer = characterStates.get(entity);
        CharacterStateEvent lastState = stateBuffer.getLast();
        if (input.getDelta() + lastState.getTime() < time.getGameTimeInMs() + MAX_INPUT_OVERFLOW) {
            CharacterStateEvent newState = stepState(input, lastState, entity);
            stateBuffer.add(newState);

            CharacterStateEvent.setToState(entity, newState);
            lastInputEvent.put(entity, input);
        } else {
            logger.warn("Received too much input from {}, dropping input.", entity);
        }
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
        if (nextSendState < time.getGameTimeInMs()) {
            long lastSendTime = nextSendState - TIME_BETWEEN_STATE_REPLICATE;
            for (Map.Entry<EntityRef, CircularBuffer<CharacterStateEvent>> entry : characterStates.entrySet()) {
                if (entry.getValue().size() > 0) {
                    CharacterStateEvent state = entry.getValue().getLast();
                    if (state.getTime() >= lastSendTime) {
                        entry.getKey().send(state);
                    } else if (time.getGameTimeInMs() - state.getTime() > MAX_INPUT_UNDERFLOW) {
                        // Haven't received input in a while, repeat last input
                        CharacterMoveInputEvent lastInput = lastInputEvent.get(entry.getKey());
                        if (lastInput != null) {
                            CharacterMoveInputEvent newInput = new CharacterMoveInputEvent(lastInput, (int) (time.getGameTimeInMs() - state.getTime()));
                            onPlayerInput(newInput, entry.getKey());
                        }
                        entry.getKey().send(state);
                    }
                }
            }
            nextSendState += TIME_BETWEEN_STATE_REPLICATE;
        }
        long renderTime = time.getGameTimeInMs() - RENDER_DELAY;
        for (Map.Entry<EntityRef, CircularBuffer<CharacterStateEvent>> entry : characterStates.entrySet()) {
            if (entry.getKey().equals(localPlayer.getCharacterEntity())) {
                continue;
            }

            setToTime(renderTime, entry.getKey(), entry.getValue());
        }
    }

    private void setToTime(long renderTime, EntityRef entity, CircularBuffer<CharacterStateEvent> buffer) {
        CharacterStateEvent previous = null;
        CharacterStateEvent next = null;
        for (CharacterStateEvent state : buffer) {
            if (state.getTime() <= renderTime) {
                previous = state;
            } else {
                next = state;
                break;
            }
        }
        if (previous != null) {
            if (next != null) {
                CharacterStateEvent.setToInterpolateState(entity, previous, next, renderTime);
            } else {
                CharacterStateEvent.setToExtrapolateState(entity, previous, renderTime);
            }
        }
    }

    @Override
    public void lagCompensate(EntityRef client, long timeMs) {
        for (Map.Entry<EntityRef, CircularBuffer<CharacterStateEvent>> entry : characterStates.entrySet()) {
            if (networkSystem.getOwnerEntity(entry.getKey()).equals(client)) {
                CharacterStateEvent.setToState(entry.getKey(), entry.getValue().getLast());
            } else {
                setToTime(timeMs - RENDER_DELAY, entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void restoreToPresent() {
        long renderTime = time.getGameTimeInMs() - RENDER_DELAY;
        for (Map.Entry<EntityRef, CircularBuffer<CharacterStateEvent>> entry : characterStates.entrySet()) {
            setToTime(renderTime, entry.getKey(), entry.getValue());
        }
    }
}
