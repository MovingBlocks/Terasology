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

import com.google.common.base.Objects;
import org.terasology.assets.ResourceUrn;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;

import static org.terasology.rendering.dag.AbstractNode.getMaterial;

/**
 * TODO: Add javadocs
 */
public final class EnableMaterial implements StateChange {
    private static final ResourceUrn DEFAULT_MATERIAL_URN = new ResourceUrn("engine:prog.default");

    private static EnableMaterial defaultInstance = new EnableMaterial(DEFAULT_MATERIAL_URN);

    private RenderPipelineTask task;
    private ResourceUrn materialUrn;

    public EnableMaterial(ResourceUrn materialUrn) {
        this.materialUrn = materialUrn;
    }

    public ResourceUrn getMaterialUrn() {
        return materialUrn;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            if (materialUrn.equals(DEFAULT_MATERIAL_URN)) {
                task = new DisableMaterialTask();
            } else {
                Material shader = getMaterial(materialUrn);
                task = new EnableMaterialTask(shader, materialUrn);
            }
        }
        return task;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(materialUrn);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof EnableMaterial) && materialUrn.equals(((EnableMaterial) obj).getMaterialUrn());
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), materialUrn.toString());
    }

    private class DisableMaterialTask implements RenderPipelineTask {
        private ShaderManager shaderManager = CoreRegistry.get(ShaderManager.class);

        @Override
        public void execute() {
            shaderManager.disableShader();
        }

        @Override
        public String toString() {
            return String.format("%30s: program 0", this.getClass().getSimpleName());
        }
    }

    private class EnableMaterialTask implements RenderPipelineTask {
        private Material material;
        private ResourceUrn materialUrn;

        private EnableMaterialTask(Material material, ResourceUrn materialUrn) {
            this.material = material;
            this.materialUrn = materialUrn;
        }

        @Override
        public void execute() {
            material.enable();
        }

        @Override
        public String toString() {
            return String.format("%30s: %s", this.getClass().getSimpleName(), materialUrn.toString());
        }
    }
}
