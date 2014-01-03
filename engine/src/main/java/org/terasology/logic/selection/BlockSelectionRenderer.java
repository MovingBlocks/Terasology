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

import java.awt.Color;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.math.Vector3i;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;

/**
 * Renders a selection. Is used by the BlockSelectionSystem.
 * <p/>
 * TODO total overhaul of this class. its neither good code, nor optimized in any way!
 *
 * @author synopia
 */
public class BlockSelectionRenderer {
    private Mesh overlayMesh;
    private Mesh overlayMesh2;
    private Texture effectsTexture;
    private Material defaultTextured;

    public BlockSelectionRenderer() {
        this(Assets.getTexture("engine:selection"));
    }

    public BlockSelectionRenderer(Color color) {
        this(Assets.get(TextureUtil.getTextureUriForColor(color), Texture.class));
    }

    public BlockSelectionRenderer(Texture effectsTexture) {
        this.effectsTexture = effectsTexture;
        Vector2f texPos = new Vector2f(0.0f, 0.0f);
        Vector2f texWidth = new Vector2f(1.f / effectsTexture.getWidth(), 1.f / effectsTexture.getHeight());

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, 1f), texPos, texWidth, 1.001f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
        overlayMesh = tessellator.generateMesh();
        tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, .2f), texPos, texWidth, 1.001f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
        overlayMesh2 = tessellator.generateMesh();
        defaultTextured = Assets.getMaterial("engine:defaultTextured");
    }

    public void beginRenderOverlay() {
        if (effectsTexture == null) {
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

    public void renderMark(Vector3i blockPos, Vector3f cameraPos) {
        glPushMatrix();
        glTranslated(blockPos.x - cameraPos.x, blockPos.y - cameraPos.y, blockPos.z - cameraPos.z);

        glMatrixMode(GL_MODELVIEW);

        overlayMesh.render();

        glPopMatrix();
    }

    public void renderMark2(Vector3i blockPos, Vector3f cameraPos) {
        glPushMatrix();
        glTranslated(blockPos.x - cameraPos.x, blockPos.y - cameraPos.y, blockPos.z - cameraPos.z);

        glMatrixMode(GL_MODELVIEW);

        overlayMesh2.render();

        glPopMatrix();
    }

}
