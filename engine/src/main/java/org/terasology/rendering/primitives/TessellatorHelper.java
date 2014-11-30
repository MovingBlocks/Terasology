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
package org.terasology.rendering.primitives;

import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.module.sandbox.API;

@API
public final class TessellatorHelper {

    private TessellatorHelper() {
    }

    public static void addBlockMesh(Tessellator tessellator, Vector4f color, float size, float light1, float light2, float posX, float posY, float posZ) {
        Vector2f defaultSize = new Vector2f(1.0f, 1.0f);
        Vector2f defaultOffset = new Vector2f(0.0f, 0.0f);
        addBlockMesh(tessellator, color, defaultOffset, defaultSize, size, light1, light2, posX, posY, posZ);
    }

    public static void addBlockMesh(Tessellator tessellator, Vector4f color, Vector2f texOffset, Vector2f texSize,
                                    float size, float light1, float light2, float posX, float posY, float posZ) {
        Vector2f[] sizes = new Vector2f[6];
        Vector2f[] offsets = new Vector2f[6];

        for (int i = 0; i < 6; i++) {
            sizes[i] = texSize;
            offsets[i] = texOffset;
        }

        addBlockMesh(tessellator, color, offsets, sizes, size, light1, light2, posX, posY, posZ);
    }

    public static void addBlockMesh(Tessellator tessellator, Vector4f color, Vector2f[] texOffsets, Vector2f[] texSizes,
                                    float size, float light1, float light2, float posX, float posY, float posZ) {
        final float sizeHalf = size / 2;

        tessellator.resetParams();
        tessellator.setColor(new Vector4f(light1 * color.x, light1 * color.y, light1 * color.z, color.w));

        tessellator.setNormal(new Vector3f(0, 1, 0));
        tessellator.addPoly(
                new Vector3f[]{
                        new Vector3f(-sizeHalf + posX, sizeHalf + posY, sizeHalf + posZ),
                        new Vector3f(sizeHalf + posX, sizeHalf + posY, sizeHalf + posZ),
                        new Vector3f(sizeHalf + posX, sizeHalf + posY, -sizeHalf + posZ),
                        new Vector3f(-sizeHalf + posX, sizeHalf + posY, -sizeHalf + posZ)
                },
                new Vector2f[]{
                        new Vector2f(texOffsets[0].x, texOffsets[0].y),
                        new Vector2f(texOffsets[0].x + texSizes[0].x, texOffsets[0].y),
                        new Vector2f(texOffsets[0].x + texSizes[0].x, texOffsets[0].y + texSizes[0].y),
                        new Vector2f(texOffsets[0].x, texOffsets[0].y + texSizes[0].y)
                });

        tessellator.setNormal(new Vector3f(-1, 0, 0));
        tessellator.addPoly(
                new Vector3f[]{
                        new Vector3f(-sizeHalf + posX, -sizeHalf + posY, -sizeHalf + posZ),
                        new Vector3f(-sizeHalf + posX, -sizeHalf + posY, sizeHalf + posZ),
                        new Vector3f(-sizeHalf + posX, sizeHalf + posY, sizeHalf + posZ),
                        new Vector3f(-sizeHalf + posX, sizeHalf + posY, -sizeHalf + posZ)
                },
                new Vector2f[]{
                        new Vector2f(texOffsets[1].x, texOffsets[1].y + texSizes[1].y),
                        new Vector2f(texOffsets[1].x + texSizes[1].x, texOffsets[1].y + texSizes[1].y),
                        new Vector2f(texOffsets[1].x + texSizes[1].x, texOffsets[1].y),
                        new Vector2f(texOffsets[1].x, texOffsets[1].y)
                });

        tessellator.setNormal(new Vector3f(1, 0, 0));
        tessellator.addPoly(
                new Vector3f[]{
                        new Vector3f(sizeHalf + posX, sizeHalf + posY, -sizeHalf + posZ),
                        new Vector3f(sizeHalf + posX, sizeHalf + posY, sizeHalf + posZ),
                        new Vector3f(sizeHalf + posX, -sizeHalf + posY, sizeHalf + posZ),
                        new Vector3f(sizeHalf + posX, -sizeHalf + posY, -sizeHalf + posZ)
                },
                new Vector2f[]{
                        new Vector2f(texOffsets[2].x, texOffsets[2].y),
                        new Vector2f(texOffsets[2].x + texSizes[2].x, texOffsets[2].y),
                        new Vector2f(texOffsets[2].x + texSizes[2].x, texOffsets[2].y + texSizes[2].y),
                        new Vector2f(texOffsets[2].x, texOffsets[2].y + texSizes[2].y)
                });


        tessellator.setColor(new Vector4f(light2 * color.x, light2 * color.y, light2 * color.z, color.w));

        tessellator.setNormal(new Vector3f(0, 0, -1));
        tessellator.addPoly(
                new Vector3f[]{
                        new Vector3f(-sizeHalf + posX, sizeHalf + posY, -sizeHalf + posZ),
                        new Vector3f(sizeHalf + posX, sizeHalf + posY, -sizeHalf + posZ),
                        new Vector3f(sizeHalf + posX, -sizeHalf + posY, -sizeHalf + posZ),
                        new Vector3f(-sizeHalf + posX, -sizeHalf + posY, -sizeHalf + posZ)
                },
                new Vector2f[]{
                        new Vector2f(texOffsets[3].x, texOffsets[3].y),
                        new Vector2f(texOffsets[3].x + texSizes[3].x, texOffsets[3].y),
                        new Vector2f(texOffsets[3].x + texSizes[3].x, texOffsets[3].y + texSizes[3].y),
                        new Vector2f(texOffsets[3].x, texOffsets[3].y + texSizes[3].y)
                });

        tessellator.setNormal(new Vector3f(0, 0, 1));
        tessellator.addPoly(
                new Vector3f[]{
                        new Vector3f(-sizeHalf + posX, -sizeHalf + posY, sizeHalf + posZ),
                        new Vector3f(sizeHalf + posX, -sizeHalf + posY, sizeHalf + posZ),
                        new Vector3f(sizeHalf + posX, sizeHalf + posY, sizeHalf + posZ),
                        new Vector3f(-sizeHalf + posX, sizeHalf + posY, sizeHalf + posZ)
                },
                new Vector2f[]{
                        new Vector2f(texOffsets[4].x, texOffsets[4].y + texSizes[4].y),
                        new Vector2f(texOffsets[4].x + texSizes[4].x, texOffsets[4].y + texSizes[4].y),
                        new Vector2f(texOffsets[4].x + texSizes[4].x, texOffsets[4].y),
                        new Vector2f(texOffsets[4].x, texOffsets[4].y)
                });

        tessellator.setNormal(new Vector3f(0, -1, 0));
        tessellator.addPoly(
                new Vector3f[]{
                        new Vector3f(-sizeHalf + posX, -sizeHalf + posY, -sizeHalf + posZ),
                        new Vector3f(sizeHalf + posX, -sizeHalf + posY, -sizeHalf + posZ),
                        new Vector3f(sizeHalf + posX, -sizeHalf + posY, sizeHalf + posZ),
                        new Vector3f(-sizeHalf + posX, -sizeHalf + posY, sizeHalf + posZ)
                },
                new Vector2f[]{
                        new Vector2f(texOffsets[5].x, texOffsets[5].y),
                        new Vector2f(texOffsets[5].x + texSizes[5].x, texOffsets[5].y),
                        new Vector2f(texOffsets[5].x + texSizes[5].x, texOffsets[5].y + texSizes[5].y),
                        new Vector2f(texOffsets[5].x, texOffsets[5].y + texSizes[5].y)
                });
    }

    public static void addGUIQuadMesh(Tessellator tessellator, Vector4f color, float sizeX, float sizeY) {
        tessellator.resetParams();
        tessellator.setColor(new Vector4f(color.x, color.y, color.z, color.w));
        tessellator.setUseLighting(false);
        tessellator.setUseNormals(false);

        tessellator.addPoly(
                new Vector3f[]{
                        new Vector3f(0, 0, 0),
                        new Vector3f(sizeX, 0, 0),
                        new Vector3f(sizeX, sizeY, 0),
                        new Vector3f(0, sizeY, 0)
                },
                new Vector2f[]{
                        new Vector2f(0, 0),
                        new Vector2f(1, 0),
                        new Vector2f(1, 1),
                        new Vector2f(0, 1)
                }
        );
        tessellator.setUseLighting(true);
        tessellator.setUseNormals(true);
    }

}
