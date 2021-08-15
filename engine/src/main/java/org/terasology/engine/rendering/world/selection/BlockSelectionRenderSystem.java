// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world.selection;

import org.joml.Vector2i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.RenderSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.selection.BlockSelectionComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * System to render registered BlockSelections.
 * <br><br>
 * This system is not currently thread-safe.
 *
 */
@RegisterSystem(RegisterMode.CLIENT)
public class BlockSelectionRenderSystem extends BaseComponentSystem implements RenderSystem {
    @In
    private EntityManager entityManager;

    /**
     * This map will contain one reusable selection renderer per texture width/height pair. This should be a reasonable
     * compromise between no caching and caching too many renderers. While it is possible that the number of cached
     * renderers could grow out of control over time, in practice most textures should be a standard size.
     */
    private Map<Vector2i, BlockSelectionRenderer> cachedBlockSelectionRendererByTextureDimensionsMap = new HashMap<>();

    @Override
    public void renderOverlay() {
        for (EntityRef entity : entityManager.getEntitiesWith(BlockSelectionComponent.class)) {
            BlockSelectionComponent blockSelectionComponent = entity.getComponent(BlockSelectionComponent.class);
            if (blockSelectionComponent.shouldRender) {
                Texture texture = blockSelectionComponent.texture;
                if (null == texture) {
                    texture = Assets.getTexture("engine:selection").get();
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
        selectionRenderer.beginRenderOverlay();
        if (blockSelectionComponent.currentSelection == null) {
            if (blockSelectionComponent.startPosition != null) {
                selectionRenderer.renderMark(blockSelectionComponent.startPosition);
            }
        } else {
            for (Vector3ic pos : blockSelectionComponent.currentSelection) {
                selectionRenderer.renderMark(pos);
            }
        }
        selectionRenderer.endRenderOverlay();

    }
}
