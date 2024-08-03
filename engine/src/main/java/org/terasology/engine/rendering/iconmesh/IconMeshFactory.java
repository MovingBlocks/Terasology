// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.iconmesh;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.rendering.assets.texture.TextureRegion;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.context.annotation.API;
import org.terasology.gestalt.naming.Name;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.Color;

import java.nio.ByteBuffer;

@API
public final class IconMeshFactory {

    private IconMeshFactory() {
    }

    public static Mesh getIconMesh(TextureRegion region) {
        if (region instanceof Asset) {
            ResourceUrn urn = ((Asset<?>) region).getUrn();
            if (urn.getFragmentName().isEmpty()) {
                return Assets.get(new ResourceUrn(urn.getModuleName(), IconMeshDataProducer.ICON_DISCRIMINATOR,
                        urn.getResourceName()), Mesh.class).get();
            } else {
                Name fragName =
                        new Name(urn.getResourceName().toString() + ResourceUrn.FRAGMENT_SEPARATOR + urn.getFragmentName().toString());
                return Assets.get(new ResourceUrn(urn.getModuleName(), IconMeshDataProducer.ICON_DISCRIMINATOR,
                        fragName), Mesh.class).get();
            }
        } else {
            return generateIconMesh(region);
        }
    }

    public static Mesh generateIconMesh(TextureRegion tex) {
        return generateIconMesh(null, tex, 0, false, null);
    }

    public static Mesh generateIconMesh(ResourceUrn urn, TextureRegion tex) {
        return generateIconMesh(urn, tex, 0, false, null);
    }

    public static MeshData generateIconMeshData(TextureRegion tex) {
        return generateIconMeshData(tex, 0, false, null);
    }

    public static Mesh generateIconMesh(ResourceUrn urn, TextureRegion tex, int alphaLimit, boolean withContour, Vector4f colorContour) {
        if (urn == null) {
            return Assets.generateAsset(generateIconMeshData(tex, alphaLimit, withContour, colorContour), Mesh.class);
        } else {
            return Assets.generateAsset(urn, generateIconMeshData(tex, alphaLimit, withContour, colorContour), Mesh.class);
        }
    }

