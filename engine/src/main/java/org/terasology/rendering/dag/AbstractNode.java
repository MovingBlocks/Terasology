/*
 * Copyright 2017 MovingBlocks
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.SimpleUri;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.utilities.Assets;

import java.util.Map;
import java.util.Set;

/**
 * TODO: Add javadocs
 */
public abstract class AbstractNode implements Node {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractNode.class);

    private SimpleUri nodeUri;

    private Set<StateChange> desiredStateChanges = Sets.newLinkedHashSet();
    private Map<SimpleUri, BaseFBOsManager> fboUsages = Maps.newHashMap();
    private boolean enabled = true;

    protected FBO requiresFBO(FBOConfig fboConfig, BaseFBOsManager fboManager) {
        SimpleUri fboName = fboConfig.getName();

        if (!fboUsages.containsKey(fboName)) {
            fboUsages.put(fboName, fboManager);
        } else {
            logger.warn("FBO " + fboName + " is already requested.");
            return fboManager.get(fboName);
        }

        return fboManager.request(fboConfig);
    }

    @Override
    public void dispose() {
        for (Map.Entry<SimpleUri, BaseFBOsManager> entry : fboUsages.entrySet()) {
            SimpleUri fboName = entry.getKey();
            BaseFBOsManager baseFBOsManager = entry.getValue();
            baseFBOsManager.release(fboName);
        }

        fboUsages.clear();
    }

    protected void addDesiredStateChange(StateChange stateChange) {
        if (stateChange.isTheDefaultInstance()) {
            logger.error("Attempted to add default state change {} to the set of desired state changes. (Node: {})",
                    stateChange.getClass().getSimpleName(), this.toString());
        }
        desiredStateChanges.add(stateChange);
    }

    protected void removeDesiredStateChange(StateChange stateChange) {
        desiredStateChanges.remove(stateChange);
    }

    public Set<StateChange> getDesiredStateChanges() {
        return desiredStateChanges;
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

    @Override
    public void setUri(SimpleUri nodeUri) {
        this.nodeUri = nodeUri;
    }

    @Override
    public SimpleUri getUri() {
        return nodeUri;
    }

    @Override
    public void handleCommand(String command, String... arguments) { }

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
