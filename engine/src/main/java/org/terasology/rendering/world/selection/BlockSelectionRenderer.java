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

import org.lwjgl.opengl.GL11;
import org.terasology.utilities.Assets;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector4f;
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
    private Rect2f textureRegion = Rect2f.createFromMinAndSize(0f, 0f, 1f, 1f);

    public BlockSelectionRenderer(Texture effectsTexture) {
        this.effectsTexture = effectsTexture;
        initialize();
    }

    private void initialize() {
        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, 1f), textureRegion.min(), textureRegion.size(), 1.001f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
        overlayMesh = tessellator.generateMesh();
        tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, .2f), textureRegion.min(), textureRegion.size(), 1.001f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
        overlayMesh2 = tessellator.generateMesh();
        defaultTextured = Assets.getMaterial("engine:prog.defaultTextured").get();
    }

    public void setEffectsTexture(TextureRegionAsset textureRegionAsset) {
        setEffectsTexture(textureRegionAsset.getTexture());
        textureRegion = textureRegionAsset.getRegion();
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

    public void renderMark(Vector3i blockPos) {
        Vector3f cameraPos = getCameraPosition();

        glPushMatrix();
        glTranslated(blockPos.x - cameraPos.x, blockPos.y - cameraPos.y, blockPos.z - cameraPos.z);

        glMatrixMode(GL_MODELVIEW);

        overlayMesh.render();

        glPopMatrix();
    }

    public void renderMark2(Vector3i blockPos) {
        Vector3f cameraPos = getCameraPosition();

        glPushMatrix();
        glTranslated(blockPos.x - cameraPos.x, blockPos.y - cameraPos.y, blockPos.z - cameraPos.z);

        glMatrixMode(GL_MODELVIEW);

        overlayMesh2.render();

        glPopMatrix();
    }

    private Vector3f getCameraPosition() {
        return CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
    }

}
