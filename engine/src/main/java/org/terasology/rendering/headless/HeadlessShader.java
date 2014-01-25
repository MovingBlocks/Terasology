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

import java.util.Iterator;

import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.shader.ShaderData;
import org.terasology.rendering.assets.shader.ShaderParameterMetadata;
import org.terasology.rendering.base.BaseShader;

public class HeadlessShader extends BaseShader implements Shader {

    public HeadlessShader(AssetUri uri, ShaderData data) {
        super(uri, data);
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
        return new EmptyIterable();
    }

    private class EmptyIterable implements Iterable<ShaderParameterMetadata> {
        @Override
        public Iterator<ShaderParameterMetadata> iterator() {
            return new Iterator<ShaderParameterMetadata>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public ShaderParameterMetadata next() {
                    return null;
                }

                @Override
                public void remove() {
                    // do nothing
                }
            };
        }
    };
}
