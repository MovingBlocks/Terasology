// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.autoCreate;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.context.annotation.API;

/**
 * This component is used to mark prefabs that should automatically created when a game begins or is loaded, if one does not already exist.
 *
 * Additional instances of the prefab may be created manually.
 *
 */
@API
public class AutoCreateComponent implements Component<AutoCreateComponent> {
    public boolean createClientSide;

    @Override
    public void copyFrom(AutoCreateComponent other) {
        this.createClientSide = other.createClientSide;
    }
}
