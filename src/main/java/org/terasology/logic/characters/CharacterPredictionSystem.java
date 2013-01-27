package org.terasology.logic.characters;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.Timer;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.CollisionGroup;
import org.terasology.utilities.collection.CircularBuffer;
import org.terasology.world.WorldProvider;

import javax.vecmath.Vector3f;
import java.util.Map;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class CharacterPredictionSystem implements EventHandlerSystem{
    private static final int BUFFER_SIZE = 256;

    @In
    private Timer timer;

    @In
    private BulletPhysics physics;

    @In
    private WorldProvider worldProvider;

    private CharacterMovementSystem characterMovementSystem;
    private Map<EntityRef, CircularBuffer<CharacterState>> playerStates = Maps.newHashMap();


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

        CircularBuffer<CharacterState> stateBuffer = CircularBuffer.create(BUFFER_SIZE);
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

    @Override
    public void initialise() {
        characterMovementSystem = new BulletCharacterMovementSystem(worldProvider);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterMovementComponent.class, LocationComponent.class})
    public void onPlayerInput(CharacterMoveInputEvent input, EntityRef entity) {
        CircularBuffer<CharacterState> stateBuffer = playerStates.get(entity);
        if (stateBuffer == null) {
            stateBuffer = CircularBuffer.create(BUFFER_SIZE);
            stateBuffer.add(createInitialState(entity));
            playerStates.put(entity, stateBuffer);
        }
        CharacterState lastState = stateBuffer.getLast();
        CharacterState newState = stepState(input, lastState, entity);
        stateBuffer.add(newState);

        setToState(entity, newState);
    }

    private CharacterState createInitialState(EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        return new CharacterState(timer.getTimeInMs(), location.getWorldPosition(), location.getWorldRotation(), new Vector3f(), MovementMode.WALKING, false);
    }

    private CharacterState stepState(CharacterMoveInputEvent input, CharacterState lastState, EntityRef entity) {
        return characterMovementSystem.step(lastState, input, entity);
    }

    private void setToState(EntityRef entity, CharacterState state) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        location.setWorldPosition(state.getPosition());
        location.setWorldRotation(state.getRotation());
        entity.saveComponent(location);
        CharacterMovementComponent movementComp = entity.getComponent(CharacterMovementComponent.class);
        movementComp.mode = state.getMode();
        movementComp.setVelocity(state.getVelocity());
        movementComp.grounded = state.isGrounded();
        entity.saveComponent(movementComp);
    }
}
