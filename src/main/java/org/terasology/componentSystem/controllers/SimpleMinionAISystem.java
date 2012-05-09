package org.terasology.componentSystem.controllers;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.LocationComponent;
import org.terasology.components.SimpleAIComponent;
import org.terasology.components.SimpleMinionAIComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.LocalPlayer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 7/05/12
 * Time: 18:25
 * To change this template use File | Settings | File Templates.
 */
public class SimpleMinionAISystem implements EventHandlerSystem, UpdateSubscriberSystem {

    private EntityManager entityManager;
    private FastRandom random = new FastRandom();
    private Timer timer;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        timer = CoreRegistry.get(Timer.class);
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(SimpleMinionAIComponent.class, CharacterMovementComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            SimpleMinionAIComponent ai = entity.getComponent(SimpleMinionAIComponent.class);
            CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);

            Vector3f worldPos = location.getWorldPosition();
            moveComp.getDrive().set(0,0,0);
            // TODO: shouldn't use local player, need some way to find nearest player
            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
            if (localPlayer != null)
            {
                if(ai.followingPlayer)
                {
                    Vector3f dist = new Vector3f(worldPos);
                    dist.sub(localPlayer.getPosition());
                    double distanceToPlayer = dist.lengthSquared();


                    if (distanceToPlayer > 8) {
                        // Head to player
                        Vector3f target = localPlayer.getPosition();
                        target.x -= 2;
                        ai.movementTarget.set(target);
                        ai.followingPlayer = true;
                        entity.saveComponent(ai);
                    }

                    Vector3f targetDirection = new Vector3f();
                    targetDirection.sub(ai.movementTarget, worldPos);
                    targetDirection.normalize();
                    moveComp.setDrive(targetDirection);

                    float yaw = (float)Math.atan2(targetDirection.x, targetDirection.z);
                    AxisAngle4f axisAngle = new AxisAngle4f(0,1,0,yaw);
                    location.getLocalRotation().set(axisAngle);
                    entity.saveComponent(moveComp);
                    entity.saveComponent(location);
                }
                else{
                    Vector3f targetDirection = new Vector3f();
                    targetDirection.sub(ai.movementTarget, worldPos);
                    targetDirection.normalize();
                    moveComp.setDrive(targetDirection);

                    float yaw = (float)Math.atan2(targetDirection.x, targetDirection.z);
                    AxisAngle4f axisAngle = new AxisAngle4f(0,1,0,yaw);
                    location.getLocalRotation().set(axisAngle);
                    entity.saveComponent(moveComp);
                    entity.saveComponent(location);
                }
            }
        }
    }

    @ReceiveEvent(components = {SimpleMinionAIComponent.class})
    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
        CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);
        if (moveComp != null && moveComp.isGrounded) {
            moveComp.jump = true;
            entity.saveComponent(moveComp);
        }
    }
}
