// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.world.selection;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.module.sandbox.API;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegionAsset;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.Assets;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslated;

/**
 * Renders a selection. Is used by the BlockSelectionSystem.
 * <br><br>
 * TODO total overhaul of this class. its neither good code, nor optimized in any way!
 *
 */
@API
public class BlockSelectionRenderer {
    private Mesh overlayMesh;
    private Mesh overlayMesh2;
    private Texture effectsTexture;
    private Material defaultTextured;
    private Rectanglef textureRegion = new Rectanglef(0, 0, 1, 1);

    public BlockSelectionRenderer(Texture effectsTexture) {
        this.effectsTexture = effectsTexture;
        initialize();
    }

    private void initialize() {
        Vector2f min = new Vector2f(textureRegion.minX(), textureRegion.minY());
        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, 1f),
                min , textureRegion.getSize(new Vector2f()),
                1.001f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
        overlayMesh = tessellator.generateMesh();
        tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, .2f),
                min, textureRegion.getSize(new Vector2f()), 1.001f, 1.0f, 1.0f, 0.0f,
                0.0f, 0.0f);
        overlayMesh2 = tessellator.generateMesh();
        defaultTextured = Assets.getMaterial("engine:prog.defaultTextured").get();
    }

    public void setEffectsTexture(TextureRegionAsset textureRegionAsset) {
        setEffectsTexture(textureRegionAsset.getTexture());
        textureRegion.set(textureRegionAsset.getRegion());
        // reinitialize to recreate the mesh's UV coordinates for this textureRegion
        initialize();
    }

    public void setEffectsTexture(Texture newEffectsTexture) {
        if ((effectsTexture.getWidth() == newEffectsTexture.getWidth()) && (effectsTexture.getHeight() == newEffectsTexture.getHeight())) {
            this.effectsTexture = newEffectsTexture;
        } else {
            // This should not be possible with the current BlockSelectionRenderSystem implementation
            throw new RuntimeException("New effectsTexture must have same height and width as the original effectsTexture");
        }
    }

    public void beginRenderOverlay() {
        if (effectsTexture == null || !effectsTexture.isLoaded()) {
            return;
        }

        defaultTextured.activateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
        defaultTextured.enable();

        glBindTexture(GL11.GL_TEXTURE_2D, effectsTexture.getId());

        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void endRenderOverlay() {
        glDisable(GL11.GL_BLEND);

        defaultTextured.deactivateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
    }

    public void renderMark(Vector3ic blockPos) {
        Vector3f cameraPos = getCameraPosition();

        glPushMatrix();
        glTranslated(blockPos.x() - cameraPos.x, blockPos.y() - cameraPos.y, blockPos.z() - cameraPos.z);

        glMatrixMode(GL_MODELVIEW);

        overlayMesh.render();

        glPopMatrix();
    }

    public void renderMark2(Vector3ic blockPos) {
        Vector3f cameraPos = getCameraPosition();

        glPushMatrix();
        glTranslated(blockPos.x() - cameraPos.x, blockPos.y() - cameraPos.y, blockPos.z() - cameraPos.z);

        glMatrixMode(GL_MODELVIEW);

        overlayMesh2.render();

        glPopMatrix();
    }

    private Vector3f getCameraPosition() {
        return CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
    }

}
