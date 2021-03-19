// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.permission;

import org.terasology.engine.entitySystem.Component;

import java.util.Set;

public class PermissionSetComponent implements Component {
    public Set<String> permissions;
}
