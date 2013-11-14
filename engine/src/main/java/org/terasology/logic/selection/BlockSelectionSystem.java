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

import org.terasology.engine.CoreRegistry;
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
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.physics.Physics;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;

/**
 * System to allow the use of BlockSelectionComponents. This system is a client only system, though no other player
 * will see selections done by one player.
 *
 * @author synopia
 */
@RegisterSystem(RegisterMode.CLIENT)
public class BlockSelectionSystem implements ComponentSystem, RenderSystem {
    @In
    private Physics physics;
    @In
    private LocalPlayer localPlayer;

    private Vector3i startPos;
    private Region3i currentSelection;
    private BlockSelectionRenderer selectionRenderer;

    @ReceiveEvent(components = {BlockSelectionComponent.class})
    public void onPlaced(ActivateEvent event, EntityRef itemEntity) {
        if (event.getTargetLocation() == null) {
            return;
        }
        if (startPos == null) {
            Vector3f worldPosition = event.getTargetLocation();
            startPos = new Vector3i(worldPosition.x, worldPosition.y, worldPosition.z);
            currentSelection = Region3i.createBounded(startPos, startPos);
        } else {
            localPlayer.getCharacterEntity().send(new ApplyBlockSelectionEvent(itemEntity, currentSelection));
            currentSelection = null;
            startPos = null;
        }
    }

    @ReceiveEvent(components = {LocationComponent.class})
    public void onCamTargetChanged(CameraTargetChangedEvent event, EntityRef entity) {
        if (startPos == null) {
            return;
        }
        EntityRef target = event.getNewTarget();
        LocationComponent locationComponent = target.getComponent(LocationComponent.class);
        if (locationComponent != null) {
            Vector3f worldPosition = locationComponent.getWorldPosition();
            Vector3i currentEndPos = new Vector3i(worldPosition.x, worldPosition.y, worldPosition.z);
            currentSelection = Region3i.createBounded(startPos, currentEndPos);
        }
    }

    @Override
    public void initialise() {
        selectionRenderer = new BlockSelectionRenderer();
    }

    @Override
    public void renderOverlay() {
        selectionRenderer.beginRenderOverlay();
        Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();

        if (startPos != null) {
            selectionRenderer.beginRenderOverlay();
            if (currentSelection == null) {
                selectionRenderer.renderMark(startPos, cameraPosition);
            } else {
                Vector3i size = currentSelection.size();
                Vector3i block = new Vector3i();
                for (int z = 0; z < size.z; z++) {
                    for (int y = 0; y < size.y; y++) {
                        for (int x = 0; x < size.x; x++) {
                            block.set(x, y, z);
                            block.add(currentSelection.min());
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
