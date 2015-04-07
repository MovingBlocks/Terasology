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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetUri;
import org.terasology.math.MatrixUtils;
import org.terasology.math.geom.Matrix3f;
import org.terasology.math.geom.Matrix4f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.material.BaseMaterial;
import org.terasology.rendering.assets.material.MaterialData;
import org.terasology.rendering.assets.shader.ShaderParameterMetadata;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.shader.ShaderParameters;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.terasology.rendering.assets.material.Material.StorageQualifier.ATTRIBUTE;
import static org.terasology.rendering.assets.material.Material.StorageQualifier.UNIFORM;

/**
 * @author Immortius
 * @author Benjamin Glatzel
 */
public class GLSLMaterial extends BaseMaterial {

    private static final Logger logger = LoggerFactory.getLogger(GLSLMaterial.class);

    private int textureIndex;

    private TIntIntMap shaderPrograms = new TIntIntHashMap();
    private TObjectIntMap<String> bindMap = new TObjectIntHashMap<>();
    private TIntObjectMap<Texture> textureMap = new TIntObjectHashMap<>();
    private GLSLShader shader;
    private boolean activeFeaturesChanged;
    private TObjectIntMap<ParameterId> parameterLocationMap = new TObjectIntHashMap<>();

    private EnumSet<ShaderProgramFeature> activeFeatures = Sets.newEnumSet(Collections.<ShaderProgramFeature>emptyList(), ShaderProgramFeature.class);
    private int activeFeaturesMask;

    private ShaderManager shaderManager;
    private ShaderParameters shaderParameters;
    private boolean disposed;

