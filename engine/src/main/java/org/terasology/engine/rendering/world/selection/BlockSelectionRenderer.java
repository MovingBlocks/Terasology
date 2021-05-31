// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world.selection;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.primitives.Tessellator;
import org.terasology.engine.rendering.primitives.TessellatorHelper;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.joml.geom.Rectanglef;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

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
    private Material blockSelectionMat;
    private Rectanglef textureRegion = new Rectanglef(0, 0, 1, 1);
    private WorldRenderer worldRenderer;

    public BlockSelectionRenderer(Texture effectsTexture) {
        this.effectsTexture = effectsTexture;
        this.worldRenderer = CoreRegistry.get(WorldRenderer.class);

        initialize();
    }

    private void initialize() {
        Vector2f min = new Vector2f(textureRegion.minX(), textureRegion.minY());
        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, 1f),
                min, textureRegion.getSize(new Vector2f()),
                1.001f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f);
        overlayMesh = tessellator.generateMesh();
        tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1, 1, 1, .2f),
                min, textureRegion.getSize(new Vector2f()), 1.001f, 1.0f, 1.0f, 0.0f,
                0.0f, 0.0f);
        overlayMesh2 = tessellator.generateMesh();
        blockSelectionMat = Assets.getMaterial("engine:prog.blockSelection").get();
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
            throw new RuntimeException("New effectsTexture must have same height and width as the original " +
                    "effectsTexture");
        }
    }

    public void beginRenderOverlay() {
        if (effectsTexture == null || !effectsTexture.isLoaded()) {
            return;
        }
        Camera camera = worldRenderer.getActiveCamera();
        blockSelectionMat.enable();
        blockSelectionMat.activateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
        blockSelectionMat.setMatrix4("projectionMatrix", camera.getProjectionMatrix());
        blockSelectionMat.setTexture("texture", effectsTexture);
        blockSelectionMat.bindTextures();

        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void endRenderOverlay() {
        glDisable(GL11.GL_BLEND);
        blockSelectionMat.deactivateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
    }

    public void renderMark(Vector3ic blockPos) {
        Camera camera = worldRenderer.getActiveCamera();
        final Vector3f cameraPosition = camera.getPosition();

        Matrix4f modelView = new Matrix4f();
        modelView.set(camera.getViewMatrix()).mul(new Matrix4f().setTranslation(
                blockPos.x() - cameraPosition.x, blockPos.y() - cameraPosition.y,
                blockPos.z() - cameraPosition.z
        ));
        blockSelectionMat.setMatrix4("modelViewMatrix", modelView);


        overlayMesh.render();
    }

    public void renderMark2(Vector3ic blockPos) {
        Camera camera = worldRenderer.getActiveCamera();
        final Vector3f cameraPosition = camera.getPosition();

        Matrix4f modelView = new Matrix4f();
        modelView.set(camera.getViewMatrix()).mul(new Matrix4f().setTranslation(
                blockPos.x() - cameraPosition.x, blockPos.y() - cameraPosition.y,
                blockPos.z() - cameraPosition.z
        ));
        blockSelectionMat.setMatrix4("modelViewMatrix", modelView);

        overlayMesh2.render();
    }

}
