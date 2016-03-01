/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.logic.health;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.terasology.utilities.Assets;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegionAsset;
import org.terasology.rendering.world.selection.BlockSelectionRenderer;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.Optional;

/**
 * This system renders damage damaged blocks using the BlockSelectionRenderer.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class BlockDamageRenderer extends BaseComponentSystem implements RenderSystem {
    @In
    private EntityManager entityManager;

    BlockSelectionRenderer blockSelectionRenderer;

    @Override
    public void renderOverlay() {
        if (blockSelectionRenderer == null) {
            Texture texture = Assets.getTextureRegion("core:blockdamageeffects#1").get().getTexture();
            blockSelectionRenderer = new BlockSelectionRenderer(texture);
        }
        // group the entities into what texture they will use so that there is less recreating meshes (changing a texture region on the BlockSelectionRenderer
        // will recreate the mesh to use the different UV coordinates).  Also this allows
        Multimap<Integer, Vector3i> groupedEntitiesByEffect = ArrayListMultimap.create();

        for (EntityRef entity : entityManager.getEntitiesWith(HealthComponent.class, BlockComponent.class)) {
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (health.currentHealth == health.maxHealth) {
                continue;
            }
            BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
            groupedEntitiesByEffect.put(getEffectsNumber(health), blockComponent.getPosition());
        }
        for (EntityRef entity : entityManager.getEntitiesWith(BlockRegionComponent.class, HealthComponent.class)) {
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (health.currentHealth == health.maxHealth) {
                continue;
            }
            BlockRegionComponent blockRegion = entity.getComponent(BlockRegionComponent.class);
            for (Vector3i blockPos : blockRegion.region) {
                groupedEntitiesByEffect.put(getEffectsNumber(health), blockPos);
            }
        }

        // we know that the texture will be the same for each block effect,  just differnt UV coordinates.  Bind the texture already
        blockSelectionRenderer.beginRenderOverlay();

        for (Integer effectsNumber : groupedEntitiesByEffect.keySet()) {
            Optional<TextureRegionAsset> texture = Assets.getTextureRegion("core:blockdamageeffects#" + effectsNumber);
            if (texture.isPresent()) {
                blockSelectionRenderer.setEffectsTexture(texture.get());
                for (Vector3i position : groupedEntitiesByEffect.get(effectsNumber)) {
                    blockSelectionRenderer.renderMark(position);
                }
            }
        }

        blockSelectionRenderer.endRenderOverlay();
    }

    private Integer getEffectsNumber(HealthComponent health) {
        return java.lang.Math.round((1f - (float) health.currentHealth / health.maxHealth) * 10.0f);
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
