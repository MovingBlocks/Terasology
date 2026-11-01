// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshBuilder;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.rendering.assets.mesh.resource.AllocationType;
import org.terasology.engine.rendering.assets.mesh.resource.DrawingMode;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.utilities.Assets;
import org.terasology.context.annotation.API;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;
import org.terasology.nui.Color;

/**
 * Renderer for an AABB.
 */
@API
public class AABBRenderer implements BlockOverlayRenderer, AutoCloseable {

    protected static final String DEFAULT_MATERIAL_URI = "engine:prog.default";

    private Vector4f solidColor = new Vector4f(1f, 1f, 1f, 1f);
    private Mesh solidMesh;
    private Mesh wireMesh;
    private AABBf aabb = new AABBf();
    private WorldRenderer worldRenderer;
    private Material defaultMaterial;

    public AABBRenderer(AABBfc aabb) {
        this.aabb.set(aabb);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
        defaultMaterial = Assets.getMaterial(DEFAULT_MATERIAL_URI).get();

    }

    @Override
    public void setAABB(AABBfc from) {
        if (from != null && !from.equals(this.aabb)) {
            this.aabb.set(from);
            dispose();
        }
    }

    public void dispose() {
        if (solidMesh != null) {
            solidMesh.dispose();
            solidMesh = null;
        }
        if (wireMesh != null) {
            wireMesh.dispose();
            wireMesh = null;
        }
    }

    public void setSolidColor(Vector4f color) {
        solidColor = color;
    }

    private void prepare() {
        defaultMaterial.enable();

        Matrix4f modelView = new Matrix4f();
        Camera camera = worldRenderer.getActiveCamera();

        Vector3f center = aabb.center(new Vector3f());
        Vector3f cameraPosition = CoreRegistry.get(LocalPlayer.class).getViewPosition(new Vector3f());
        modelView.set(camera.getViewMatrix()).mul(new Matrix4f().setTranslation(
                center.x() - cameraPosition.x, center.y() - cameraPosition.y,
                center.z() - cameraPosition.z
        ));

        defaultMaterial.setMatrix4("modelViewMatrix", modelView);
        defaultMaterial.setMatrix4("projectionMatrix", camera.getProjectionMatrix());

    }

    /**
     * Renders this AABB.
     * <br><br>
     */
    @Override
    public void render() {
        prepare();
        renderLocally();
    }

    public void renderSolid() {
        prepare();
        renderSolidLocally();
    }

    /**
     * Maintained for API compatibility.
     */
    public void renderLocally(float ignored) {
        renderLocally();
    }

    public void renderLocally() {
        if (wireMesh == null) {
            generateDisplayListWire();
        }
        wireMesh.render();
    }

    public void renderSolidLocally() {
        if (solidMesh == null) {
            generateDisplayListSolid();
        }
        solidMesh.render();
    }

    private void generateDisplayListSolid() {
        MeshBuilder builder = new MeshBuilder();
        builder.addBox(aabb.extent(new Vector3f()).mul(-1.0f), aabb.extent(new Vector3f()).mul(2.0f), 0.0f, 0.0f)
                .setTextureMapper(new MeshBuilder.TextureMapper() {
                    @Override
                    public void initialize(Vector3fc offset, Vector3fc size) {

                    }

                    @Override
                    public Vector2fc map(int vertexIndex, float u, float v) {
                        switch (vertexIndex) {
                            // Front face
                            case  0 : return new Vector2f(0f, 1f);
                            case  1 : return new Vector2f(1f, 1f);
                            case  2 : return new Vector2f(1f, 1);
                            case  3 : return new Vector2f(0f, 1);
                            // Back face
                            case  4 : return new Vector2f(1f, 1f);
                            case  5 : return new Vector2f(1f, 1);
                            case  6 : return new Vector2f(0f, 1);
                            case  7 : return new Vector2f(0f, 1f);
                            // Top face
                            case  8 : return new Vector2f(1f, 0f);
                            case  9 : return new Vector2f(1f, 1f);
                            case 10 : return new Vector2f(0f, 1f);
                            case 11 : return new Vector2f(0f, 0f);
                            // Bottom face
                            case 12 : return new Vector2f(1f, 0f);
                            case 13 : return new Vector2f(0f, 0f);
                            case 14 : return new Vector2f(0f, 1f);
                            case 15 : return new Vector2f(1f, 1f);
                            // Right face
                            case 16 : return new Vector2f(1f, 1f);
                            case 17 : return new Vector2f(1f, 1);
                            case 18 : return new Vector2f(0f, 1);
                            case 19 : return new Vector2f(0f, 1f);
                            // Left face
                            case 20 : return new Vector2f(0f, 0f);
                            case 21 : return new Vector2f(1f, 0f);
                            case 22 : return new Vector2f(1f, 1.0f);
                            case 23 : return new Vector2f(0f, 1.0f);

                            default : throw new RuntimeException("Unreachable state.");
                        }
                    }
                });
        for (int  x = 0; x < 24; x++) {
            builder.addColor(new Color(solidColor.x, solidColor.y, solidColor.z, solidColor.w));
        }
        solidMesh = builder.build();
    }

    private void generateDisplayListWire() {
        float offset = 0.001f;

        StandardMeshData meshData = new StandardMeshData(DrawingMode.LINES, AllocationType.DYNAMIC);


        Vector3f dimensions = aabb.extent(new Vector3f());
        Vector3f pos = new Vector3f();
        // top verts
        meshData.position.put(pos.set(-dimensions.x - offset, -dimensions.y - offset, -dimensions.z - offset)); // 0
        meshData.position.put(pos.set(+dimensions.x + offset, -dimensions.y - offset, -dimensions.z - offset)); // 1
        meshData.position.put(pos.set(+dimensions.x + offset, -dimensions.y - offset, +dimensions.z + offset)); // 2
        meshData.position.put(pos.set(-dimensions.x - offset, -dimensions.y - offset, +dimensions.z + offset)); // 3

        // bottom verts
        meshData.position.put(pos.set(-dimensions.x - offset, +dimensions.y + offset, -dimensions.z - offset)); // 4
        meshData.position.put(pos.set(+dimensions.x + offset, +dimensions.y + offset, -dimensions.z - offset)); // 5
        meshData.position.put(pos.set(+dimensions.x + offset, +dimensions.y + offset, +dimensions.z + offset)); // 6
        meshData.position.put(pos.set(-dimensions.x - offset, +dimensions.y + offset, +dimensions.z + offset)); // 7

        meshData.indices.putAll(new int[]{
                // top loop
                0, 1,
                1, 2,
                2, 3,
                3, 0,

                // connecting edges between top and bottom
                0, 4,
                1, 5,
                2, 6,
                3, 7,

                // bottom loop
                4, 5,
                5, 6,
                6, 7,
                7, 4,
        });

        for (int i = 0; i < 8; i++) {
            meshData.color0.put(Color.black);
        }
        wireMesh = Assets.generateAsset(meshData, Mesh.class);
    }

    public AABBfc getAABB() {
        return aabb;
    }

    @Override
    public void close() {
        dispose();
    }
}
