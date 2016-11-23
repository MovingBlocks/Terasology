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
import org.terasology.rendering.dag.tasks.SetInputTextureTask;

import java.util.Objects;

/**
 * TODO
 */
public class SetInputTexture implements StateChange {

    private final int textureSlot;
    private final int textureId;
    private final Material material;
    private final String materialParameter;

    private SetInputTexture defaultInstance;
    private SetInputTextureTask task;

    public SetInputTexture(int textureSlot, int textureId, Material material, String materialParameter) {
        this.textureSlot = textureSlot;
        this.textureId = textureId;
        this.material = material;
        this.materialParameter = materialParameter;
    }

    private SetInputTexture(int textureSlot, Material material, String materialParameter) {
        this.textureSlot = textureSlot;
        this.textureId = 0;
        this.material = material;
        this.materialParameter = materialParameter;

        defaultInstance = this;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetInputTextureTask(textureSlot, textureId, material, materialParameter);
        }
        return task;
    }

    @Override
    public int hashCode() {
        return Objects.hash(textureSlot, textureId, material, materialParameter);
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof SetInputTexture)
                && this.textureSlot == ((SetInputTexture) other).textureSlot
                && this.textureId == ((SetInputTexture) other).textureId
                && this.material == ((SetInputTexture) other).material
                && this.materialParameter.equals(((SetInputTexture) other).materialParameter);
    }

    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new SetInputTexture(textureSlot, material, materialParameter);
        }
        return defaultInstance;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return this == defaultInstance;
    }
}

