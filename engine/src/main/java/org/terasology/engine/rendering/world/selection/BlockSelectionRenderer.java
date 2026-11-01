// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world.selection;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.lwjgl.opengl.GL11;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.utilities.Assets;
import org.terasology.context.annotation.API;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

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


    private static void buildBlockMesh(StandardMeshData mesh, float size, Rectanglef texRect, Colorc c) {
        Vector3f pos = new Vector3f();
        Vector3f norm = new Vector3f();
        Vector2f texCoord = new Vector2f();
        final float sizeHalf = (size / 2) + .001f;

        int firstIndex = mesh.position.getPosition();

        // top
        mesh.position.put(pos.zero().add(-sizeHalf, sizeHalf, sizeHalf));
        mesh.position.put(pos.zero().add(sizeHalf, sizeHalf, sizeHalf));
        mesh.position.put(pos.zero().add(sizeHalf, sizeHalf, -sizeHalf));
        mesh.position.put(pos.zero().add(-sizeHalf, sizeHalf, -sizeHalf));
        mesh.uv0.put(texCoord.set(texRect.minX, texRect.minY));
        mesh.uv0.put(texCoord.set(texRect.maxX, texRect.minY));
        mesh.uv0.put(texCoord.set(texRect.maxX, texRect.maxY));
        mesh.uv0.put(texCoord.set(texRect.minX, texRect.maxY));
        for (int i = 0; i < 4; i++) {
            mesh.normal.put(norm.set(0, 1.0f, 0));
            mesh.color0.put(c);
        }

        // left
        mesh.position.put(pos.zero().add(-sizeHalf, -sizeHalf, -sizeHalf));
        mesh.position.put(pos.zero().add(-sizeHalf, -sizeHalf, sizeHalf));
        mesh.position.put(pos.zero().add(-sizeHalf, sizeHalf, sizeHalf));
        mesh.position.put(pos.zero().add(-sizeHalf, sizeHalf, -sizeHalf));
        mesh.uv0.put(texCoord.set(texRect.minX, texRect.maxY));
        mesh.uv0.put(texCoord.set(texRect.maxX, texRect.maxY));
        mesh.uv0.put(texCoord.set(texRect.maxX, texRect.minY));
        mesh.uv0.put(texCoord.set(texRect.minX, texRect.minY));
        for (int i = 0; i < 4; i++) {
            mesh.normal.put(norm.set(-1.0f, 0, 0));
            mesh.color0.put(c);
        }

        // right
        mesh.position.put(pos.zero().add(sizeHalf, sizeHalf, -sizeHalf));
        mesh.position.put(pos.zero().add(sizeHalf, sizeHalf, sizeHalf));
        mesh.position.put(pos.zero().add(sizeHalf, -sizeHalf, sizeHalf));
        mesh.position.put(pos.zero().add(sizeHalf, -sizeHalf, -sizeHalf));
        mesh.uv0.put(texCoord.set(texRect.minX, texRect.minY));
        mesh.uv0.put(texCoord.set(texRect.maxX, texRect.minY));
        mesh.uv0.put(texCoord.set(texRect.maxX, texRect.maxY));
        mesh.uv0.put(texCoord.set(texRect.minX, texRect.maxY));
        for (int i = 0; i < 4; i++) {
            mesh.normal.put(norm.set(1.0f, 0, 0));
            mesh.color0.put(c);
        }

        // back
        mesh.position.put(pos.zero().add(-sizeHalf, sizeHalf, -sizeHalf));
        mesh.position.put(pos.zero().add(sizeHalf, sizeHalf, -sizeHalf));
        mesh.position.put(pos.zero().add(sizeHalf, -sizeHalf, -sizeHalf));
        mesh.position.put(pos.zero().add(-sizeHalf, -sizeHalf, -sizeHalf));
        mesh.uv0.put(texCoord.set(texRect.minX, texRect.minY));
        mesh.uv0.put(texCoord.set(texRect.maxX, texRect.minY));
        mesh.uv0.put(texCoord.set(texRect.maxX, texRect.maxY));
        mesh.uv0.put(texCoord.set(texRect.minX, texRect.maxY));
        for (int i = 0; i < 4; i++) {
            mesh.normal.put(norm.set(0, 0, -1.0f));
            mesh.color0.put(c);
        }

        // front
        mesh.position.put(pos.zero().add(-sizeHalf, -sizeHalf, sizeHalf));
        mesh.position.put(pos.zero().add(sizeHalf, -sizeHalf, sizeHalf));
        mesh.position.put(pos.zero().add(sizeHalf, sizeHalf, sizeHalf));
        mesh.position.put(pos.zero().add(-sizeHalf, sizeHalf, sizeHalf));
        mesh.uv0.put(texCoord.set(texRect.minX, texRect.maxY));
        mesh.uv0.put(texCoord.set(texRect.maxX, texRect.maxY));
        mesh.uv0.put(texCoord.set(texRect.maxX, texRect.minY));
        mesh.uv0.put(texCoord.set(texRect.minX, texRect.minY));
        for (int i = 0; i < 4; i++) {
            mesh.normal.put(norm.set(0, 0, 1.0f));
            mesh.color0.put(c);
        }

        // bottom
        mesh.position.put(pos.zero().add(-sizeHalf, -sizeHalf, -sizeHalf));
        mesh.position.put(pos.zero().add(sizeHalf, -sizeHalf, -sizeHalf));
        mesh.position.put(pos.zero().add(sizeHalf, -sizeHalf, sizeHalf));
        mesh.position.put(pos.zero().add(-sizeHalf, -sizeHalf, sizeHalf));
        mesh.uv0.put(texCoord.set(texRect.minX, texRect.minY));
        mesh.uv0.put(texCoord.set(texRect.maxX, texRect.minY));
        mesh.uv0.put(texCoord.set(texRect.maxX, texRect.maxY));
        mesh.uv0.put(texCoord.set(texRect.minX, texRect.maxY));
        for (int i = 0; i < 4; i++) {
            mesh.normal.put(norm.set(0, -1, 0f));
            mesh.color0.put(c);
        }


        int lastIndex = mesh.position.getPosition();
        for (int i = firstIndex; i < lastIndex - 2; i += 4) {
            mesh.indices.put(i);
            mesh.indices.put(i + 1);
            mesh.indices.put(i + 2);

            mesh.indices.put(i + 2);
            mesh.indices.put(i + 3);
            mesh.indices.put(i);
        }
    }

    private void initialize() {
        StandardMeshData overlayMeshData = new StandardMeshData();
        buildBlockMesh(overlayMeshData, 1.0f, textureRegion, Color.white);
        overlayMesh = Assets.generateAsset(overlayMeshData, Mesh.class);

        StandardMeshData overlayMeshData2 = new StandardMeshData();
        buildBlockMesh(overlayMeshData2, 1.0f, textureRegion, new Color(1.0f, 1.0f, 1.0f, .2f));
        overlayMesh2 = Assets.generateAsset(overlayMeshData2, Mesh.class);
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
        blockSelectionMat.setTexture("tex", effectsTexture);
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
