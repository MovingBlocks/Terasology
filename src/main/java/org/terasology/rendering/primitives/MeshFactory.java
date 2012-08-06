/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import org.terasology.asset.AssetManager;
import org.terasology.rendering.assets.Texture;

import javax.vecmath.Vector4f;
import java.nio.ByteBuffer;

public class MeshFactory {

    /* SINGLETON */
    private static MeshFactory _instance;
    private Tessellator tessellator = new Tessellator();

    public static MeshFactory getInstance() {
        if (_instance == null)
            _instance = new MeshFactory();

        return _instance;
    }

    private MeshFactory() {
    }

    public Mesh generateItemMesh(int posX, int posY) {
        Texture tex = AssetManager.loadTexture("engine:items");
        ByteBuffer buffer = tex.getImageData(0);

        posX *= 16;
        posY *= 16;

        int stride = tex.getWidth() * 4;

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int r = buffer.get((posY + y) * stride + (posX + x) * 4) & 255;
                int g = buffer.get((posY + y) * stride + (posX + x) * 4 + 1) & 255;
                int b = buffer.get((posY + y) * stride + (posX + x) * 4 + 2) & 255;
                int a = buffer.get((posY + y) * stride + (posX + x) * 4 + 3) & 255;

                if (a != 0) {
                    TessellatorHelper.addBlockMesh(tessellator, new Vector4f(r / 255f, g / 255f, b / 255f, 1.0f), 2f * 0.0625f, 1.0f, 0.5f, 2f * 0.0625f * x - 1f / 2f, 2f * 0.0625f * (16 - y) - 1f, 0f);
                }
            }
        }

        Mesh result = tessellator.generateMesh();
        tessellator.resetAll();

        return result;
    }

}
