/*
 * Copyright 2014 MovingBlocks
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

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetUri;
import org.terasology.math.MatrixUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.material.BaseMaterial;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.shader.ShaderParameterMetadata;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.shader.ShaderParameters;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GLSLMaterial extends BaseMaterial {

    private static final Logger logger = LoggerFactory.getLogger(GLSLMaterial.class);

    private int textureIndex;

    private TIntIntMap shaderPrograms = new TIntIntHashMap();
    private TObjectIntMap<String> bindMap = new TObjectIntHashMap<>();
    private TIntObjectMap<Texture> textureMap = new TIntObjectHashMap<>();
    private GLSLShader shader;
    private boolean activeFeaturesChanged;
    private TObjectIntMap<UniformId> uniformLocationMap = new TObjectIntHashMap<>();

    private EnumSet<ShaderProgramFeature> activeFeatures = Sets.newEnumSet(Collections.<ShaderProgramFeature>emptyList(), ShaderProgramFeature.class);
    private int activeFeaturesMask;

    private ShaderManager shaderManager;
    private ShaderParameters shaderParameters;

    public GLSLMaterial(AssetUri uri, MaterialData data) {
        super(uri);
        shaderManager = CoreRegistry.get(ShaderManager.class);
        reload(data);
    }

    public ShaderParameters getShaderParameters() {
        return shaderParameters;
    }

    public void setShaderParameters(ShaderParameters param) {
        this.shaderParameters = param;
        param.initialParameters(this);
    }

    @Override
    public void enable() {
        if (shaderManager.getActiveMaterial() != this || activeFeaturesChanged) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL20.glUseProgram(getActiveShaderProgramId());

            // Make sure the shader manager knows that this program is currently active
            shaderManager.setActiveMaterial(this);
            activeFeaturesChanged = false;

            // Set the shader parameters if available
            if (shaderParameters != null) {
                shaderParameters.applyParameters(this);
            }
        }
    }

    @Override
    public void bindTextures() {
        if (isDisposed()) {
            return;
        }

        enable();
        for (int slot : textureMap.keys()) {
            shaderManager.bindTexture(slot, textureMap.get(slot));
        }
    }

    @Override
    public void recompile() {
        TIntIntIterator it = shaderPrograms.iterator();
        while (it.hasNext()) {
            it.advance();
            GL20.glDeleteProgram(it.value());
        }
        shaderPrograms.clear();
        uniformLocationMap.clear();

        shaderPrograms.put(0, shader.linkShaderProgram(0));
        for (Set<ShaderProgramFeature> permutation : Sets.powerSet(shader.getAvailableFeatures())) {
            int featureMask = ShaderProgramFeature.getBitset(permutation);
            shaderPrograms.put(featureMask, shader.linkShaderProgram(featureMask));
        }
        if (shaderParameters != null) {
            shaderParameters.initialParameters(this);
        }
    }

    @Override
    public final void reload(MaterialData data) {
        dispose();

        shader = (GLSLShader) data.getShader();
        recompile();

        for (Map.Entry<String, Texture> entry : data.getTextures().entrySet()) {
            setTexture(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Float> entry : data.getFloatParams().entrySet()) {
            setFloat(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : data.getIntegerParams().entrySet()) {
            setInt(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, float[]> entry : data.getFloatArrayParams().entrySet()) {
            switch (entry.getValue().length) {
                case 1:
                    setFloat(entry.getKey(), entry.getValue()[0]);
                    break;
                case 2:
                    setFloat2(entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
                    break;
                case 3:
                    setFloat3(entry.getKey(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2]);
                    break;
                case 4:
                    setFloat4(entry.getKey(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2], entry.getValue()[3]);
                    break;
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing material {}.", getURI());

        TIntIntIterator it = shaderPrograms.iterator();
        while (it.hasNext()) {
            it.advance();
            GL20.glDeleteProgram(it.value());
        }
        shaderPrograms.clear();
        uniformLocationMap.clear();
        shader = null;
    }

    @Override
    public boolean isDisposed() {
        return shader == null;
    }

    @Override
    public void setTexture(String desc, Texture texture) {
        if (isDisposed()) {
            return;
        }

        int texId;
        if (bindMap.containsKey(desc)) {
            texId = bindMap.get(desc);
        } else {
            // TODO: do this initially, and try and have similar textures in similar slots for all materials.
            ShaderParameterMetadata metadata = shader.getParameter(desc);
            if (metadata == null || !metadata.getType().isTexture()) {
                return;
            }
            texId = textureIndex++;

            // Make sure to bind the texture for all permutations
            setInt(desc, texId);

            bindMap.put(desc, texId);
        }

        textureMap.put(texId, texture);
    }

    @Override
    public void activateFeature(ShaderProgramFeature feature) {
        if (shader.getAvailableFeatures().contains(feature)) {
            activeFeatures.add(feature);
            activeFeaturesMask = ShaderProgramFeature.getBitset(activeFeatures);
            activeFeaturesChanged = true;
        } else {
            logger.error("Attempt to activate unsupported feature {} for material {} using shader {}", feature, getURI(), shader.getURI());
        }
    }

    @Override
    public void deactivateFeature(ShaderProgramFeature feature) {
        if (activeFeatures.remove(feature)) {
            activeFeaturesMask = ShaderProgramFeature.getBitset(activeFeatures);
            activeFeaturesChanged = true;
        }
    }

    @Override
    public void deactivateFeatures(ShaderProgramFeature... features) {
        for (ShaderProgramFeature feature : Arrays.asList(features)) {
            deactivateFeature(feature);
        }
    }

    @Override
    public boolean supportsFeature(ShaderProgramFeature feature) {
        return shader.getAvailableFeatures().contains(feature);
    }

    @Override
    public void setFloat(String desc, float f, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniform1f(id, f);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform1f(id, f);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat1(String desc, FloatBuffer buffer, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniform1(id, buffer);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform1(id, buffer);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat2(String desc, float f1, float f2, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniform2f(id, f1, f2);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform2f(id, f1, f2);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat2(String desc, FloatBuffer buffer, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniform2(id, buffer);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform2(id, buffer);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat3(String desc, float f1, float f2, float f3, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniform3f(id, f1, f2, f3);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform3f(id, f1, f2, f3);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat3(String desc, FloatBuffer buffer, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniform3(id, buffer);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform3(id, buffer);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat4(String desc, float f1, float f2, float f3, float f4, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniform4f(id, f1, f2, f3, f4);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform4f(id, f1, f2, f3, f4);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat4(String desc, FloatBuffer buffer, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniform4(id, buffer);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform4(id, buffer);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setInt(String desc, int i, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniform1i(id, i);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform1i(id, i);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setBoolean(String desc, boolean value, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniform1i(id, value ? 1 : 0);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform1i(id, value ? 1 : 0);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setMatrix3(String desc, Matrix3f value, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniformMatrix3(id, false, MatrixUtils.matrixToFloatBuffer(value));
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniformMatrix3(id, false, MatrixUtils.matrixToFloatBuffer(value));
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setMatrix3(String desc, FloatBuffer value, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniformMatrix3(id, false, value);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniformMatrix3(id, false, value);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setMatrix4(String desc, Matrix4f value, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniformMatrix4(id, false, MatrixUtils.matrixToFloatBuffer(value));
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniformMatrix4(id, false, MatrixUtils.matrixToFloatBuffer(value));
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setMatrix4(String desc, FloatBuffer value, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniformMatrix4(id, false, value);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniformMatrix4(id, false, value);
            }

            restoreStateAfterUniformsSet();
        }
    }

    private int getActiveShaderProgramId() {
        return shaderPrograms.get(activeFeaturesMask);
    }

    private int getUniformLocation(int activeShaderProgramId, String desc) {
        UniformId id = new UniformId(activeShaderProgramId, desc);

        if (uniformLocationMap.containsKey(id)) {
            return uniformLocationMap.get(id);
        }

        int loc = GL20.glGetUniformLocation(activeShaderProgramId, desc);
        uniformLocationMap.put(id, loc);

        return loc;
    }

    private void restoreStateAfterUniformsSet() {
        if (shaderManager.getActiveMaterial() == this) {
            GL20.glUseProgram(getActiveShaderProgramId());
        } else {
            enable();
        }
    }

    private static final class UniformId {
        private int shaderProgramId;
        private String name;

        public UniformId(int shaderProgramId, String name) {
            this.shaderProgramId = shaderProgramId;
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof UniformId) {
                UniformId other = (UniformId) obj;
                return shaderProgramId == other.shaderProgramId && Objects.equal(name, other.name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(shaderProgramId, name);
        }
    }


}
