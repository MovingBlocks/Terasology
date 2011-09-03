package com.github.begla.blockmania.world;

import gnu.trove.list.array.TFloatArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public class ChunkMesh extends RenderableObject {

    public class VertexElements {
        public VertexElements() {
            quads = new TFloatArrayList();
            tex = new TFloatArrayList();
            color = new TFloatArrayList();
        }

        public final TFloatArrayList quads;
        public final TFloatArrayList tex;
        public final TFloatArrayList color;

        public FloatBuffer vertices;
        public IntBuffer indices;
    }

    private final int[] _vertexBuffers = new int[3];
    private final int[] _idxBuffers = new int[3];
    private final int[] _idxBufferCount = new int[3];
    public VertexElements[] _vertexElements = new VertexElements[3];

    private boolean _generated;

    public ChunkMesh() {
        _vertexElements[0] = new VertexElements();
        _vertexElements[1] = new VertexElements();
        _vertexElements[2] = new VertexElements();
    }

    /**
     * Generates the display lists from the pre calculated arrays.
     */
    public void generateVBOs() {
        // IMPORTANT: A mesh can only be generated once.
        if (_generated)
            return;

        for (int i = 0; i < 3; i++)
            generateVBO(i);

        // IMPORTANT: Free unused memory!!
        _vertexElements = null;
        // Make sure this mesh can not be generated again
        _generated = true;
    }

    private void generateVBO(int id) {
        _vertexBuffers[id] = createVboId();
        _idxBuffers[id] = createVboId();
        _idxBufferCount[id] = _vertexElements[id].indices.limit();

        bufferVboElementData(_idxBuffers[id], _vertexElements[id].indices);
        bufferVboData(_vertexBuffers[id], _vertexElements[id].vertices);
    }

    private void renderVbo(int id) {

        if (_vertexBuffers[id] == -1)
            return;

        int stride = (3 + 2 + 2 + 4) * 4;

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, _idxBuffers[id]);
        ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, _vertexBuffers[id]);

        int offset = (0 * 4);

        glVertexPointer(3, GL11.GL_FLOAT, stride, offset);

        offset = (3 * 4);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);

        glTexCoordPointer(2, GL11.GL_FLOAT, stride, offset);

        offset = ((2 + 3) * 4);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE1);

        glTexCoordPointer(2, GL11.GL_FLOAT, stride, offset);

        offset = ((2 + 3 + 2) * 4);

        glColorPointer(4, GL11.GL_FLOAT, stride, offset);

        GL12.glDrawRangeElements(GL11.GL_QUADS, 0, _idxBufferCount[id], _idxBufferCount[id], GL_UNSIGNED_INT, 0);

        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
    }

    public void render(boolean translucent) {
        if (!translucent) {
            renderVbo(0);
        } else {
            // Render two passes: The first one only writes to the depth buffer, the second one to the frame buffer
            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    glColorMask(false, false, false, false);
                } else {
                    glColorMask(true, true, true, true);
                }
                glDisable(GL_CULL_FACE);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

                // WATER
                renderVbo(1);

                glDisable(GL_BLEND);
                glEnable(GL_CULL_FACE);
            }

            glEnable(GL_BLEND);
            glDisable(GL_CULL_FACE);

            // BILLBOARDS
            renderVbo(2);

            glEnable(GL_CULL_FACE);
            glDisable(GL_BLEND);

        }
    }

    @Override
    public void render() {
        render(false);
        render(true);
    }

    @Override
    public void update() {
        // Do nothing.
    }

    public boolean isGenerated() {
        return _generated;
    }

    public void dispose() {
        // Remove the old VBOs.
        IntBuffer ib = BufferUtils.createIntBuffer(6);
        ib.put(_vertexBuffers[0]);
        ib.put(_vertexBuffers[1]);
        ib.put(_vertexBuffers[2]);
        ib.put(_idxBuffers[0]);
        ib.put(_idxBuffers[1]);
        ib.put(_idxBuffers[2]);
        ib.flip();

        _vertexBuffers[0] = -1;
        _vertexBuffers[1] = -1;
        _vertexBuffers[2] = -1;
        _idxBuffers[0] = -1;
        _idxBuffers[1] = -1;
        _idxBuffers[2] = -1;

        ARBVertexBufferObject.glDeleteBuffersARB(ib);
    }

    private static int createVboId() {
        if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            IntBuffer buffer = BufferUtils.createIntBuffer(1);
            ARBVertexBufferObject.glGenBuffersARB(buffer);
            return buffer.get(0);
        }
        return 0;
    }

    private static void bufferVboData(int id, FloatBuffer buffer) {
        if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, id);
            ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, buffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
        }
    }

    private static void bufferVboElementData(int id, IntBuffer buffer) {
        if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, id);
            ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, buffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
        }
    }
}
