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

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.vecmath.Vector3f;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.logic.selection.event.RegisterBlockSelectionForRenderingEvent;
import org.terasology.logic.selection.event.UnregisterBlockSelectionForRenderingEvent;
import org.terasology.math.Vector3i;
import org.terasology.rendering.world.WorldRenderer;

/**
 * System to render registered BlockSelections.
 * 
 * This system is not currently thread-safe.
 * 
 * @author synopia mkienenb@gmail.com
 */
@RegisterSystem(RegisterMode.CLIENT)
public class BlockSelectionRenderSystem implements RenderSystem {

    private Map<BlockSelectionComponent, BlockSelectionRenderer> registeredBlockSelectionComponents = new HashMap<BlockSelectionComponent, BlockSelectionRenderer>();

    private Color transparent(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 200);
    }
    
    private int colorIndex = -1;
    private Color[] SELECTION_COLORS = new Color[] {
        transparent(Color.YELLOW),
        transparent(Color.BLUE),
        transparent(Color.GREEN),
        transparent(Color.RED),
        transparent(Color.CYAN),
        transparent(Color.ORANGE),
        transparent(Color.MAGENTA)
    };
    
    private Color getNextColor() {
        colorIndex++;
        if (colorIndex >= SELECTION_COLORS.length) {
            colorIndex = 0;
        }
        return SELECTION_COLORS[colorIndex];
    }
    
    @ReceiveEvent()
    public void onRegisterBlockSelection(RegisterBlockSelectionForRenderingEvent event, EntityRef itemEntity) {
        // A future improvement might be to add different rendering options for different kinds of block selections
        BlockSelectionRenderer selectionRenderer = new BlockSelectionRenderer(getNextColor());
        BlockSelectionComponent blockSelectionComponent = event.getBlockSelectionComponent();
        
        if (!registeredBlockSelectionComponents.containsKey(blockSelectionComponent)) {
            // At some point, we might need to verify that both key and value are non-null
            registeredBlockSelectionComponents.put(blockSelectionComponent, selectionRenderer);
        }
    }

    @ReceiveEvent()
    public void onUnregisterBlockSelection(UnregisterBlockSelectionForRenderingEvent event, EntityRef itemEntity) {
        BlockSelectionComponent blockSelectionComponent = event.getBlockSelectionComponent();

        // TODO: do we need to render anything at this point to void the current selection?
        // BlockSelectionRenderer selectionRenderer =
        registeredBlockSelectionComponents.remove(blockSelectionComponent);
        // if (null != selectionRenderer) {
        //     renderOverlayForOneBlockSelection(blockSelectionComponent, selectionRenderer);
        // }
    }

    @Override
    public void renderOverlay() {
        for (Entry<BlockSelectionComponent, BlockSelectionRenderer> entry : registeredBlockSelectionComponents.entrySet()) {
            BlockSelectionComponent blockSelectionComponent = entry.getKey();
            BlockSelectionRenderer selectionRenderer = entry.getValue();
            renderOverlayForOneBlockSelection(blockSelectionComponent, selectionRenderer);
        }
    }

    private void renderOverlayForOneBlockSelection(BlockSelectionComponent blockSelectionComponent,
                                                   BlockSelectionRenderer selectionRenderer) {
        // TODO: why is there a second beginRenderOverlay here at the start with no matching endRenderOverlay?
        selectionRenderer.beginRenderOverlay();
        Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();

        if (blockSelectionComponent.startPosition != null) {
            selectionRenderer.beginRenderOverlay();
            if (blockSelectionComponent.currentSelection == null) {
                selectionRenderer.renderMark( blockSelectionComponent.startPosition, cameraPosition);
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
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void renderFirstPerson() {
    }

    @Override
    public void renderShadows() {
    }

    @Override
    public void renderOpaque() {
    }

    @Override
    public void renderAlphaBlend() {
    }
}
