package org.terasology.logic.systems;

import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.LocationComponent;
import org.terasology.components.SimpleAIComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandler;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.game.Terasology;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class SimpleAISystem implements EventHandler {

    private EntityManager entityManager;
    // TODO: Should be able to get players without using the worldRenderer
    private WorldRenderer worldRenderer;
    private FastRandom random;

    public void update(float delta) {
        if (worldRenderer == null) return;
        for (EntityRef entity : entityManager.iteratorEntities(SimpleAIComponent.class, CharacterMovementComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            SimpleAIComponent ai = entity.getComponent(SimpleAIComponent.class);
            CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);

            Vector3f worldPos = location.getWorldPosition();
            moveComp.getDrive().set(0,0,0);
            if (worldRenderer.getPlayer() != null)
            {
                Vector3f dist = new Vector3f(worldPos);
                dist.sub(worldRenderer.getPlayer().getPosition());
                double distanceToPlayer = dist.lengthSquared();

                if (distanceToPlayer > 6 && distanceToPlayer < 16) {
                    // Head to player
                    ai.movementTarget.set(worldRenderer.getPlayer().getPosition());
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

    public FastRandom getRandom() {
        return random;
    }

    public void setRandom(FastRandom random) {
        this.random = random;
    }

    public WorldRenderer getWorldRenderer() {
        return worldRenderer;
    }

    public void setWorldRenderer(WorldRenderer worldRenderer) {
        this.worldRenderer = worldRenderer;
    }

    public void setEntityManager(EntityManager manager) {
        this.entityManager = manager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
