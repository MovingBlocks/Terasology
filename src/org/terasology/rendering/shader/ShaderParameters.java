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
package org.terasology.rendering.shader;

import org.lwjgl.opengl.GL20;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.utilities.Helper;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class ShaderParameters {

    protected String _shaderDesc;

    public ShaderParameters(String shaderDesc) {
        _shaderDesc = shaderDesc;
    }

    public abstract void applyParameters();

    public void setFloat(String desc, float f) {
        int id = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader(_shaderDesc), desc);
        GL20.glUniform1f(id, f);
    }

    public void setFloat3(String desc, Vector3f v) {
        setFloat3(desc, v.x, v.y, v.z);
    }

    public void setFloat3(String desc, float f1, float f2, float f3) {
        int id = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader(_shaderDesc), desc);
        GL20.glUniform3f(id, f1, f2, f3);
    }

    public void setFloat4(String desc, float f1, float f2, float f3, float f4) {
        int id = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader(_shaderDesc), desc);
        GL20.glUniform4f(id, f1, f2, f3, f4);
    }

    public void setInt(String desc, int i) {
        int id = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader(_shaderDesc), desc);
        GL20.glUniform1f(id, i);
    }

    public void setFloat2(String desc, FloatBuffer buffer) {
        int id = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader(_shaderDesc), desc);
        GL20.glUniform2(id, buffer);
    }

    public void setFloat1(String desc, FloatBuffer buffer) {
        int id = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader(_shaderDesc), desc);
        GL20.glUniform1(id, buffer);
    }
}