    public GLSLMaterial(AssetUri uri, MaterialData data) {
        super(uri);
        shaderManager = CoreRegistry.get(ShaderManager.class);
        onReload(data);
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
        parameterLocationMap.clear();

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
    public final void onReload(MaterialData data) {
        disposeData();

        shader = (GLSLShader) data.getShader();
        recompile();

        for (Map.Entry<String, Texture> entry : data.getTextures().entrySet()) {
            setTexture(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Float> entry : data.getFloatUniforms().entrySet()) {
            setFloat(UNIFORM, entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Float> entry : data.getFloatAttributes().entrySet()) {
            setFloat(ATTRIBUTE, entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : data.getIntegerUniforms().entrySet()) {
            setInt(UNIFORM, entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, float[]> entry : data.getFloatArrayUniforms().entrySet()) {
            switch (entry.getValue().length) {
                case 1:
                    setFloat(UNIFORM, entry.getKey(), entry.getValue()[0]);
                    break;
                case 2:
                    setFloat2(UNIFORM, entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
                    break;
                case 3:
                    setFloat3(UNIFORM, entry.getKey(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2]);
                    break;
                case 4:
                    setFloat4(UNIFORM, entry.getKey(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2], entry.getValue()[3]);
                    break;
                default:
                    logger.error("MaterialData contains float array entry of size > 4");
                    break;
            }
        }

        for (Map.Entry<String, float[]> entry : data.getFloatArrayAttributes().entrySet()) {
            switch (entry.getValue().length) {
                case 1:
                    setFloat(ATTRIBUTE, entry.getKey(), entry.getValue()[0]);
                    break;
                case 2:
                    setFloat2(ATTRIBUTE, entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
                    break;
                case 3:
                    setFloat3(ATTRIBUTE, entry.getKey(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2]);
                    break;
                case 4:
                    setFloat4(ATTRIBUTE, entry.getKey(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2], entry.getValue()[3]);
                    break;
                default:
                    logger.error("MaterialData contains float array entry of size > 4");
                    break;
            }
        }
    }

    @Override
    protected void onDispose() {
        logger.debug("Disposing material {}.", getURI());
        disposeData();
        disposed = true;
    }

    private void disposeData() {
        TIntIntIterator it = shaderPrograms.iterator();
        while (it.hasNext()) {
            it.advance();
            GL20.glDeleteProgram(it.value());
        }
        shaderPrograms.clear();
        parameterLocationMap.clear();
        shader = null;
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
            setInt(UNIFORM, desc, texId);

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
        Arrays.asList(features).forEach(this::deactivateFeature);
    }

    @Override
    public boolean supportsFeature(ShaderProgramFeature feature) {
        return shader.getAvailableFeatures().contains(feature);
    }

    @Override
    public void setFloat(StorageQualifier storageQualifier, String desc, float f, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetFloat(storageQualifier, desc, f);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetFloat(storageQualifier, desc, f);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat1(StorageQualifier storageQualifier, String desc, FloatBuffer buffer, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetFloat1(storageQualifier, desc, buffer);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetFloat1(storageQualifier, desc, buffer);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat2(StorageQualifier storageQualifier, String desc, float f1, float f2, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetFloat2(storageQualifier, desc, f1, f2);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetFloat2(storageQualifier, desc, f1, f2);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat2(StorageQualifier storageQualifier, String desc, FloatBuffer buffer, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetFloat2(storageQualifier, desc, buffer);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetFloat2(storageQualifier, desc, buffer);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat3(StorageQualifier storageQualifier, String desc, float f1, float f2, float f3, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetFloat3(storageQualifier, desc, f1, f2, f3);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetFloat3(storageQualifier, desc, f1, f2, f3);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat3(StorageQualifier storageQualifier, String desc, FloatBuffer buffer, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetFloat3(storageQualifier, desc, buffer);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetFloat3(storageQualifier, desc, buffer);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat4(StorageQualifier storageQualifier, String desc, float f1, float f2, float f3, float f4, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetFloat4(storageQualifier, desc, f1, f2, f3, f4);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetFloat4(storageQualifier, desc, f1, f2, f3, f4);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setFloat4(StorageQualifier storageQualifier, String desc, FloatBuffer buffer, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetFloat4(storageQualifier, desc, buffer);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetFloat4(storageQualifier, desc, buffer);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setInt(StorageQualifier storageQualifier, String desc, int i, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetInt(storageQualifier, desc, i);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetInt(storageQualifier, desc, i);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setBoolean(StorageQualifier storageQualifier, String desc, boolean value, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetInt(storageQualifier, desc, value ? 1 : 0);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetInt(storageQualifier, desc, value ? 1 : 0);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setMatrix3(StorageQualifier qualifier, String desc, Matrix3f value, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetMatrix3(qualifier, desc, MatrixUtils.matrixToFloatBuffer(value));
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetMatrix3(qualifier, desc, MatrixUtils.matrixToFloatBuffer(value));
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setMatrix3(StorageQualifier qualifier, String desc, FloatBuffer value, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetMatrix3(qualifier, desc, value);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetMatrix3(qualifier, desc, value);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setMatrix4(StorageQualifier qualifier, String desc, Matrix4f value, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetMatrix4(qualifier, desc, MatrixUtils.matrixToFloatBuffer(value));
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetMatrix4(qualifier, desc, MatrixUtils.matrixToFloatBuffer(value));
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setMatrix4(StorageQualifier qualifier, String desc, FloatBuffer value, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            glSetMatrix4(qualifier, desc, value);
        } else {
            TIntIntIterator it = shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                glSetMatrix4(qualifier, desc, value);
            }

            restoreStateAfterUniformsSet();
        }
    }

    private int getActiveShaderProgramId() {
        return shaderPrograms.get(activeFeaturesMask);
    }

    private int getParameterLocation(StorageQualifier qualifier, int activeShaderProgramId, String desc) {
        ParameterId id = new ParameterId(activeShaderProgramId, desc);

        if (parameterLocationMap.containsKey(id)) {
            return parameterLocationMap.get(id);
        }

        int loc;
        switch (qualifier) {
            case UNIFORM:
                loc = GL20.glGetUniformLocation(activeShaderProgramId, desc);
                parameterLocationMap.put(id, loc);
                break;
            case ATTRIBUTE:
                loc = GL20.glGetAttribLocation(activeShaderProgramId, desc);
                parameterLocationMap.put(id, loc);
                break;
            default:
                throw noSwitchCaseImplemented(qualifier);
        }

        return loc;
    }

    private void restoreStateAfterUniformsSet() {
        if (shaderManager.getActiveMaterial() == this) {
            GL20.glUseProgram(getActiveShaderProgramId());
        } else {
            enable();
        }
    }

    private void glSetFloat(StorageQualifier storageQualifier, String desc, float f) {
        int id;
        switch (storageQualifier) {
            case UNIFORM:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glUniform1f(id, f);
                break;
            case ATTRIBUTE:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glVertexAttrib1f(id, f);
                break;
            default:
                throw noSwitchCaseImplemented(storageQualifier);
        }
    }

    private void glSetFloat1(StorageQualifier storageQualifier, String desc, FloatBuffer f) {
        int id;
        switch (storageQualifier) {
            case UNIFORM:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glUniform1(id, f);
                break;
            case ATTRIBUTE:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glVertexAttribPointer(id, 1, false, 0, f);
                break;
            default:
                throw noSwitchCaseImplemented(storageQualifier);
        }
    }

    private void glSetFloat2(StorageQualifier storageQualifier, String desc, FloatBuffer f) {
        int id;
        switch (storageQualifier) {
            case UNIFORM:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glUniform2(id, f);
                break;
            case ATTRIBUTE:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glVertexAttribPointer(id, 2, false, 0, f);
                break;
            default:
                throw noSwitchCaseImplemented(storageQualifier);
        }
    }

    private void glSetFloat2(StorageQualifier storageQualifier, String desc, float f1, float f2) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(2).put(f1).put(f2);
        buffer.flip();

        glSetFloat2(storageQualifier, desc, buffer);
    }

    private void glSetFloat3(StorageQualifier storageQualifier, String desc, FloatBuffer f) {
        int id;
        switch (storageQualifier) {
            case UNIFORM:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glUniform3(id, f);
                break;
            case ATTRIBUTE:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glVertexAttribPointer(id, 3, false, 0, f);
                break;
            default:
                throw noSwitchCaseImplemented(storageQualifier);
        }
    }

    private void glSetFloat3(StorageQualifier storageQualifier, String desc, float f1, float f2, float f3) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(3).put(f1).put(f2).put(f3);
        buffer.flip();

        glSetFloat3(storageQualifier, desc, buffer);
    }

    private void glSetFloat4(StorageQualifier storageQualifier, String desc, FloatBuffer f) {
        int id;
        switch (storageQualifier) {
            case UNIFORM:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glUniform4(id, f);
                break;
            case ATTRIBUTE:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glVertexAttribPointer(id, 4, false, 0, f);
                break;
            default:
                throw noSwitchCaseImplemented(storageQualifier);
        }
    }

    private void glSetFloat4(StorageQualifier storageQualifier, String desc, float f1, float f2, float f3, float f4) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4).put(f1).put(f2).put(f3).put(f4);
        buffer.flip();

        glSetFloat4(storageQualifier, desc, buffer);
    }

    private void glSetInt(StorageQualifier storageQualifier, String desc, int i) {
        int id;
        switch (storageQualifier) {
            case UNIFORM:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glUniform1i(id, i);
                break;
            case ATTRIBUTE:
                throw new UnsupportedOperationException("Can't have int attributes in GLSL 1.20");
            default:
                throw noSwitchCaseImplemented(storageQualifier);
        }
    }

    //TODO: Add attribute implementation
    private void glSetMatrix3(StorageQualifier storageQualifier, String desc, FloatBuffer value) {
        int id;
        switch (storageQualifier) {
            case UNIFORM:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glUniformMatrix3(id, false, value);
                break;
            case ATTRIBUTE:
                throw new UnsupportedOperationException("Setting matrix 3 attributes is not implemented yet");
            default:
                throw noSwitchCaseImplemented(storageQualifier);
        }
    }

    //TODO: Add attribute implementation
    private void glSetMatrix4(StorageQualifier storageQualifier, String desc, FloatBuffer value) {
        int id;
        switch (storageQualifier) {
            case UNIFORM:
                id = getParameterLocation(storageQualifier, getActiveShaderProgramId(), desc);
                GL20.glUniformMatrix4(id, false, value);
                break;
            case ATTRIBUTE:
                throw new UnsupportedOperationException("Setting matrix 4 attributes is not implemented yet");
            default:
                throw noSwitchCaseImplemented(storageQualifier);
        }
    }

    private static UnsupportedOperationException noSwitchCaseImplemented(StorageQualifier qualifier) {
        return new UnsupportedOperationException(
                "Switch statement does not have a case in for " + qualifier
        );
    }

    private static final class ParameterId {
        private int shaderProgramId;
        private String name;

        public ParameterId(int shaderProgramId, String name) {
            this.shaderProgramId = shaderProgramId;
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof ParameterId) {
                ParameterId other = (ParameterId) obj;
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
