// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.suggesters;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.console.commandSystem.CommandParameterSuggester;

import java.util.Map;
import java.util.Set;

/**
 * Suggest asset names. When only one asset resource exists for the entire set of loaded modules,
 * the name alone suffices. Otherwise, the module name must be prepended.
 */
public abstract class AssetSuggester implements CommandParameterSuggester<String> {
    private final Class<? extends Asset<?>> assetType;
    private final AssetManager assetManager;

    public AssetSuggester(Class<? extends Asset<?>> assetType, AssetManager assetManager) {
        this.assetType = assetType;
        this.assetManager = assetManager;
    }

    @Override
    public Set<String> suggest(EntityRef sender, Object... resolvedParameters) {
        Map<String, Set<ResourceUrn>> resourceMap = Maps.newHashMap();
        Set<String> suggestions = Sets.newHashSet();

        for (ResourceUrn resolvedParameter : assetManager.getAvailableAssets(assetType)) {
            String resourceName = resolvedParameter.getResourceName().toString();
            if (!resourceMap.containsKey(resourceName)) {
                resourceMap.put(resourceName, Sets.newHashSet());
            }

            resourceMap.get(resourceName).add(resolvedParameter);
        }

        for (String key : resourceMap.keySet()) {
            Set<ResourceUrn> set = resourceMap.get(key);
            if (set.size() == 1) {
                suggestions.add(set.iterator().next().getResourceName().toString());
            } else {
                for (ResourceUrn resourceUrn : set) {
                    suggestions.add(resourceUrn.toString());
                }
            }
        }

        return suggestions;
    }
}
