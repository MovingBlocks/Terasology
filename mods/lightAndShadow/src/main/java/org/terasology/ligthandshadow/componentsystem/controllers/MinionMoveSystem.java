package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.ligthandshadow.components.AnimationComponent;
import org.terasology.ligthandshadow.components.MinionComponent;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.logic.SkeletalMeshComponent;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

/**
 * Moves minions, that have their targetBlock property set.
 *
 * @author synopia
 */
@RegisterComponentSystem
public class MinionMoveSystem implements EventHandlerSystem, UpdateSubscriberSystem {
    private EntityManager entityManager;

    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(MinionComponent.class, LocationComponent.class)) {
            MinionComponent minion = entity.getComponent(MinionComponent.class);
            AnimationComponent animcomp = entity.getComponent(AnimationComponent.class);
            if( minion.dead ) {
                changeAnimation(entity, animcomp.dieAnim, false);
            } else {
                if( minion.targetBlock!=null ) {
                    Vector3f blockPosition = new Vector3f(minion.targetBlock.x, minion.targetBlock.y+0.5f, minion.targetBlock.z);
                    if( setMovement(blockPosition, entity) ) {
                        minion.targetBlock = null;
                        entity.saveComponent(minion);
                        changeAnimation(entity, animcomp.idleAnim, true);
                    } else {
                        changeAnimation(entity, animcomp.walkAnim, true);
                    }
                }
            }
        }
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = { MinionComponent.class })
    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
        CharacterMovementComponent moveComp = entity
                .getComponent(CharacterMovementComponent.class);
        if ((moveComp != null) && (moveComp.isGrounded)) {
            moveComp.jump = true;
            entity.saveComponent(moveComp);
        }
    }

    private void changeAnimation(EntityRef entity, MeshAnimation animation,
                                 boolean loop) {
        SkeletalMeshComponent skeletalcomp = entity.getComponent(SkeletalMeshComponent.class);
        if (skeletalcomp.animation != animation) {
            skeletalcomp.animation = animation;
            skeletalcomp.loop = loop;
            entity.saveComponent(skeletalcomp);
        }
    }

    private boolean setMovement(Vector3f currentTarget, EntityRef entity) {
        LocationComponent location = entity
                .getComponent(LocationComponent.class);
        CharacterMovementComponent moveComp = entity
                .getComponent(CharacterMovementComponent.class);
        SkeletalMeshComponent skeletalcomp = entity
                .getComponent(SkeletalMeshComponent.class);
        Vector3f worldPos = new Vector3f(location.getWorldPosition());

        Vector3f dist = new Vector3f(worldPos);
        dist.sub(currentTarget);

        Vector3f targetDirection = new Vector3f();
        targetDirection.sub(currentTarget, worldPos);
        boolean finished;
        if (targetDirection.x * targetDirection.x + targetDirection.z
                * targetDirection.z > 0.01f) {

            targetDirection.normalize();
            moveComp.setDrive(targetDirection);

            float yaw = (float) Math
                    .atan2(targetDirection.x, targetDirection.z);
            AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
            location.getLocalRotation().set(axisAngle);
            finished = false;
        } else {
            moveComp.setDrive(new Vector3f());
            finished = true;
        }
        entity.saveComponent(moveComp);
        entity.saveComponent(location);

        return finished;
    }

}
