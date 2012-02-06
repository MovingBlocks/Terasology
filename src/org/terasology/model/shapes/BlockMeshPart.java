package org.terasology.model.shapes;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
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
    
}
