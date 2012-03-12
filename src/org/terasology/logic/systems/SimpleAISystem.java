package org.terasology.logic.systems;

import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.LocationComponent;
import org.terasology.components.SimpleAIComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.componentSystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.componentSystem.UpdateSubscriberSystem;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Terasology;
import org.terasology.logic.global.LocalPlayer;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class SimpleAISystem implements EventHandlerSystem, UpdateSubscriberSystem {

    private EntityManager entityManager;
    private FastRandom random = new FastRandom();

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(SimpleAIComponent.class, CharacterMovementComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            SimpleAIComponent ai = entity.getComponent(SimpleAIComponent.class);
            CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);

            Vector3f worldPos = location.getWorldPosition();
            moveComp.getDrive().set(0,0,0);
            // TODO: shouldn't use local player
            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
            if (localPlayer != null)
            {
                Vector3f dist = new Vector3f(worldPos);
                dist.sub(localPlayer.getPosition());
                double distanceToPlayer = dist.lengthSquared();

                if (distanceToPlayer > 6 && distanceToPlayer < 16) {
                    // Head to player
                    ai.movementTarget.set(localPlayer.getPosition());
                    ai.followingPlayer = true;
                } else {
                    // Random walk
                    if (Terasology.getInstance().getTimeInMs() - ai.lastChangeOfDirectionAt > 12000 || ai.followingPlayer) {
                        ai.movementTarget.set(worldPos.x + random.randomFloat() * 500, worldPos.y, worldPos.z + random.randomFloat() * 500);
                        ai.lastChangeOfDirectionAt = Terasology.getInstance().getTimeInMs();
                        ai.followingPlayer = false;
                    }
                }

                Vector3f targetDirection = new Vector3f();
                targetDirection.sub(ai.movementTarget, worldPos);
                targetDirection.normalize();
                moveComp.setDrive(targetDirection);

                float yaw = (float)Math.atan2(targetDirection.x, targetDirection.z);
                AxisAngle4f axisAngle = new AxisAngle4f(0,1,0,yaw);
                location.getLocalRotation().set(axisAngle);
            }
        }
    }

    @ReceiveEvent(components = {SimpleAIComponent.class})
    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
        CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);
        if (moveComp != null && moveComp.isGrounded) {
            moveComp.jump = true;
        }
    }
}
