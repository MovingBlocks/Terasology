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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Iterator;

/**
 * @author Immortius
 */
@RegisterSystem
public class Location extends BaseComponentSystem {

    /**
     * Attaches an entity to another entity. Both must have location components.
     * This method sets the child's relative offset and rotation to the
     *
     * @param parent
     * @param child
     * @param offset
     * @param relativeRotation
     */
    public static void attachChild(EntityRef parent, EntityRef child, Vector3f offset, Quat4f relativeRotation) {
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
}
