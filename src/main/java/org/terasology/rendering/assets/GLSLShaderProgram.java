/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.assets;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.metadata.ParamMetadata;
import org.terasology.rendering.assets.metadata.ParamType;
import org.terasology.rendering.assets.metadata.ShaderMetadata;
import org.terasology.rendering.shader.IShaderParameters;

/**
 * GLSL Shader Program class.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GLSLShaderProgram implements Asset {
    private static final Logger logger = LoggerFactory.getLogger(GLSLShaderProgram.class);

    private String title;
    private String vertShader;
    private String fragShader;

    private AssetUri uri;
    private boolean disposed = false;

    private Map<String, ParamType> materialParameters = Maps.newHashMap();
    private HashSet<GLSLShaderProgramInstance> shaderProgramInstances = new HashSet<GLSLShaderProgramInstance>();

    public GLSLShaderProgram(AssetUri uri, String vertShader, String fragShader, ShaderMetadata metadata) {
        this.uri = uri;
        this.vertShader = vertShader;
        this.fragShader = fragShader;
        this.title = uri.toString();

        logger.info("Loading shader program '"+uri+"' as asset...");

        for (ParamMetadata paramData : metadata.getParameters()) {
            materialParameters.put(paramData.getName(), paramData.getType());
        }
    }

    @Override
    public AssetUri getURI() {
        return uri;
    }

    @Override
    public void dispose() {
        for (GLSLShaderProgramInstance instance : shaderProgramInstances) {
            instance.dispose();
        }

        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public ParamMetadata getMaterialParameter(String desc) {
        if (materialParameters.containsKey(desc)) {
            return new ParamMetadata(desc, materialParameters.get(desc));
        }
        return null;
    }

    public GLSLShaderProgramInstance createShaderProgramInstance(IShaderParameters shaderParameters) {
        GLSLShaderProgramInstance instance = new GLSLShaderProgramInstance(this, shaderParameters);

        return instance;
    }

    public GLSLShaderProgramInstance createShaderProgramInstance() {
        return createShaderProgramInstance(null);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVertShader() {
        return vertShader;
    }

    public void setVertShader(String vertShader) {
        this.vertShader = vertShader;
    }

    public String getFragShader() {
        return fragShader;
    }

    public void setFragShader(String fragShader) {
        this.fragShader = fragShader;
    }

    public void registerShaderProgramInstance(GLSLShaderProgramInstance instance) {
        shaderProgramInstances.add(instance);
    }

    public void unregisterShaderProgramInstance(GLSLShaderProgramInstance instance) {
        shaderProgramInstances.remove(instance);
    }
}
