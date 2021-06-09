// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.permission;

import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Set;

public class PermissionSetComponent implements Component<PermissionSetComponent> {
    public Set<String> permissions;

    @Override
    public void copy(PermissionSetComponent other) {
        this.permissions = other.permissions;
    }
}
