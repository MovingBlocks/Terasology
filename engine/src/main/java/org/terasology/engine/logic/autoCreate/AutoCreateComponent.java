// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.autoCreate;

import org.terasology.module.sandbox.API;
import org.terasology.engine.entitySystem.Component;

/**
 * This component is used to mark prefabs that should automatically created when a game begins or is loaded, if one does not already exist.
 *
 * Additional instances of the prefab may be created manually.
 *
 */
@API
public class AutoCreateComponent implements Component {
    public boolean createClientSide;
}
