/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.nameTags;

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.rendering.logic.FloatingTextComponent;

import java.util.HashMap;
import java.util.Map;


@RegisterSystem(RegisterMode.CLIENT)
public class NameTagClientSystem extends BaseComponentSystem {

    private Map<EntityRef, EntityRef> nameTagEntityToFloatingTextMap = new HashMap<>();

    @In
    private EntityManager entityManager;


    @ReceiveEvent(components = {NameTagComponent.class, LocationComponent.class})
    public void onNameTagOwnerActivated(OnActivatedComponent event, EntityRef entity,
                                    NameTagComponent nameTagComponent) {
        createOrUpdateNameTagFor(entity, nameTagComponent);
    }

    @ReceiveEvent(components = {NameTagComponent.class })
    public void onDisplayNameChange(OnChangedComponent event, EntityRef entity,
                                    NameTagComponent nameTagComponent) {
        createOrUpdateNameTagFor(entity, nameTagComponent);
    }


    private void createOrUpdateNameTagFor(EntityRef entity, NameTagComponent nameTagComponent) {
        EntityRef nameTag = nameTagEntityToFloatingTextMap.get(entity);
        Vector3f offset = new Vector3f(0, nameTagComponent.yOffset, 0);
        if (nameTag != null) {
            FloatingTextComponent floatingText = nameTag.getComponent(FloatingTextComponent.class);
            floatingText.text = nameTagComponent.text;
            floatingText.textColor = nameTagComponent.textColor;
            floatingText.scale = nameTagComponent.scale;
            nameTag.saveComponent(floatingText);
            LocationComponent nameTagLoc = nameTag.getComponent(LocationComponent.class);
            nameTagLoc.setLocalPosition(offset);
            nameTag.saveComponent(nameTagLoc);
        } else {
            EntityBuilder nameTagBuilder = entityManager.newBuilder();
            FloatingTextComponent floatingTextComponent = new FloatingTextComponent();
            nameTagBuilder.addComponent(floatingTextComponent);
            LocationComponent locationComponent = new LocationComponent();
            nameTagBuilder.addComponent(locationComponent);
            floatingTextComponent.text = nameTagComponent.text;
            floatingTextComponent.textColor = nameTagComponent.textColor;
            floatingTextComponent.scale = nameTagComponent.scale;
            nameTagBuilder.setOwner(entity);
            nameTagBuilder.setPersistent(false);

            nameTag = nameTagBuilder.build();
            nameTagEntityToFloatingTextMap.put(entity, nameTag);

            Location.attachChild(entity, nameTag, offset, new Quat4f(1, 0, 0, 0));
        }
    }

    private void destroyNameTagOf(EntityRef entity) {
        EntityRef nameTag = nameTagEntityToFloatingTextMap.remove(entity);
        if (nameTag != null) {
            nameTag.destroy();
        }
    }


    @ReceiveEvent(components = {NameTagComponent.class })
    public void onNameTagOwnerRemoved(BeforeDeactivateComponent event, EntityRef entity) {
        destroyNameTagOf(entity);
    }

    @Override
    public void shutdown() {
        /* Explicitly no deletion of name tag entities as some system might not be in the right state anymore.
         * Since they aren't persistent it does not make any difference anyway.
         */
        nameTagEntityToFloatingTextMap.clear();
    }
}
