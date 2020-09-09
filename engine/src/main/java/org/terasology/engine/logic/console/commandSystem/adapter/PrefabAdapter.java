// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem.adapter;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.utilities.Assets;

/**
 *
 */
public class PrefabAdapter implements ParameterAdapter<Prefab> {
    @Override
    public Prefab parse(String raw) {
        return Assets.get(raw, Prefab.class).orElse(null);
    }

    @Override
    public String convertToString(Prefab value) {
        return value.getUrn().toString();
    }
}
