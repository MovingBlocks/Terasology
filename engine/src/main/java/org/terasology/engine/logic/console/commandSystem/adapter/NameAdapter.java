// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem.adapter;

import org.terasology.gestalt.naming.Name;

public class NameAdapter implements ParameterAdapter<Name> {
    @Override
    public Name parse(String raw) {
        return new Name(raw);
    }

    @Override
    public String convertToString(Name value) {
        return value.toString();
    }
}
