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

import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.model.shapes.BlockMeshPart;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class Tessellator {

    private TFloatArrayList _color = new TFloatArrayList();
    private TFloatArrayList _vertices = new TFloatArrayList();
    private TFloatArrayList _texCoord0 = new TFloatArrayList();
    private TFloatArrayList _texCoord1 = new TFloatArrayList();
    private TFloatArrayList _normals = new TFloatArrayList();
    private TIntArrayList _indices = new TIntArrayList();
    private int _indexOffset = 0;

    private Vector4f _activeColor = new Vector4f();
    private Vector3f _activeNormal = new Vector3f();
    private Vector2f _activeTex = new Vector2f();
    private Vector3f _lighting = new Vector3f();

    private boolean useLighting = true;
    private boolean useNormals = true;


    public Tessellator() {
        resetParams();
    }

    public void resetAll() {
        _color.reset();
        _vertices.reset();
        _texCoord0.reset();
        _texCoord1.reset();
        _normals.reset();
        _indices.reset();
        _indexOffset = 0;

        resetParams();
    }

    public void setUseLighting(boolean enable) {
        this.useLighting = enable;
    }

    public void setUseNormals(boolean enable) {
        this.useNormals = enable;
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

            if (useNormals) {
                _normals.add(_activeNormal.x);
                _normals.add(_activeNormal.y);
                _normals.add(_activeNormal.z);
            }

            _texCoord0.add(texCoords[i].x);
            _texCoord0.add(texCoords[i].y);

            if (useLighting) {
                _texCoord1.add(_lighting.x);
                _texCoord1.add(_lighting.y);
                _texCoord1.add(_lighting.z);
            }
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
            _texCoord0.add(uv.x);
            _texCoord0.add(uv.y);

            _texCoord1.add(_lighting.x);
            _texCoord1.add(_lighting.y);
            _texCoord1.add(_lighting.z);
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


    public Mesh generateMesh() {
        return Mesh.buildMesh(_vertices, _texCoord0, _texCoord1, _normals, _color, _indices);
    }
}
