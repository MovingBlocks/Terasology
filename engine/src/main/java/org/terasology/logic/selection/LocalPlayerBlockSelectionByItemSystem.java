/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.selection;

import javax.vecmath.Vector3f;

import org.newdawn.slick.util.Log;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.selection.event.BlockEndSelectionEvent;
import org.terasology.logic.selection.event.BlockSelectionCompletedEvent;
import org.terasology.logic.selection.event.BlockStartSelectionEvent;
import org.terasology.math.Vector3i;
import org.terasology.physics.Physics;
import org.terasology.rendering.world.WorldRenderer;

/**
 * System to allow the use of BlockSelectionComponents. This system is a client only system, though no other player
 * will see selections done by one player.
 *
 * @author synopia
 */
@RegisterSystem(RegisterMode.CLIENT)
public class LocalPlayerBlockSelectionByItemSystem implements ComponentSystem, RenderSystem {
    @In
    private EntityManager entityManager;
    @In
    private Physics physics;
    @In
    private LocalPlayer localPlayer;

    private BlockSelectionRenderer selectionRenderer;

    private BlockSelectionComponent blockSelectionComponent;
    
    class LocalPlayerBlockSelectionByItemSystemListener implements Component {
        private EntityRef itemEntity;

		public LocalPlayerBlockSelectionByItemSystemListener(EntityRef itemEntity) {
			this.itemEntity = itemEntity;
		}

		public EntityRef getItemEntity() {
			return itemEntity;
		}
    }
    
    @ReceiveEvent(components = {BlockSelectionComponent.class})
    public void onPlaced(ActivateEvent event, EntityRef itemEntity) {
        if (event.getTargetLocation() == null) {
            return;
        }
        
        LocationComponent targetLocation = new LocationComponent();
        targetLocation.setWorldPosition(event.getTargetLocation());

        LocalPlayerBlockSelectionByItemSystemListener listener = new LocalPlayerBlockSelectionByItemSystemListener(itemEntity);
        
        EntityRef targetLocationEntity = entityManager.create(targetLocation, listener);
        
        this.blockSelectionComponent = itemEntity.getComponent(BlockSelectionComponent.class);
        
        if (null == blockSelectionComponent.startPosition) {
        	Log.debug("local-player-block-selection start at" + event.getTargetLocation());
        	targetLocationEntity.send(new BlockStartSelectionEvent(blockSelectionComponent));
        } else {
        	Log.debug("local-player-block-selection end at" + event.getTargetLocation());
        	targetLocationEntity.send(new BlockEndSelectionEvent(blockSelectionComponent));
        }
    }

    @ReceiveEvent(components = {LocalPlayerBlockSelectionByItemSystemListener.class})
    public void onBlockSelectionCompleted(BlockSelectionCompletedEvent event, EntityRef targetLocationEntity) {
    	LocalPlayerBlockSelectionByItemSystemListener listener = targetLocationEntity.getComponent(LocalPlayerBlockSelectionByItemSystemListener.class);
        localPlayer.getCharacterEntity().send(new ApplyBlockSelectionEvent(listener.getItemEntity(), event.getBlockSelectionComponent().currentSelection));
        blockSelectionComponent.currentSelection = null;
        blockSelectionComponent.startPosition = null;
    }

    @ReceiveEvent(components = {LocationComponent.class})
    public void onCamTargetChanged(CameraTargetChangedEvent event, EntityRef entity) {
    	if (null == blockSelectionComponent) {
    		return;
    	}
    	
        if (blockSelectionComponent.startPosition == null) {
            return;
        }
        EntityRef target = event.getNewTarget();
        target.send(new BlockEndSelectionEvent(blockSelectionComponent));
    }

    @Override
    public void initialise() {
        selectionRenderer = new BlockSelectionRenderer();
    }

    @Override
    public void renderOverlay() {
    	if (null == blockSelectionComponent) {
    		return;
    	}
    	
        selectionRenderer.beginRenderOverlay();
        Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();

        if (blockSelectionComponent.startPosition != null) {
            selectionRenderer.beginRenderOverlay();
            if (blockSelectionComponent.currentSelection == null) {
                selectionRenderer.renderMark(blockSelectionComponent.startPosition, cameraPosition);
            } else {
                Vector3i size = blockSelectionComponent.currentSelection.size();
                Vector3i block = new Vector3i();
                for (int z = 0; z < size.z; z++) {
                    for (int y = 0; y < size.y; y++) {
                        for (int x = 0; x < size.x; x++) {
                            block.set(x, y, z);
                            block.add(blockSelectionComponent.currentSelection.min());
                            selectionRenderer.renderMark(block, cameraPosition);
                        }
                    }
                }
            }
            selectionRenderer.endRenderOverlay();
        }

    }

    @Override
    public void renderFirstPerson() {
    }

    @Override
    public void renderShadows() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void renderOpaque() {
    }

    @Override
    public void renderAlphaBlend() {
    }
}