    private static void addPixel(StandardMeshData mesh, Vector2fc position, float size, Color c) {
        Vector3f pos = new Vector3f();
        Vector3f norm = new Vector3f();
        final float sizeHalf = size / 2;

        int firstIndex = mesh.position.getPosition();

        // top
        mesh.position.put(pos.set(position, 0.0f).add(-sizeHalf, sizeHalf, sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(sizeHalf, sizeHalf, sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(sizeHalf, sizeHalf, -sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(-sizeHalf, sizeHalf, -sizeHalf));
        for (int i = 0; i < 4; i++) {
            mesh.normal.put(norm.set(0, 1.0f, 0));
            mesh.color0.put(c);
        }

        // left
        mesh.position.put(pos.set(position, 0.0f).add(-sizeHalf, -sizeHalf, -sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(-sizeHalf, -sizeHalf, sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(-sizeHalf, sizeHalf, sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(-sizeHalf, sizeHalf, -sizeHalf));
        for (int i = 0; i < 4; i++) {
            mesh.normal.put(norm.set(-1.0f, 0, 0));
            mesh.color0.put(c);
        }

        // right
        mesh.position.put(pos.set(position, 0.0f).add(sizeHalf, sizeHalf, -sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(sizeHalf, sizeHalf, sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(sizeHalf, -sizeHalf, sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(sizeHalf, -sizeHalf, -sizeHalf));
        for (int i = 0; i < 4; i++) {
            mesh.normal.put(norm.set(1.0f, 0, 0));
            mesh.color0.put(c);
        }

        // darkern for sides facing left, right and bottom
        Color cd = new Color(c.rf() * 0.6f, c.gf() * 0.6f, c.bf() * 0.6f, c.af());
        // back
        mesh.position.put(pos.set(position, 0.0f).add(-sizeHalf, sizeHalf, -sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(sizeHalf, sizeHalf, -sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(sizeHalf, -sizeHalf, -sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(-sizeHalf, -sizeHalf, -sizeHalf));
        for (int i = 0; i < 4; i++) {
            mesh.normal.put(norm.set(0, 0, -1.0f));
            mesh.color0.put(cd);
        }

        // front
        mesh.position.put(pos.set(position, 0.0f).add(-sizeHalf, -sizeHalf, sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(sizeHalf, -sizeHalf, sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(sizeHalf, sizeHalf, sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(-sizeHalf, sizeHalf, sizeHalf));
        for (int i = 0; i < 4; i++) {
            mesh.normal.put(norm.set(0, 0, 1.0f));
            mesh.color0.put(cd);
        }

        // bottom
        mesh.position.put(pos.set(position, 0.0f).add(-sizeHalf, -sizeHalf, -sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(sizeHalf, -sizeHalf, -sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(sizeHalf, -sizeHalf, sizeHalf));
        mesh.position.put(pos.set(position, 0.0f).add(-sizeHalf, -sizeHalf, sizeHalf));
        for (int i = 0; i < 4; i++) {
            mesh.normal.put(norm.set(0, -1, 0f));
            mesh.color0.put(cd);
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

    public static MeshData generateIconMeshData(TextureRegion tex, int alphaLimit, boolean withContour, Vector4f colorContour) {
        ByteBuffer buffer = tex.getTexture().getData().getBuffers()[0];

        Rectanglei pixelRegion = tex.getPixelRegion();
        int posX = pixelRegion.minX;
        int posY = pixelRegion.minY;

        int stride = tex.getTexture().getWidth() * 4;

        float textureSize = Math.max(tex.getWidth(), tex.getHeight());

        StandardMeshData mesh = new StandardMeshData();
        Vector2f pos = new Vector2f();
        Color color = new Color();
        for (int y = 0; y < tex.getHeight(); y++) {
            for (int x = 0; x < tex.getWidth(); x++) {
                int r = buffer.get((posY + y) * stride + (posX + x) * 4) & 255;
                int g = buffer.get((posY + y) * stride + (posX + x) * 4 + 1) & 255;
                int b = buffer.get((posY + y) * stride + (posX + x) * 4 + 2) & 255;
                int a = buffer.get((posY + y) * stride + (posX + x) * 4 + 3) & 255;

                if (a > alphaLimit) {
                    color.setRed(r)
                            .setGreen(g)
                            .setBlue(b)
                            .setAlpha(a);
                    pos.set(2f / textureSize * x - 1f, 2f / textureSize * (tex.getHeight() - y - 1) - 1f);
                    addPixel(mesh, pos, 2f / textureSize, color);

                    if (withContour) {
                        int newX = 0;
                        int newY = 0;
                        int newA = 0;

                        for (int i = 0; i < 4; i++) {
                            newA = alphaLimit + 1;
                            switch (i) {
                                case 0:
                                    //check left
                                    if (x > 0) {
                                        newX = x - 1;
                                        newY = y;
                                        newA = buffer.get((posY + newY) * stride + (posX + newX) * 4 + 3) & 255;
                                    }
                                    break;
                                case 1:
                                    //check top
                                    if (y > 0) {
                                        newX = x;
                                        newY = y - 1;
                                        newA = buffer.get((posY + newY) * stride + (posX + newX) * 4 + 3) & 255;
                                    }
                                    break;
                                case 2:
                                    //check right
                                    if (x < 16) {
                                        newX = x + 1;
                                        newY = y;
                                        newA = buffer.get((posY + newY) * stride + (posX + newX) * 4 + 3) & 255;
                                    }
                                    break;
                                case 3:
                                    //check bottom
                                    if (y < 16) {
                                        newX = x;
                                        newY = y + 1;
                                        newA = buffer.get((posY + newY) * stride + (posX + newX) * 4 + 3) & 255;
                                    }
                                    break;
                                default:
                                    break;
                            }

                            if (newA < alphaLimit) {
                                color.setRed(colorContour.x)
                                        .setGreen(colorContour.y)
                                        .setBlue(colorContour.z)
                                        .setAlpha(colorContour.w);
                                addPixel(mesh, pos.set(2f * 0.0625f * newX - 0.5f, 0.125f * (15 - newY) - 1f), 0.125f, color);
                            }
                        }
                    }
                }
            }
        }
        return mesh;
    }

}
