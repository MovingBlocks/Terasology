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
package org.terasology.rendering.dag;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.utilities.Assets;

/**
 * TODO: Add javadocs
 */
public abstract class AbstractNode implements Node {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractNode.class);

    private Set<StateChange> desiredStateChanges = Sets.newLinkedHashSet();
    private Set<StateChange> desiredStateResets = Sets.newLinkedHashSet();
    private Map<ResourceUrn, BaseFBOsManager> fboUsages = Maps.newHashMap();
    private NodeTask task;
    private boolean enabled = true;

    protected FBO requiresFBO(FBOConfig fboConfig, BaseFBOsManager frameBuffersManager) {
        ResourceUrn fboName = fboConfig.getName();

        if (!fboUsages.containsKey(fboName)) {
            fboUsages.put(fboName, frameBuffersManager);
        } else {
            logger.warn("FBO " + fboName + " is already requested.");
            return frameBuffersManager.get(fboName);
        }

        return frameBuffersManager.request(fboConfig);
    }

    @Override
    public void dispose() {
        for (Map.Entry<ResourceUrn, BaseFBOsManager> entry : fboUsages.entrySet()) {
            ResourceUrn fboName = entry.getKey();
            BaseFBOsManager baseFBOsManager = entry.getValue();
            baseFBOsManager.release(fboName);
        }

        fboUsages.clear();
    }

    protected void addDesiredStateChange(StateChange stateChange) {
        if (stateChange.isTheDefaultInstance()) {
            logger.error("Attempted to add default state change %s to the set of desired state changes", stateChange.getClass().getSimpleName());
        }
        desiredStateChanges.add(stateChange);
        desiredStateResets.add(stateChange.getDefaultInstance());
    }

    protected void removeDesiredStateChange(StateChange stateChange) {
        desiredStateChanges.remove(stateChange);
        desiredStateResets.remove(stateChange.getDefaultInstance());
    }

    public Set<StateChange> getDesiredStateChanges() {
        return desiredStateChanges;
    }
    public Set<StateChange> getDesiredStateResets() {
        return desiredStateResets;
    }

    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new NodeTask(this);
        }
        return task;
    }

    @Override
    public String toString() {
        return String.format("%30s", this.getClass().getSimpleName());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Utility method to conveniently retrieve materials from the Assets system,
     * hiding the relative complexity of the exception handling.
     *
     * @param materialUrn a ResourceUrn instance providing the name of the material to be obtained.
     * @return a Material instance
     * @throws RuntimeException if the material couldn't be resolved through the asset system.
     */
    public static Material getMaterial(ResourceUrn materialUrn) {
        String materialName = materialUrn.toString();
        return Assets.getMaterial(materialName).orElseThrow(() ->
                new RuntimeException("Failed to resolve required asset: '" + materialName + "'"));
    }
}
