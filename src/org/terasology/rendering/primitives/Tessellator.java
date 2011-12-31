package org.terasology.rendering.primitives;

import gnu.trove.list.array.TFloatArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.terasology.logic.manager.VertexBufferObjectManager;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Tessellator {

    /* SINGLETON */
    private static Tessellator _instance;

    private TFloatArrayList _color = new TFloatArrayList();
    private TFloatArrayList _vertices = new TFloatArrayList();
    private TFloatArrayList _tex = new TFloatArrayList();
    private TFloatArrayList _normals = new TFloatArrayList();

    private Vector4f _activeColor = new Vector4f();
    private Vector3f _activeNormal = new Vector3f();
    private Vector2f _activeTex = new Vector2f();
    private Vector3f _lighting = new Vector3f();

    public static Tessellator getInstance() {
        if (_instance == null)
            _instance = new Tessellator();

        return _instance;
    }

    private Tessellator() {
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
        _lighting.set(1, 1, 1);
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

        _tex.add(_lighting.x);
        _tex.add(_lighting.y);
        _tex.add(_lighting.z);
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

    public void setLighting(Vector3f v) {
        _lighting.set(v);
    }

    private FloatBuffer createCombinedBuffer() {
        final int size = _color.size() + _vertices.size() + _normals.size() + _tex.size();
        FloatBuffer buffer = BufferUtils.createFloatBuffer(size);

        int n = 0, t = 0, c = 0;
        for (int v = 0; v < _vertices.size(); v += 3, t += 5, c += 4, n += 3) {
            buffer.put(_vertices.get(v)).put(_vertices.get(v + 1)).put(_vertices.get(v + 2));
            buffer.put(_tex.get(t)).put(_tex.get(t + 1)).put(_tex.get(t + 2)).put(_tex.get(t + 3)).put(_tex.get(t + 4));
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

    public Mesh generateMesh() {
        Mesh mesh = new Mesh();
        mesh.vboVertexBuffer = VertexBufferObjectManager.getInstance().getVboId();
        mesh.vboIndexBuffer = VertexBufferObjectManager.getInstance().getVboId();

        FloatBuffer vertices = createCombinedBuffer();
        IntBuffer indices = createIndexBuffer();
        mesh.vertexCount = indices.capacity();

        VertexBufferObjectManager.getInstance().bufferVboData(mesh.vboVertexBuffer, vertices, GL15.GL_STATIC_DRAW);
        VertexBufferObjectManager.getInstance().bufferVboElementData(mesh.vboIndexBuffer, indices, GL15.GL_STATIC_DRAW);

        return mesh;
    }
}
