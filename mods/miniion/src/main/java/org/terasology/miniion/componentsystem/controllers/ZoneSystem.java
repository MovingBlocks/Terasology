package org.terasology.miniion.componentsystem.controllers;

import org.terasology.componentSystem.*;
import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.ZoneSelectionComponent;

@RegisterComponentSystem(headedOnly = true)
public class ZoneSystem implements UpdateSubscriberSystem, RenderSystem {

    private EntityManager entityManager;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {
    }

    public void update(float delta) {
        
    }

    public void renderTransparent() {

        for (EntityRef entity : entityManager.iteratorEntities(ZoneSelectionComponent.class)) {
        	ZoneSelectionComponent selection = entity.getComponent(ZoneSelectionComponent.class);        	
        	selection.blockGrid.render();
            //entity.saveComponent(selection); // deserialization error tracing 
        }

    }

    public void renderOpaque() {
    }

    public void renderOverlay() {
    }

    public void renderFirstPerson() {

    }
}