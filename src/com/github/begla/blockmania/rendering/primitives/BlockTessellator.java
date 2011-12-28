package com.github.begla.blockmania.rendering.primitives;

import com.github.begla.blockmania.logic.manager.VertexBufferObjectManager;
import com.github.begla.blockmania.rendering.interfaces.RenderableObject;
import gnu.trove.list.array.TFloatArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public class BlockTessellator {

    /* SINGLETON */
    private static BlockTessellator _instance;

    public class BlockMesh implements RenderableObject {
        public final int STRIDE = (3 + 3 + 2 + 4) * 4;
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

            glVertexPointer(3, GL11.GL_FLOAT, STRIDE, 0);

            GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
            glTexCoordPointer(2, GL11.GL_FLOAT, STRIDE, 12);

            glColorPointer(4, GL11.GL_FLOAT, STRIDE, 20);
            glNormalPointer(GL11.GL_FLOAT, STRIDE, 36);

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

    private TFloatArrayList _color = new TFloatArrayList();
    private TFloatArrayList _vertices = new TFloatArrayList();
    private TFloatArrayList _tex = new TFloatArrayList();
    private TFloatArrayList _normals = new TFloatArrayList();

    private Vector4f _activeColor = new Vector4f();
    private Vector3f _activeNormal = new Vector3f();
    private Vector2f _activeTex = new Vector2f();

    public static BlockTessellator getInstance() {
        if (_instance == null)
            _instance = new BlockTessellator();

        return _instance;
    }

    private BlockTessellator() {
        resetAll();
    }

    public void resetAll() {
        _color.reset();
        _vertices.reset();
        _tex.reset();
        _normals.reset();

        resetParams();
    }

    public void resetParams() {
        _activeColor.set(1, 1, 1, 1);
        _activeTex.set(0, 0);
        _activeNormal.set(0, 1, 0);
    }

    public void addVertex(Vector3f v) {
        _vertices.add(v.x);
        _vertices.add(v.y);
        _vertices.add(v.z);

        _color.add(_activeColor.x);
        _color.add(_activeColor.y);
        _color.add(_activeColor.z);
        _color.add(_activeColor.w);

        _normals.add(_activeNormal.x);
        _normals.add(_activeNormal.y);
        _normals.add(_activeNormal.z);

        _tex.add(_activeTex.x);
        _tex.add(_activeTex.y);
    }

    public void setColor(Vector4f v) {
        _activeColor.set(v);
    }

    public void setNormal(Vector3f v) {
        _activeNormal.set(v);
    }

    public void setTex(Vector2f v) {
        _activeTex.set(v);
    }

    private FloatBuffer createCombinedBuffer() {
        final int size = _color.size() + _vertices.size() + _normals.size() + _tex.size();
        FloatBuffer buffer = BufferUtils.createFloatBuffer(size);

        int n = 0, t = 0, c = 0;
        for (int v = 0; v < _vertices.size(); v += 3, t += 2, c += 4, n += 3) {
            buffer.put(_vertices.get(v)).put(_vertices.get(v + 1)).put(_vertices.get(v + 2));
            buffer.put(_tex.get(t)).put(_tex.get(t + 1));
            buffer.put(_color.get(c)).put(_color.get(c + 1)).put(_color.get(c + 2)).put(_color.get(c + 3));
            buffer.put(_normals.get(n)).put(_normals.get(n + 1)).put(_normals.get(n + 2));
        }

        buffer.flip();
        return buffer;
    }

    private IntBuffer createIndexBuffer() {
        IntBuffer indices = BufferUtils.createIntBuffer(((_vertices.size() / 3) / 4) * 6);

        int idx = 0;
        for (int i = 0; i < _vertices.size(); i += 3) {
            if (i % 4 == 0) {
                indices.put(idx);
                indices.put(idx + 1);
                indices.put(idx + 2);

                indices.put(idx + 2);
                indices.put(idx + 3);
                indices.put(idx);

                idx += 4;
            }
        }

        indices.flip();
        return indices;
    }

    public BlockMesh generateBlockMesh() {
        BlockMesh blockMesh = new BlockMesh();
        blockMesh.vboVertexBuffer = VertexBufferObjectManager.getInstance().getVboId();
        blockMesh.vboIndexBuffer = VertexBufferObjectManager.getInstance().getVboId();

        FloatBuffer vertices = createCombinedBuffer();
        IntBuffer indices = createIndexBuffer();
        blockMesh.vertexCount = indices.capacity();

        VertexBufferObjectManager.getInstance().bufferVboData(blockMesh.vboVertexBuffer, vertices, GL15.GL_STATIC_DRAW);
        VertexBufferObjectManager.getInstance().bufferVboElementData(blockMesh.vboIndexBuffer, indices, GL15.GL_STATIC_DRAW);

        return blockMesh;
    }
}
