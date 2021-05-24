// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.utilities.Assets;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

public class PrefabTypeHandler extends StringRepresentationTypeHandler<Prefab> {

    public PrefabTypeHandler() {
    }

    @Override
    public String getAsString(Prefab item) {
        if (item == null) {
            return "";
        }
        return item.getName();
    }

    @Override
    public Prefab getFromString(String representation) {
        if (representation == null) {
            return null;
        }
        return Assets.getPrefab(representation).orElse(null);
    }
}
