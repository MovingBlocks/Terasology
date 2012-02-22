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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.terasology.rendering.interfaces.IGameObject;

import static org.lwjgl.opengl.GL11.*;

public class Mesh implements IGameObject {
    private static final int STRIDE = (3 + 3 + 3 + 3 + 4) * 4;
    private static final int OFFSET_VERTEX = 0;
    private static final int OFFSET_TEX_0 = (3 * 4);
    private static final int OFFSET_TEX_1 = ((3 + 3) * 4);
    private static final int OFFSET_COLOR = ((3 + 3 + 3) * 4);
    private static final int OFFSET_NORMAL = ((3 + 3 + 3 + 4) * 4);

    public int vboVertexBuffer;
    public int vboIndexBuffer;
    public int vertexCount;

    public void render() {
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboVertexBuffer);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexBuffer);

        glVertexPointer(3, GL11.GL_FLOAT, STRIDE, OFFSET_VERTEX);

        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
        glTexCoordPointer(3, GL11.GL_FLOAT, STRIDE, OFFSET_TEX_0);

        GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
        glTexCoordPointer(3, GL11.GL_FLOAT, STRIDE, OFFSET_TEX_1);

        glColorPointer(4, GL11.GL_FLOAT, STRIDE, OFFSET_COLOR);
        glNormalPointer(GL11.GL_FLOAT, STRIDE, OFFSET_NORMAL);

        GL12.glDrawRangeElements(GL11.GL_TRIANGLES, 0, vertexCount, vertexCount, GL_UNSIGNED_INT, 0);

        glDisableClientState(GL_NORMAL_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void update() {
    }
}