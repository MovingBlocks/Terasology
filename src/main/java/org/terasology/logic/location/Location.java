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

package org.terasology.logic.location;

import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.entitySystem.event.RemovedComponentEvent;

import javax.vecmath.Vector3f;
import java.util.Iterator;

/**
 * @author Immortius
 */
@RegisterSystem
public class Location implements ComponentSystem {


    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

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

    @ReceiveEvent(components = LocationComponent.class)
    public void onDestroyed(RemovedComponentEvent event, EntityRef entity) {
        LocationComponent parentLoc = entity.getComponent(LocationComponent.class);
        if (parentLoc == null)
        if (parentLoc.parent != null) {
            removeChild(parentLoc.parent, entity);
        }
        Iterator<EntityRef> childIterator = parentLoc.getChildren().iterator();
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
