// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.opengl;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.core.subsystem.lwjgl.LwjglGraphicsProcessing;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.ShaderManager;
import org.terasology.engine.rendering.assets.material.BaseMaterial;
import org.terasology.engine.rendering.assets.material.MaterialData;
import org.terasology.engine.rendering.assets.shader.ShaderParameterMetadata;
import org.terasology.engine.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class GLSLMaterial extends BaseMaterial {

    private static final Logger logger = LoggerFactory.getLogger(GLSLMaterial.class);
    private final LwjglGraphicsProcessing graphicsProcessing;

    private int textureIndex;

    private TObjectIntMap<String> bindMap = new TObjectIntHashMap<>();
    private TIntObjectMap<Texture> textureMap = new TIntObjectHashMap<>();

    private GLSLShader shader;
    private boolean activeFeaturesChanged;
    private TObjectIntMap<UniformId> uniformLocationMap = new TObjectIntHashMap<>();

    private EnumSet<ShaderProgramFeature> activeFeatures = Sets.newEnumSet(Collections.emptyList(), ShaderProgramFeature.class);
    private int activeFeaturesMask;

    private ShaderManager shaderManager;

    private DisposalAction disposalAction;
    private MaterialData materialData;

    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public GLSLMaterial(ResourceUrn urn, AssetType<?, MaterialData> assetType, MaterialData data,
                        LwjglGraphicsProcessing graphicsProcessing, GLSLMaterial.DisposalAction disposalAction) {
        super(urn, assetType, disposalAction);
        this.graphicsProcessing = graphicsProcessing;
        this.disposalAction = disposalAction;
        this.materialData = data;
        shaderManager = CoreRegistry.get(ShaderManager.class);
        graphicsProcessing.asynchToDisplayThread(() -> {
            reload(data);
        });
    }

    public static GLSLMaterial create(ResourceUrn urn, LwjglGraphicsProcessing graphicsProcessing,
                                      AssetType<?, MaterialData> assetType, MaterialData data) {
        return new GLSLMaterial(urn, assetType, data, graphicsProcessing, new DisposalAction(urn, graphicsProcessing));
    }


    @Override
    public void enable() {
        if (shaderManager.getActiveMaterial() != this || activeFeaturesChanged) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL20.glUseProgram(getActiveShaderProgramId());

            // Make sure the shader manager knows that this program is currently active
            shaderManager.setActiveMaterial(this);
            activeFeaturesChanged = false;
        }
    }

    @Override
    public void bindTextures() {
        if (isDisposed()) {
            return;
        }

        enable();
        for (int slot : textureMap.keys()) {
            Texture texture = textureMap.get(slot);
            if (texture.isDisposed()) {
                textureMap.remove(slot);
                logger.error("Attempted to bind disposed texture {}", texture);
            } else {
                shaderManager.bindTexture(slot, texture);
            }
        }
    }

    @Override
    public boolean isRenderable() {
        for (Texture texture : textureMap.valueCollection()) {
            if (!texture.isLoaded()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void recompile() {
        TIntIntIterator it = disposalAction.shaderPrograms.iterator();
        while (it.hasNext()) {
            it.advance();
            GL20.glDeleteProgram(it.value());
        }
        disposalAction.shaderPrograms.clear();
        uniformLocationMap.clear();
        bindMap.clear();

        disposalAction.shaderPrograms.put(0, shader.linkShaderProgram(0));
        for (Set<ShaderProgramFeature> permutation : Sets.powerSet(shader.getAvailableFeatures())) {
            int featureMask = ShaderProgramFeature.getBitset(permutation);
            disposalAction.shaderPrograms.put(featureMask, shader.linkShaderProgram(featureMask));
        }

        //resolves #966
        //Some of the uniforms are not updated constantly between frames
        //this function will rebind any uniforms that are not bound
        rebindVariables(materialData);
    }

    @Override
    public final void doReload(MaterialData data) {
        try {
            GameThread.synch(() -> {
                disposalAction.close();
                uniformLocationMap.clear();

                shader = (GLSLShader) data.getShader();
                recompile();
                rebindVariables(data);
            });
        } catch (InterruptedException e) {
            logger.error("Failed to reload {}", getUrn(), e); //NOPMD
        }
    }

    private void rebindVariables(MaterialData data) {
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
            float[] value = entry.getValue();
            switch (value.length) {
                case 1:
                    setFloat(entry.getKey(), value[0]);
                    break;
                case 2:
                    setFloat2(entry.getKey(), value[0], value[1]);
                    break;
                case 3:
                    setFloat3(entry.getKey(), value[0], value[1], value[2]);
                    break;
                case 4:
                    setFloat4(entry.getKey(), value[0], value[1], value[2], value[3]);
                    break;
                default:
                    logger.error("MaterialData contains float array entry of size > 4");
                    break;
            }
        }
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
            logger.error("Attempt to activate unsupported feature {} for material {} using shader {}", feature, getUrn(), shader.getUrn()); //NOPMD
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
    public void setFloat(String desc, float f, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniform1f(id, f);
        } else {
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
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
            GL20.glUniform1fv(id, buffer);
        } else {
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform1fv(id, buffer);
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
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
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
            GL20.glUniform2fv(id, buffer);
        } else {
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform2fv(id, buffer);
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
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
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
            GL20.glUniform3fv(id, buffer);
        } else {
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform3fv(id, buffer);
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
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
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
            GL20.glUniform4fv(id, buffer);
        } else {
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniform4fv(id, buffer);
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
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
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
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
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
    public void setMatrix3(String desc, Matrix3fc value, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        matrixBuffer.rewind();
        value.get(matrixBuffer);
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniformMatrix3fv(id, false, matrixBuffer);
        } else {
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniformMatrix3fv(id, false, matrixBuffer);
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
            GL20.glUniformMatrix3fv(id, false, value);
        } else {
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniformMatrix3fv(id, false, value);
            }

            restoreStateAfterUniformsSet();
        }
    }

    @Override
    public void setMatrix4(String desc, Matrix4fc value, boolean currentOnly) {
        if (isDisposed()) {
            return;
        }
        matrixBuffer.rewind();
        value.get(matrixBuffer);
        if (currentOnly) {
            enable();
            int id = getUniformLocation(getActiveShaderProgramId(), desc);
            GL20.glUniformMatrix4fv(id, false, matrixBuffer);
        } else {
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniformMatrix4fv(id, false, matrixBuffer);
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
            GL20.glUniformMatrix4fv(id, false, value);
        } else {
            TIntIntIterator it = disposalAction.shaderPrograms.iterator();
            while (it.hasNext()) {
                it.advance();

                GL20.glUseProgram(it.value());
                int id = getUniformLocation(it.value(), desc);
                GL20.glUniformMatrix4fv(id, false, value);
            }

            restoreStateAfterUniformsSet();
        }
    }

    private int getActiveShaderProgramId() {
        return disposalAction.shaderPrograms.get(activeFeaturesMask);
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
        private int hashCode;

        // made package-private after Jenkins' suggestion
        UniformId(int shaderProgramId, String name) {
            this.shaderProgramId = shaderProgramId;
            this.name = name;
            this.hashCode = Objects.hashCode(shaderProgramId, name);
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
            return this.hashCode;
        }
    }

    private static class DisposalAction implements DisposableResource {

        private final ResourceUrn urn;
        private final LwjglGraphicsProcessing graphicsProcessing;

        private TIntIntMap shaderPrograms = new TIntIntHashMap();

        // made package-private after Jenkins' suggestion
        DisposalAction(ResourceUrn urn, LwjglGraphicsProcessing graphicsProcessing) {
            this.urn = urn;
            this.graphicsProcessing = graphicsProcessing;
        }


        @Override
        public void close() {
            try {
                GameThread.synch(() -> {
                    logger.debug("Disposing material {}.", urn);
                    final TIntIntMap deletedPrograms = new TIntIntHashMap(shaderPrograms);
                    graphicsProcessing.asynchToDisplayThread(() -> {
                        TIntIntIterator it = deletedPrograms.iterator();
                        while (it.hasNext()) {
                            it.advance();
                            GL20.glDeleteProgram(it.value());
                        }
                    });
                    shaderPrograms.clear();
                });
            } catch (InterruptedException e) {
                logger.error("Failed to dispose {}", urn, e);
            }
        }
    }
}
