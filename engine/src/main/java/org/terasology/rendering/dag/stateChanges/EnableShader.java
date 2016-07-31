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
package org.terasology.rendering.dag.stateChanges;

import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.tasks.EnableShaderTask;
import org.terasology.utilities.Assets;

/**
 * TODO: Add javadocs
 */
public class EnableShader implements StateChange {
    private EnableShaderTask task;
    private String shaderName;

    public EnableShader(String shaderName) {
        this.shaderName = shaderName;
    }

    public String getShaderName() {
        return shaderName;
    }

    @Override
    public StateChange getDefaultInstance() {
        return null;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            Material shader = getMaterial(shaderName);
            task = new EnableShaderTask(shader, shaderName);
        }
        return task;
    }

    @Override
    public boolean isEqualTo(StateChange stateChange) {
        if (stateChange instanceof EnableShader) {
            return shaderName.equals(((EnableShader) stateChange).getShaderName());
        }
        return false;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return false;
    }

    private static Material getMaterial(String assetId) {
        return Assets.getMaterial(assetId).orElseThrow(() ->
                new RuntimeException("Failed to resolve required asset: '" + assetId + "'"));
    }

    @Override
    public String toString() {
        return String.format("%21s(%s)", this.getClass().getSimpleName(), shaderName);
    }
}
