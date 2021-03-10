// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import com.google.common.base.Objects;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.ShaderManager;
import org.terasology.engine.rendering.dag.StateChange;

import static org.terasology.engine.rendering.dag.AbstractNode.getMaterial;

/**
 * TODO: Add javadocs
 */
public final class EnableMaterial implements StateChange {
    private static StateChange defaultInstance = new DisableMaterial();

    private ResourceUrn materialUrn;
    private Material material;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new EnableMaterial("engine:prog.chunk"));
     */
    public EnableMaterial(ResourceUrn materialUrn) {
        this.materialUrn = materialUrn;
        this.material = getMaterial(materialUrn);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(materialUrn);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof EnableMaterial) && materialUrn.equals(((EnableMaterial) obj).materialUrn);
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), materialUrn.toString());
    }

    @Override
    public void process() {
        material.enable();
    }

    private static final class DisableMaterial implements StateChange {
        // TODO: Switch from CoreRegistry to Context
        private ShaderManager shaderManager = CoreRegistry.get(ShaderManager.class);

        @Override
        public StateChange getDefaultInstance() {
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof DisableMaterial);
        }

        @Override
        public int hashCode() {
            return DisableMaterial.class.hashCode();
        }

        @Override
        public String toString() {
            return String.format("%30s", this.getClass().getSimpleName());
        }

        @Override
        public void process() {
            shaderManager.disableShader();
        }
    }
}
