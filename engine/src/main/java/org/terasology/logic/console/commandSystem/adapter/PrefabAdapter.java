// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.console.commandSystem.adapter;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.registry.In;

/**
 *
 */
public class PrefabAdapter implements ParameterAdapter<Prefab> {
    @In
    private AssetManager assetManager;

    @Override
    public Prefab parse(String raw) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(raw));
        return assetManager.getAsset(raw, Prefab.class).orElse(null);
    }

    @Override
    public String convertToString(Prefab value) {
        return value.getUrn().toString();
    }
}
