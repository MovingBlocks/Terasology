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
package org.terasology.rendering.headless;

import java.util.Collections;

import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.shader.ShaderData;
import org.terasology.rendering.assets.shader.ShaderParameterMetadata;

public class HeadlessShader extends AbstractAsset<ShaderData> implements Shader {

    private ShaderData shaderProgramBase;

    public HeadlessShader(AssetUri uri, ShaderData data) {
        super(uri);
        reload(data);
    }

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

    @Override
    public void recompile() {
        // do nothing
    }

    @Override
    public ShaderParameterMetadata getParameter(String desc) {
        return null;
    }

    @Override
    public Iterable<ShaderParameterMetadata> listParameters() {
        return Collections.emptyList();
    }
}
