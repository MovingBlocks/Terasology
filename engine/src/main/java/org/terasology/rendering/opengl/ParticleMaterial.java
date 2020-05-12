/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.rendering.opengl;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ParticleMaterial {

    private final Vector3f color = new Vector3f();
    private final Matrix4f viewProjection = new Matrix4f();
    private final GLSLParticleShader shader;

    public ParticleMaterial(GLSLParticleShader shader) {
        this.shader = shader;
    }

    public void enable() {
        shader.bind();
        shader.setColor(color.x, color.y, color.z);
        shader.setViewProjection(viewProjection);
    }

    public void disable() {
        shader.unbind();
    }

    public void setColor(Vector3fc newColor) {
        color.set(newColor);
    }

    public void setViewProjectionMatrix(Matrix4fc matrix) {
        viewProjection.set(matrix);
    }
}
