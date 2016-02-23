/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.block.entity.damage;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector4f;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.regions.BlockRegionComponent;

import static org.lwjgl.opengl.GL11.GL_DST_COLOR;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.GL_ZERO;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 */
@RegisterSystem(RegisterMode.CLIENT)
public class BlockDamageRenderer extends BaseComponentSystem implements RenderSystem {

    @In
    private EntityManager entityManager;
    @In
    private WorldProvider worldProvider;
    @In
    private LocalPlayer localPlayer;

    private Mesh overlayMesh;
    private Texture effectsTexture;

    @Override
    public void initialise() {
        this.effectsTexture = Assets.getTexture("engine:effects").get();
        Vector2f texPos = new Vector2f(0.0f, 0.0f);
        Vector2f texWidth = new Vector2f(0.0624f, 0.0624f);

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, 1), texPos, texWidth, 1.001f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
        overlayMesh = tessellator.generateMesh();
    }

    @Override
    public void renderOverlay() {
        if (effectsTexture == null || !effectsTexture.isLoaded()) {
            return;
        }

        Material defaultTextured = Assets.getMaterial("engine:prog.defaultTextured").get();
        defaultTextured.activateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
        defaultTextured.enable();

        glBindTexture(GL11.GL_TEXTURE_2D, effectsTexture.getId());

        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL_DST_COLOR, GL_ZERO);

        Vector3f cameraPosition = localPlayer.getViewPosition();

        for (EntityRef entity : entityManager.getEntitiesWith(HealthComponent.class, BlockComponent.class)) {
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (health.currentHealth == health.maxHealth) {
                continue;
            }
            BlockComponent blockComp = entity.getComponent(BlockComponent.class);
            renderHealth(blockComp.getPosition(), health, cameraPosition);
        }
        for (EntityRef entity : entityManager.getEntitiesWith(BlockRegionComponent.class, HealthComponent.class)) {
            HealthComponent health = entity.getComponent(HealthComponent.class);
            if (health.currentHealth == health.maxHealth) {
                continue;
            }
            BlockRegionComponent blockRegion = entity.getComponent(BlockRegionComponent.class);
            for (Vector3i blockPos : blockRegion.region) {
                renderHealth(blockPos, health, cameraPosition);
            }
        }

        glDisable(GL11.GL_BLEND);

        defaultTextured.deactivateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
    }

    private void renderHealth(Vector3i blockPos, HealthComponent health, Vector3f cameraPos) {
        if (!worldProvider.isBlockRelevant(blockPos)) {
            return;
        }

        glPushMatrix();
        glTranslated(blockPos.x - cameraPos.x, blockPos.y - cameraPos.y, blockPos.z - cameraPos.z);

        float offset = java.lang.Math.round((1.0f - (float) health.currentHealth / health.maxHealth) * 9.0f) * 0.0625f;

        glMatrixMode(GL_TEXTURE);
        glPushMatrix();
        glTranslatef(offset, 0f, 0f);
        glMatrixMode(GL_MODELVIEW);

        overlayMesh.render();

        glPopMatrix();

        glMatrixMode(GL_TEXTURE);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
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
