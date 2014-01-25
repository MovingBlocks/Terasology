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
package org.terasology.rendering.base;

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.shader.ShaderData;
import org.terasology.rendering.assets.shader.ShaderParameterMetadata;

public abstract class BaseShader extends AbstractAsset<ShaderData> implements Shader {

    private ShaderData shaderProgramBase;

    public BaseShader(AssetUri uri, ShaderData data) {
        super(uri);
        reload(data);
    }

    public abstract void recompile();

    public abstract ShaderParameterMetadata getParameter(String desc);

    public abstract Iterable<ShaderParameterMetadata> listParameters();

    @Override
    public void reload(ShaderData data) {
        shaderProgramBase = data;
    }

    @Override
    public void dispose() {
        shaderProgramBase = null;
    }

    @Override
    public boolean isDisposed() {
        return shaderProgramBase == null;
    }
}
