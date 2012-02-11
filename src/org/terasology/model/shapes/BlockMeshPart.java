package org.terasology.model.shapes;

import org.terasology.rendering.primitives.ChunkMesh;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.Arrays;

/**
 * Describes the elements composing part of a block mesh. Multiple parts are patched together to define the mesh
 * for a block, or its appearance in the world.
 * @author Immortius <immortius@gmail.com>
 */
public class BlockMeshPart {
    private Vector3f[] _vertices;
    private Vector3f[] _normals;
    private Vector2f[] _texCoords;
    private int[] _indices;
    
    public BlockMeshPart(Vector3f[] vertices, Vector3f[] normals, Vector2f[] texCoords, int[] indices)
    {
        _vertices = Arrays.copyOf(vertices, vertices.length);
        _normals = Arrays.copyOf(normals, normals.length);
        _texCoords = Arrays.copyOf(texCoords, texCoords.length);
        _indices = Arrays.copyOf(indices, indices.length);
    }
    
    public int size()
    {
        return _vertices.length;
    }
    
    public int indicesSize()
    {
        return _indices.length;
    }
    
    public Vector3f getVertex(int i)
    {
        return _vertices[i];
    }
    
    public Vector3f getNormal(int i)
    {
        return _normals[i];
    }
    
    public Vector2f getTexCoord(int i)
    {
        return _texCoords[i];
    }
    
    public int getIndex(int i)
    {
        return _indices[i];
    }

    public BlockMeshPart mapTexCoords(Vector2f offset, float width)
    {
        Vector2f[] newTexCoords = new Vector2f[_texCoords.length];
        for (int i = 0; i < newTexCoords.length; ++i) {
            newTexCoords[i] = new Vector2f(offset.x + _texCoords[i].x * width, offset.y + _texCoords[i].y * width);
        }
        return new BlockMeshPart(_vertices, _normals, newTexCoords, _indices);
    }

    public void appendTo(ChunkMesh chunk, int offsetX, int offsetY, int offsetZ, Vector4f colorOffset, int meshBit)
    {
        for (Vector2f texCoord : _texCoords)
        {
            chunk._vertexElements[meshBit].tex.add(texCoord.x);
            chunk._vertexElements[meshBit].tex.add(texCoord.y);
            chunk._vertexElements[meshBit].tex.add(1.0f);
        }

        int nextIndex = chunk._vertexElements[meshBit].vertCount;
        for (int vIdx = 0; vIdx < _vertices.length; ++vIdx)
        {
            chunk._vertexElements[meshBit].color.add(colorOffset.x);
            chunk._vertexElements[meshBit].color.add(colorOffset.y);
            chunk._vertexElements[meshBit].color.add(colorOffset.z);
            chunk._vertexElements[meshBit].color.add(colorOffset.w);
            chunk._vertexElements[meshBit].vertices.add(_vertices[vIdx].x + offsetX);
            chunk._vertexElements[meshBit].vertices.add(_vertices[vIdx].y + offsetY);
            chunk._vertexElements[meshBit].vertices.add(_vertices[vIdx].z + offsetZ);
            chunk._vertexElements[meshBit].normals.add(_normals[vIdx].x);
            chunk._vertexElements[meshBit].normals.add(_normals[vIdx].y);
            chunk._vertexElements[meshBit].normals.add(_normals[vIdx].z);
        }
        chunk._vertexElements[meshBit].vertCount += _vertices.length;

        for (int i = 0; i < _indices.length; ++i)
        {
            chunk._vertexElements[meshBit].indices.add(_indices[i] + nextIndex);
        }
    }
}
