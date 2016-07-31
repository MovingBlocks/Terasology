/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.dag.tasks;

import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.RenderPipelineTask;

/**
 * TODO: Add javadocs
 */
public class EnableShaderTask implements RenderPipelineTask {
    private Material shader;
    private String shaderName;

    public EnableShaderTask(Material shader, String shaderName) {
        this.shader = shader;
        this.shaderName = shaderName;
    }

    @Override
    public void execute() {
        shader.enable();
    }

    @Override
    public String toString() {
        return String.format("%21s(%s)", this.getClass().getSimpleName(), shaderName);
    }
}
