package org.terasology.rendering.primitives;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.terasology.logic.manager.VertexBufferObjectManager;
import org.terasology.model.shapes.BlockMeshPart;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Tessellator {

    private TFloatArrayList _color = new TFloatArrayList();
    private TFloatArrayList _vertices = new TFloatArrayList();
    private TFloatArrayList _tex = new TFloatArrayList();
    private TFloatArrayList _normals = new TFloatArrayList();
    private TIntArrayList _indices = new TIntArrayList();
    private int _indexOffset = 0;

    private Vector4f _activeColor = new Vector4f();
    private Vector3f _activeNormal = new Vector3f();
    private Vector2f _activeTex = new Vector2f();
    private Vector3f _lighting = new Vector3f();


    public Tessellator() {
        resetParams();
    }

    public void resetAll() {
        _color.reset();
        _vertices.reset();
        _tex.reset();
        _normals.reset();
        _indices.reset();
        _indexOffset = 0;

        resetParams();
    }

    public void resetParams() {
        _activeColor.set(1, 1, 1, 1);
        _activeTex.set(0, 0);
        _lighting.set(1, 1, 1);
        _activeNormal.set(0, 1, 0);
    }

    public void addPoly(Vector3f[] vertices, Vector2f[] texCoords) {
        if (vertices.length != texCoords.length || vertices.length < 3) {
            throw new IllegalArgumentException("addPoly expected vertices.length == texCoords.length > 2");
        }

        for (int i = 0; i < vertices.length; ++i) {
            _vertices.add(vertices[i].x);
            _vertices.add(vertices[i].y);
            _vertices.add(vertices[i].z);

            _color.add(_activeColor.x);
            _color.add(_activeColor.y);
            _color.add(_activeColor.z);
            _color.add(_activeColor.w);

            _normals.add(_activeNormal.x);
            _normals.add(_activeNormal.y);
            _normals.add(_activeNormal.z);

            _tex.add(texCoords[i].x);
            _tex.add(texCoords[i].y);
            _tex.add(0.0f);

            _tex.add(_lighting.x);
            _tex.add(_lighting.y);
            _tex.add(_lighting.z);
        }

        // Standard fan
        for (int i = 0; i < vertices.length - 2; i++) {
            _indices.add(_indexOffset);
            _indices.add(_indexOffset + i + 1);
            _indices.add(_indexOffset + i + 2);
        }
        _indexOffset += vertices.length;
    }

    public void addMeshPart(BlockMeshPart part) {
        for (int i = 0; i < part.size(); ++i) {
            Vector3f vertex = part.getVertex(i);
            _vertices.add(vertex.x);
            _vertices.add(vertex.y);
            _vertices.add(vertex.z);

            _color.add(_activeColor.x);
            _color.add(_activeColor.y);
            _color.add(_activeColor.z);
            _color.add(_activeColor.w);

            Vector3f normal = part.getNormal(i);
            _normals.add(normal.x);
            _normals.add(normal.y);
            _normals.add(normal.z);

            Vector2f uv = part.getTexCoord(i);
            _tex.add(uv.x);
            _tex.add(uv.y);
            _tex.add(0);

            _tex.add(_lighting.x);
            _tex.add(_lighting.y);
            _tex.add(_lighting.z);
        }

        for (int i = 0; i < part.indicesSize(); ++i) {
            _indices.add(_indexOffset + part.getIndex(i));
        }
        _indexOffset += part.size();
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
        for (int v = 0; v < _vertices.size(); v += 3, t += 6, c += 4, n += 3) {
            buffer.put(_vertices.get(v)).put(_vertices.get(v + 1)).put(_vertices.get(v + 2));
            buffer.put(_tex.get(t)).put(_tex.get(t + 1)).put(_tex.get(t + 2)).put(_tex.get(t + 3)).put(_tex.get(t + 4)).put(_tex.get(t + 5));
            buffer.put(_color.get(c)).put(_color.get(c + 1)).put(_color.get(c + 2)).put(_color.get(c + 3));
            buffer.put(_normals.get(n)).put(_normals.get(n + 1)).put(_normals.get(n + 2));
        }

        buffer.flip();
        return buffer;
    }

    private IntBuffer createIndexBuffer() {
        IntBuffer indices = BufferUtils.createIntBuffer(_indices.size());
        TIntIterator iterator = _indices.iterator();
        while (iterator.hasNext()) {
            indices.put(iterator.next());
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
