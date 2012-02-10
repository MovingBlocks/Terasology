package org.terasology.rendering.primitives;

import com.bulletphysics.collision.shapes.IndexedMesh;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.terasology.logic.manager.VertexBufferObjectManager;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL11.*;

/**
 * Chunk meshes are used to store the vertex data of tessellated chunks.
 */
public class ChunkMesh {

    /**
     * Data structure for storing vertex data. Abused like a "struct" in C/C++. Just sad.
     */
    public class VertexElements {

        public VertexElements() {
            vertCount = 0;
            normals = new TFloatArrayList();
            vertices = new TFloatArrayList();
            tex = new TFloatArrayList();
            color = new TFloatArrayList();
            indices = new TIntArrayList();
        }

        public final TFloatList normals;
        public final TFloatList vertices;
        public final TFloatList tex;
        public final TFloatList color;
        public final TIntList indices;
        public int vertCount;

        public FloatBuffer finalVertices;
        public IntBuffer finalIndices;
    }

    /**
     * Possible rendering types.
     */
    public enum RENDER_TYPE {
        OPAQUE, BILLBOARD_AND_TRANSLUCENT, WATER_AND_ICE
    }

    /* CONST */
    private static final int STRIDE = (3 + 3 + 3 + 3 + 4) * 4;
    private static final int OFFSET_VERTEX = 0;
    private static final int OFFSET_TEX_0 = (3 * 4);
    private static final int OFFSET_TEX_1 = ((3 + 3) * 4);
    private static final int OFFSET_COLOR = ((3 + 3 + 3) * 4);
    private static final int OFFSET_NORMAL = ((3 + 3 + 3 + 4) * 4);

    /* VERTEX DATA */
    private final int[] _vertexBuffers = new int[4];
    private final int[] _idxBuffers = new int[4];
    private final int[] _vertexCount = new int[4];

    /* STATS */
    private int _triangleCount = -1;

    /* TEMPORARY DATA */
    public VertexElements[] _vertexElements = new VertexElements[4];

    /* BULLET PHYSICS */
    public IndexedMesh _indexedMesh;
    private boolean _disposed = false;

    /* CONCURRENCY */
    public ReentrantLock _lock = new ReentrantLock();

    public ChunkMesh() {
        _vertexElements[0] = new VertexElements();
        _vertexElements[1] = new VertexElements();
        _vertexElements[2] = new VertexElements();
        _vertexElements[3] = new VertexElements();
    }

    /**
     * Generates the VBOs from the pre calculated arrays.
     *
     * @return True if something was generated
     */
    public boolean generateVBOs() {
        if (_lock.tryLock()) {
            try {
                // IMPORTANT: A mesh can only be generated once.
                if (_vertexElements == null || _disposed)
                    return false;

                for (int i = 0; i < _vertexBuffers.length; i++)
                    generateVBO(i);

                // Free unused space on the heap
                _vertexElements = null;
                // Calculate the final amount of triangles
                _triangleCount = (_vertexCount[0] + _vertexCount[1] + _vertexCount[2] + _vertexCount[3]) / 3;
            } finally {
                _lock.unlock();
            }

            return true;
        }

        return false;
    }

    private void generateVBO(int id) {
        if (_lock.tryLock()) {
            try {
                if (!_disposed) {
                    _vertexBuffers[id] = VertexBufferObjectManager.getInstance().getVboId();
                    _idxBuffers[id] = VertexBufferObjectManager.getInstance().getVboId();
                    _vertexCount[id] = _vertexElements[id].finalIndices.limit();

                    VertexBufferObjectManager.getInstance().bufferVboElementData(_idxBuffers[id], _vertexElements[id].finalIndices, GL15.GL_STATIC_DRAW);
                    VertexBufferObjectManager.getInstance().bufferVboData(_vertexBuffers[id], _vertexElements[id].finalVertices, GL15.GL_STATIC_DRAW);
                }
            } finally {
                _lock.unlock();
            }
        }
    }

    private void renderVbo(int id) {
        if (_lock.tryLock()) {
            try {
                if (_vertexBuffers[id] <= 0 || _disposed)
                    return;

                glEnableClientState(GL_VERTEX_ARRAY);
                glEnableClientState(GL_TEXTURE_COORD_ARRAY);
                glEnableClientState(GL_COLOR_ARRAY);
                glEnableClientState(GL_NORMAL_ARRAY);

                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, _idxBuffers[id]);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, _vertexBuffers[id]);

                glVertexPointer(3, GL11.GL_FLOAT, STRIDE, OFFSET_VERTEX);

                GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
                glTexCoordPointer(3, GL11.GL_FLOAT, STRIDE, OFFSET_TEX_0);

                GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
                glTexCoordPointer(3, GL11.GL_FLOAT, STRIDE, OFFSET_TEX_1);

                glColorPointer(4, GL11.GL_FLOAT, STRIDE, OFFSET_COLOR);

                glNormalPointer(GL11.GL_FLOAT, STRIDE, OFFSET_NORMAL);

                GL11.glDrawElements(GL11.GL_TRIANGLES, _vertexCount[id], GL_UNSIGNED_INT, 0);

                glDisableClientState(GL_NORMAL_ARRAY);
                glDisableClientState(GL_COLOR_ARRAY);
                glDisableClientState(GL_TEXTURE_COORD_ARRAY);
                glDisableClientState(GL_VERTEX_ARRAY);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            } finally {
                _lock.unlock();
            }
        }
    }

    public void render(RENDER_TYPE type) {
        switch (type) {
            case OPAQUE:
                renderVbo(0);
                break;
            case BILLBOARD_AND_TRANSLUCENT:
                renderVbo(1);
                // BILLBOARDS
                glDisable(GL_CULL_FACE);
                renderVbo(2);
                glEnable(GL_CULL_FACE);
                break;
            case WATER_AND_ICE:
                renderVbo(3);
                break;
        }
    }

    public void dispose() {
        _lock.lock();

        try {
            if (!_disposed) {
                for (int i = 0; i < _vertexBuffers.length; i++) {
                    int id = _vertexBuffers[i];

                    VertexBufferObjectManager.getInstance().putVboId(id);
                    _vertexBuffers[i] = 0;

                    id = _idxBuffers[i];

                    VertexBufferObjectManager.getInstance().putVboId(id);
                    _idxBuffers[i] = 0;
                }

                _disposed = true;
                _vertexElements = null;
                _indexedMesh = null;
            }
        } finally {
            _lock.unlock();
        }
    }

    public boolean isGenerated() {
        return _vertexElements == null;
    }

    public boolean isDisposed() {
        return _disposed;
    }

    public int triangleCount() {
        return _triangleCount;
    }

    public boolean isEmpty() {
        return _triangleCount == 0;
    }
}
