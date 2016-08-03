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
import org.terasology.rendering.dag.tasks.DisableMaterialTask;
import org.terasology.rendering.dag.tasks.EnableMaterialTask;
import org.terasology.utilities.Assets;

/**
 * TODO: Add javadocs
 */
public class EnableMaterial implements StateChange {
    private static final String DEFAULT_MATERIAL_NAME = "DEFAULT";
    private static EnableMaterial defaultInstance = new EnableMaterial(DEFAULT_MATERIAL_NAME);

    private RenderPipelineTask task;
    private String materialName;

    public EnableMaterial(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialName() {
        return materialName;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            if (materialName.equals(DEFAULT_MATERIAL_NAME)) {
                task = new DisableMaterialTask();
            } else {
                Material shader = getMaterial(materialName);
                task = new EnableMaterialTask(shader, materialName);
            }
        }
        return task;
    }

    @Override
    public boolean isEqualTo(StateChange stateChange) {
        if (stateChange instanceof EnableMaterial) {
            return materialName.equals(((EnableMaterial) stateChange).getMaterialName());
        }
        return false;
    }

    @Override
    public boolean isTheDefaultInstance() {
        return this == defaultInstance;
    }

    private static Material getMaterial(String assetId) {
        return Assets.getMaterial(assetId).orElseThrow(() ->
                new RuntimeException("Failed to resolve required asset: '" + assetId + "'"));
    }

    @Override
    public String toString() {
        return String.format("%21s(%s)", this.getClass().getSimpleName(), materialName);
    }
}
