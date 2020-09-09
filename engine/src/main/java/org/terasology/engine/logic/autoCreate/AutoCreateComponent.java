// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.autoCreate;

import org.terasology.engine.entitySystem.Component;
import org.terasology.gestalt.module.sandbox.API;

/**
 * This component is used to mark prefabs that should automatically created when a game begins or is loaded, if one does
 * not already exist.
 * <p>
 * Additional instances of the prefab may be created manually.
 */
@API
public class AutoCreateComponent implements Component {
    public boolean createClientSide;
}
