/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.characters;

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.AUTHORITY)
public class GazeAuthoritySystem extends BaseComponentSystem {
    @In
    EntityManager entityManager;

    @ReceiveEvent
    public void ensureGazeContainerEntitiesCreated(OnActivatedComponent event, EntityRef entityRef, GazeMountPointComponent gazeMountPointComponent, LocationComponent locationComponent) {
        if (!gazeMountPointComponent.gazeContainer.exists()) {
            gazeMountPointComponent.gazeContainer = createGazeContainer();
            entityRef.saveComponent(gazeMountPointComponent);
        }
        Location.attachChild(entityRef, gazeMountPointComponent.gazeContainer, gazeMountPointComponent.translate, new Quat4f(Quat4f.IDENTITY));
    }

    private EntityRef createGazeContainer() {
        EntityRef gaze = createGaze();
        EntityBuilder gazeContainerBuilder = entityManager.newBuilder("engine:gazeContainer");
        GazeComponent gazeComponent = gazeContainerBuilder.getComponent(GazeComponent.class);
        gazeComponent.gazeEntity = gaze;
        EntityRef gazeContainer = gazeContainerBuilder.build();
        Location.attachChild(gazeContainer, gaze, Vector3f.zero(), new Quat4f(Quat4f.IDENTITY));
        return gazeContainer;
    }

    private EntityRef createGaze() {
        EntityBuilder gaze = entityManager.newBuilder("engine:gaze");
        return gaze.build();
    }

    /**
     * Traverses the gaze mount point to the actual entity that has the location component with look information.  At very least, the character entity will be returned.
     *
     * @param character
     * @return
     */
    public static EntityRef getGazeEntityForCharacter(EntityRef character) {
        GazeMountPointComponent gazeMountPointComponent = character.getComponent(GazeMountPointComponent.class);
        if (gazeMountPointComponent != null && gazeMountPointComponent.gazeContainer.exists()) {
            return gazeMountPointComponent.gazeContainer.getComponent(GazeComponent.class).gazeEntity;
        }
        return character;
    }
}
