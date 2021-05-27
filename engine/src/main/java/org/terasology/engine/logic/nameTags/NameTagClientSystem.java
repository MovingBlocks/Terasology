// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.nameTags;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.Location;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.logic.FloatingTextComponent;

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

    @ReceiveEvent(components = NameTagComponent.class)
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

            Location.attachChild(entity, nameTag, offset, new Quaternionf());
        }
    }

    private void destroyNameTagOf(EntityRef entity) {
        EntityRef nameTag = nameTagEntityToFloatingTextMap.remove(entity);
        if (nameTag != null) {
            nameTag.destroy();
        }
    }


    @ReceiveEvent(components = NameTagComponent.class)
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
