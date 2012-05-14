/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.VertexBufferObjectManager;
import org.terasology.rendering.interfaces.IGameObject;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

// TODO: Store mesh information in Mesh class in a usable format, for
public class Mesh implements Asset {

    private static final int VERTEX_SIZE = 3;
    private static final int TEX_COORD_0_SIZE = 2;
    private static final int TEX_COORD_1_SIZE = 3;
    private static final int COLOR_SIZE = 4;
    private static final int NORMAL_SIZE = 3;

    public static Mesh buildMesh(AssetUri uri, TFloatList vertices, TFloatList texCoord0, TFloatList texCoord1, TFloatList normals, TFloatList colors, TIntList indices) {
        Mesh mesh = buildMesh(vertices, texCoord0, texCoord1, normals, colors, indices);
        mesh.uri = uri;
        return mesh;
    }

    public static Mesh buildMesh(TFloatList vertices, TFloatList texCoord0, TFloatList texCoord1, TFloatList normals, TFloatList colors, TIntList indices) {
        
        int vertexCount = vertices.size() / VERTEX_SIZE;
        boolean hasTexCoord0 = texCoord0 != null && texCoord0.size() / TEX_COORD_0_SIZE == vertexCount;
        boolean hasTexCoord1 = texCoord1 != null && texCoord1.size() / TEX_COORD_1_SIZE == vertexCount;
        boolean hasNormals = normals != null && normals.size() / NORMAL_SIZE == vertexCount;
        boolean hasColors = colors != null && colors.size() / COLOR_SIZE == vertexCount;

        int vertSize = VERTEX_SIZE + (hasTexCoord0 ? TEX_COORD_0_SIZE : 0) + (hasTexCoord1 ? TEX_COORD_1_SIZE : 0) + (hasNormals ? NORMAL_SIZE : 0) + (hasColors ? COLOR_SIZE : 0);
        FloatBuffer vertexBuffer = createVertexBuffer(vertices, texCoord0, texCoord1, normals, colors, vertexCount, hasTexCoord0, hasTexCoord1, hasNormals, hasColors, vertSize);

        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.size());
        TIntIterator iterator = indices.iterator();
        while (iterator.hasNext()) {
            indexBuffer.put(iterator.next());
        }
        indexBuffer.flip();

        Mesh mesh = new Mesh();
        mesh.vboVertexBuffer = VertexBufferObjectManager.getInstance().getVboId();
        mesh.vboIndexBuffer = VertexBufferObjectManager.getInstance().getVboId();
        mesh.indexCount = indices.size();

        VertexBufferObjectManager.getInstance().bufferVboData(mesh.vboVertexBuffer, vertexBuffer, GL15.GL_STATIC_DRAW);
        VertexBufferObjectManager.getInstance().bufferVboElementData(mesh.vboIndexBuffer, indexBuffer, GL15.GL_STATIC_DRAW);

        mesh.stride = vertSize * 4;
        mesh.vertexOffset = 0;
        int offset = VERTEX_SIZE;

        if (hasTexCoord0) {
            mesh.hasTexCoord0 = hasTexCoord0;
            mesh.texCoord0Offset = 4 * offset;
            offset += TEX_COORD_0_SIZE;
        }
        if (hasTexCoord1) {
            mesh.hasTexCoord1 = hasTexCoord1;
            mesh.texCoord1Offset = 4 * offset;
            offset += TEX_COORD_1_SIZE;
        }
        if (hasNormals) {
            mesh.hasNormal = hasNormals;
            mesh.normalOffset = 4 * offset;
            offset += NORMAL_SIZE;
        }
        if (hasColors) {
            mesh.hasColor = hasColors;
            mesh.colorOffset = 4 * offset;
            offset += COLOR_SIZE;
        }

        return mesh;
    }

    private static FloatBuffer createVertexBuffer(TFloatList vertices, TFloatList texcoord0, TFloatList texcoord1, TFloatList normals, TFloatList colors, int vertexCount, boolean hasTexCoord0, boolean hasTexCoord1, boolean hasNormal, boolean hasColor, int vertSize) {
        FloatBuffer vertexBuffer;
        vertexBuffer = BufferUtils.createFloatBuffer(vertSize * vertexCount);
        int texCoord0Size = (hasTexCoord0) ? TEX_COORD_0_SIZE : 0;
        int texCoord1Size = (hasTexCoord1) ? TEX_COORD_1_SIZE : 0;
        int normalSize = (hasNormal) ? NORMAL_SIZE : 0;
        int colorSize = (hasColor) ? COLOR_SIZE : 0;
        int uv1 = 0, uv2 = 0, n = 0, c = 0;
        for (int v = 0; v < vertices.size(); v += 3) {
            vertexBuffer.put(vertices.get(v)).put(vertices.get(v + 1)).put(vertices.get(v + 2));
            for (int i = 0; i < texCoord0Size; ++i) {
                vertexBuffer.put(texcoord0.get(uv1 + i));
            }
            for (int i = 0; i < texCoord1Size; ++i) {
                vertexBuffer.put(texcoord1.get(uv2 + i));
            }
            for (int i = 0; i < normalSize; ++i) {
                vertexBuffer.put(normals.get(n + i));
            }
            for (int i = 0; i < colorSize; ++i) {
                vertexBuffer.put(colors.get(c + i));
            }

            uv1 += texCoord0Size;
            uv2 += texCoord1Size;
            n += normalSize;
            c += colorSize;
        }

        vertexBuffer.flip();
        return vertexBuffer;
    }

    private Mesh() {}

    private AssetUri uri = new AssetUri("");

    private int stride;
    private int vertexOffset;
    private int texCoord0Offset;
    private int texCoord1Offset;
    private int colorOffset;
    private int normalOffset;

    private boolean hasTexCoord0 = false;
    private boolean hasTexCoord1 = false;
    private boolean hasColor = false;
    private boolean hasNormal = false;

    private int vboVertexBuffer;
    private int vboIndexBuffer;
    private int indexCount;

    @Override
    public AssetUri getURI() {
        return uri;
    }

    public void render() {
        glEnableClientState(GL_VERTEX_ARRAY);
        if (hasTexCoord0 || hasTexCoord1) glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        if (hasColor) glEnableClientState(GL_COLOR_ARRAY);
        if (hasNormal) glEnableClientState(GL_NORMAL_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertexBuffer);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexBuffer);

        glVertexPointer(VERTEX_SIZE, GL11.GL_FLOAT, stride, vertexOffset);

        if (hasTexCoord0) {
            GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
            glTexCoordPointer(TEX_COORD_0_SIZE, GL11.GL_FLOAT, stride, texCoord0Offset);
        }

        if (hasTexCoord1) {
            GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
            glTexCoordPointer(TEX_COORD_1_SIZE, GL11.GL_FLOAT, stride, texCoord1Offset);
        }

        if (hasColor) glColorPointer(COLOR_SIZE, GL11.GL_FLOAT, stride, colorOffset);
        if (hasNormal) glNormalPointer(GL11.GL_FLOAT, stride, normalOffset);

        GL12.glDrawRangeElements(GL11.GL_TRIANGLES, 0, indexCount, indexCount, GL_UNSIGNED_INT, 0);

        if (hasNormal) glDisableClientState(GL_NORMAL_ARRAY);
        if (hasColor) glDisableClientState(GL_COLOR_ARRAY);
        if (hasTexCoord0 || hasTexCoord1) glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

}