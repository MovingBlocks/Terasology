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

package org.terasology.logic.location;

import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import java.util.Iterator;

/**
 */
@RegisterSystem
public class Location extends BaseComponentSystem {

    /**
     * Attaches an entity to another entity. Both must have location components.
     * This method sets the child's relative offset and rotation to the parent {@link LocationComponent}
     *
     * @param parent           entity that will be the parent Location to the child
     * @param child            entity will be attached relative to the parent
     * @param offset           relative position from parent
     * @param relativeRotation relative rotation from parent
     * @deprecated This is scheduled for removal in an upcoming version
     * method will be replaced with JOML implementation {@link #attachChild(EntityRef, EntityRef, Vector3fc, Quaternionfc, float)}.
     */
    @Deprecated
    public static void attachChild(EntityRef parent, EntityRef child, Vector3f offset, Quat4f relativeRotation, float relativeScale) {
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
     * Attaches an entity to another entity. Both must have location components.
     * This method sets the child's relative offset and rotation to the parent {@link LocationComponent}
     *
     * @param parent           entity with a {@link LocationComponent}
     * @param child            entity with a {@link LocationComponent} attach to the parent
     * @param offset           relative position from parent
     * @param relativeRotation relative rotation from parent
     * @deprecated This is scheduled for removal in an upcoming version
     * method will be replaced with JOML implementation {@link #attachChild(EntityRef, EntityRef, Vector3fc, Quaternionfc)}.
     */
    @Deprecated
    public static void attachChild(EntityRef parent, EntityRef child, Vector3f offset, Quat4f relativeRotation) {
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
     **/
    public static void attachChild(EntityRef parent, EntityRef child, Vector3fc offset, Quaternionfc relativeRotation) {
        attachChild(parent, child, JomlUtil.from(offset), JomlUtil.from(relativeRotation), 1f);
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
        attachChild(parent, child, JomlUtil.from(offset), JomlUtil.from(relativeRotation), relativeScale);
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
            Vector3f oldWorldPos = childLoc.getWorldPosition();
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
            Vector3f oldWorldPos = childLoc.getWorldPosition();
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
                Vector3f oldWorldPos = childLoc.getWorldPosition();
                childLoc.parent = EntityRef.NULL;
                childLoc.setWorldPosition(oldWorldPos);
                child.saveComponent(childLoc);
            }
            childIterator.remove();
        }
    }

    @ReceiveEvent(netFilter = RegisterMode.REMOTE_CLIENT)
    public void onResyncLocation(LocationResynchEvent event, EntityRef entityRef, LocationComponent locationComponent) {
        locationComponent.setWorldPosition(JomlUtil.from(event.getPosition()));
        locationComponent.setWorldRotation(JomlUtil.from(event.getRotation()));
        entityRef.saveComponent(locationComponent);
    }
}
