// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.location;

import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.NetFilterEvent;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.util.Iterator;

@RegisterSystem
public class Location extends BaseComponentSystem {

    /**
     * Attaches an entity to another entity. Both must have location components.
     * This method sets the child's relative offset and rotation to the parent {@link LocationComponent}
     *
     * @param parent           entity with a {@link LocationComponent}
     * @param child            entity with a {@link LocationComponent} attach to the parent
     * @param offset           relative position from parent
     * @param relativeRotation relative rotation from parent
     **/
    public static void attachChild(EntityRef parent, EntityRef child, Vector3fc offset, Quaternionfc relativeRotation) {
        attachChild(parent, child, offset, relativeRotation, 1f);
    }

    /**
     * Attaches an entity to another entity. Both must have location components.
     * This method sets the child's relative offset and rotation to the parent {@link LocationComponent}
     *
     * @param parent           entity with a {@link LocationComponent}
     * @param child            entity with a {@link LocationComponent} attach to the parent
     * @param offset           relative position from parent
     * @param relativeRotation relative rotation from parent
     * @param relativeScale    relative scale from parent
     **/
    public static void attachChild(EntityRef parent, EntityRef child, Vector3fc offset, Quaternionfc relativeRotation, float relativeScale) {
        LocationComponent childLoc = child.getComponent(LocationComponent.class);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (childLoc != null && parentLoc != null && !childLoc.getParent().equals(parent)) {
            LocationComponent oldParentLoc = childLoc.getParent().getComponent(LocationComponent.class);
            if (oldParentLoc != null) {
                oldParentLoc.children.remove(child);
                childLoc.getParent().saveComponent(oldParentLoc);
            }
            childLoc.parent = parent;
            childLoc.setLocalPosition(offset);
            childLoc.setLocalRotation(relativeRotation);
            childLoc.setLocalScale(relativeScale);
            parentLoc.children.add(child);
            child.saveComponent(childLoc);
            parent.saveComponent(parentLoc);
        }
    }

    /**
     * Attaches an entity to another entity. Both must have location components. The child maintains its previous position
     * and rotation but follows the parent.
     *
     * @param parent
     * @param child
     */
    public static void attachChild(EntityRef parent, EntityRef child) {
        LocationComponent childLoc = child.getComponent(LocationComponent.class);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (childLoc != null && parentLoc != null && !childLoc.getParent().equals(parent)) {
            Vector3f oldWorldPos = childLoc.getWorldPosition(new Vector3f());
            LocationComponent oldParentLoc = childLoc.getParent().getComponent(LocationComponent.class);
            if (oldParentLoc != null) {
                oldParentLoc.children.remove(child);
                childLoc.getParent().saveComponent(oldParentLoc);
            }
            childLoc.parent = parent;
            childLoc.setWorldPosition(oldWorldPos);
            parentLoc.children.add(child);
            child.saveComponent(childLoc);
            parent.saveComponent(parentLoc);
        }
    }

    public static void removeChild(EntityRef parent, EntityRef child) {
        LocationComponent childLoc = child.getComponent(LocationComponent.class);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (childLoc != null && parentLoc != null && childLoc.getParent().equals(parent)) {
            Vector3f oldWorldPos = childLoc.getWorldPosition(new Vector3f());
            parentLoc.children.remove(child);
            childLoc.parent = EntityRef.NULL;
            childLoc.setWorldPosition(oldWorldPos);
            child.saveComponent(childLoc);
            parent.saveComponent(parentLoc);
        }
    }

    @ReceiveEvent
    public void onDestroyed(BeforeRemoveComponent event, EntityRef entity, LocationComponent location) {
        if (location.parent.exists()) {
            removeChild(location.parent, entity);
        }
        Iterator<EntityRef> childIterator = location.getChildren().iterator();
        while (childIterator.hasNext()) {
            EntityRef child = childIterator.next();
            LocationComponent childLoc = child.getComponent(LocationComponent.class);
            if (childLoc != null) {
                Vector3f oldWorldPos = childLoc.getWorldPosition(new Vector3f());
                childLoc.parent = EntityRef.NULL;
                childLoc.setWorldPosition(oldWorldPos);
                child.saveComponent(childLoc);
            }
            childIterator.remove();
        }
    }

    @NetFilterEvent(netFilter = RegisterMode.REMOTE_CLIENT)
    @ReceiveEvent
    public void onResyncLocation(LocationResynchEvent event, EntityRef entityRef, LocationComponent locationComponent) {
        locationComponent.setWorldPosition(event.getPosition());
        locationComponent.setWorldRotation(event.getRotation());
        entityRef.saveComponent(locationComponent);
    }
}
