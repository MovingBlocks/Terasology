package com.github.begla.blockmania.world;

import gnu.trove.list.array.TFloatArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

public class ChunkMesh extends RenderableObject {

    public class VertexElements {
        public VertexElements() {
            quads = new TFloatArrayList();
            normals = new TFloatArrayList();
            tex = new TFloatArrayList();
            color = new TFloatArrayList();
            light = new TFloatArrayList();
        }

        public TFloatArrayList quads;
        public TFloatArrayList normals;
        public TFloatArrayList tex;
        public TFloatArrayList color;
        public TFloatArrayList light;
    }

    private int[] _vertexBuffers = new int[3];
    private int[] _idxBuffers = new int[3];
    private int[] _idxBufferCount = new int[3];
    public VertexElements[] _vertexElements = new VertexElements[3];

    boolean _generated;

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

        FloatBuffer vertices = BufferUtils.createFloatBuffer(_vertexElements[id].quads.size() + _vertexElements[id].tex.size() + _vertexElements[id].light.size() + _vertexElements[id].color.size());
        IntBuffer idxBuffer = BufferUtils.createIntBuffer(_vertexElements[id].quads.size());

        HashMap<Vector3f, Integer> indexLut = new HashMap<Vector3f, Integer>();

        int tex = 0;
        int color = 0;
        int idxCounter = 0;
        for (int i = 0; i < _vertexElements[id].quads.size(); i += 3, tex += 2, color += 4) {

            Vector3f vertexPos = new Vector3f(_vertexElements[id].quads.get(i), _vertexElements[id].quads.get(i + 1), _vertexElements[id].quads.get(i + 2));

            // Check if this vertex is a new one
            if (indexLut.containsKey(vertexPos)) {
                int index = indexLut.get(vertexPos);
                idxBuffer.put(index);
                continue;
            }

            vertices.put(vertexPos.x);
            vertices.put(vertexPos.y);
            vertices.put(vertexPos.z);

            vertices.put(_vertexElements[id].tex.get(tex));
            vertices.put(_vertexElements[id].tex.get(tex + 1));

            vertices.put(_vertexElements[id].light.get(tex));
            vertices.put(_vertexElements[id].light.get(tex + 1));

            vertices.put(_vertexElements[id].color.get(color));
            vertices.put(_vertexElements[id].color.get(color + 1));
            vertices.put(_vertexElements[id].color.get(color + 2));
            vertices.put(_vertexElements[id].color.get(color + 3));

            // Log this vertex
            indexLut.put(vertexPos, idxCounter);
            idxBuffer.put(idxCounter++);
        }

        idxBuffer.flip();
        vertices.flip();

        _idxBufferCount[id] = idxBuffer.limit();

        bufferVboElementData(_idxBuffers[id], idxBuffer);
        bufferVboData(_vertexBuffers[id], vertices);
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
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_ALPHA_TEST);
            glAlphaFunc(GL_GREATER, 0.5f);

            renderVbo(1);

            glDisable(GL_CULL_FACE);
            renderVbo(2);
            glEnable(GL_CULL_FACE);

            glDisable(GL_BLEND);
            glDisable(GL_ALPHA_TEST);
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
        ARBVertexBufferObject.glDeleteBuffersARB(ib);
    }

    public static int createVboId() {
        if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            IntBuffer buffer = BufferUtils.createIntBuffer(1);
            ARBVertexBufferObject.glGenBuffersARB(buffer);
            return buffer.get(0);
        }
        return 0;
    }

    public static void bufferVboData(int id, FloatBuffer buffer) {
        if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, id);
            ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, buffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
        }
    }

    public static void bufferVboElementData(int id, IntBuffer buffer) {
        if (GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
            ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, id);
            ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, buffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
        }
    }
}
