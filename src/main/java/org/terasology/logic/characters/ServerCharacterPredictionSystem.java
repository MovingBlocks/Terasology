package org.terasology.logic.characters;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.Timer;
import org.terasology.logic.players.LocalPlayer;
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
public class ServerCharacterPredictionSystem implements UpdateSubscriberSystem {
    private static final int BUFFER_SIZE = 128;
    private static final int TIME_BETWEEN_STATE_REPLICATE = 50;
    public static final int RENDER_DELAY = 100;

    @In
    private Timer timer;

    @In
    private BulletPhysics physics;

    @In
    private WorldProvider worldProvider;

    @In
    private LocalPlayer localPlayer;

    private CharacterMovementSystem characterMovementSystem;
    private Map<EntityRef, CircularBuffer<CharacterStateEvent>> playerStates = Maps.newHashMap();
    private long nextSendState;

    @Override
    public void initialise() {
        characterMovementSystem = new BulletCharacterMovementSystem(worldProvider);
        nextSendState = timer.getTimeInMs() + TIME_BETWEEN_STATE_REPLICATE;
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onCreate(final AddComponentEvent event, final EntityRef entity) {
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
        playerStates.put(entity, stateBuffer);
    }

    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onDestroy(final RemovedComponentEvent event, final EntityRef entity) {
        CharacterMovementComponent comp = entity.getComponent(CharacterMovementComponent.class);
        if (comp.collider != null) {
            physics.removeCollider(comp.collider);
        }
        playerStates.remove(entity);
    }



    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onPlayerInput(CharacterMoveInputEvent input, EntityRef entity) {
        CircularBuffer<CharacterStateEvent> stateBuffer = playerStates.get(entity);

        CharacterStateEvent lastState = stateBuffer.getLast();
        CharacterStateEvent newState = stepState(input, lastState, entity);
        stateBuffer.add(newState);

        CharacterStateEvent.setToState(entity, newState);
    }

    private CharacterStateEvent createInitialState(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        return new CharacterStateEvent(timer.getTimeInMs(), 0, location.getWorldPosition(), location.getWorldRotation(), new Vector3f(), MovementMode.WALKING, false);
    }

    private CharacterStateEvent stepState(CharacterMoveInputEvent input, CharacterStateEvent lastState, EntityRef entity) {
        return characterMovementSystem.step(lastState, input, entity);
    }

    @Override
    public void update(float delta) {
        if (nextSendState < timer.getTimeInMs()) {
            for (Map.Entry<EntityRef, CircularBuffer<CharacterStateEvent>> entry : playerStates.entrySet()) {
                if (entry.getValue().size() > 0) {
                    entry.getKey().send(entry.getValue().getLast());
                }
            }
            nextSendState += TIME_BETWEEN_STATE_REPLICATE;
        }
        long renderTime = timer.getTimeInMs() - RENDER_DELAY;
        for (Map.Entry<EntityRef, CircularBuffer<CharacterStateEvent>> entry : playerStates.entrySet()) {
            if (entry.getKey().equals(localPlayer.getCharacterEntity())) {
                continue;
            }

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
