package org.terasology.miniion.componentsystem.controllers;

import org.terasology.componentSystem.*;
import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;
import org.terasology.miniion.components.ZoneSelectionComponent;

@RegisterComponentSystem(headedOnly = true)
public class ZoneSystem implements UpdateSubscriberSystem, RenderSystem {

	private EntityManager entityManager;

	@Override
	public void initialise() {
		entityManager = CoreRegistry.get(EntityManager.class);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void update(float delta) {

	}

	@Override
	public void renderTransparent() {

		for (EntityRef entity : entityManager
				.iteratorEntities(ZoneSelectionComponent.class)) {
			ZoneSelectionComponent selection = entity
					.getComponent(ZoneSelectionComponent.class);
			selection.blockGrid.render();
			// entity.saveComponent(selection); // deserialization error tracing
		}

	}

	@Override
	public void renderOpaque() {
	}

	@Override
	public void renderOverlay() {
	}

	@Override
	public void renderFirstPerson() {

	}
}