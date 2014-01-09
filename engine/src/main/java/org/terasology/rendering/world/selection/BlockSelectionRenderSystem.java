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
package org.terasology.rendering.world.selection;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.selection.BlockSelectionComponent;

/**
 * System to render registered BlockSelections.
 * 
 * This system is not currently thread-safe.
 * 
 * @author synopia mkienenb@gmail.com
 */
@RegisterSystem(RegisterMode.CLIENT)
public class BlockSelectionRenderSystem implements RenderSystem {
    @In
    private EntityManager entityManager;

    /**
     * This map will contain one reusable selection renderer per texture width/height pair.
     * This should be a reasonable compromise between no caching and caching too many renderers.
     * While it is possible that the number of cached renderers could grow out of control over time,
     * in practice most textures should be a standard size.
     */
    private Map<Vector2i, BlockSelectionRenderer> cachedBlockSelectionRendererByTextureDimensionsMap = new HashMap<Vector2i, BlockSelectionRenderer>();

    @Override
    public void renderOverlay() {
        for (EntityRef entity : entityManager.getEntitiesWith(BlockSelectionComponent.class)) {
            BlockSelectionComponent blockSelectionComponent = entity.getComponent(BlockSelectionComponent.class);
            if (blockSelectionComponent.shouldRender) {
                Texture texture = blockSelectionComponent.texture;
                if (null == texture) {
                    texture = Assets.getTexture("engine:selection");
                }

                Vector2i textureDimensions = new Vector2i(texture.getWidth(), texture.getHeight());
                BlockSelectionRenderer selectionRenderer = cachedBlockSelectionRendererByTextureDimensionsMap.get(textureDimensions);
                if (null == selectionRenderer) {
                    selectionRenderer = new BlockSelectionRenderer(texture);
                    cachedBlockSelectionRendererByTextureDimensionsMap.put(textureDimensions, selectionRenderer);
                } else {
                    selectionRenderer.setEffectsTexture(texture);
                }

                renderOverlayForOneBlockSelection(blockSelectionComponent, selectionRenderer);
            }
        }
    }

    private void renderOverlayForOneBlockSelection(BlockSelectionComponent blockSelectionComponent,
                                                   BlockSelectionRenderer selectionRenderer) {
        // TODO: why is there a second beginRenderOverlay() call here at the start with no matching endRenderOverlay()?
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
