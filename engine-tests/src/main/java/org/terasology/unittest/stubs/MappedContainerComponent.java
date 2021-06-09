// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

import java.util.Set;

public class MappedContainerComponent implements Component<MappedContainerComponent> {
    public Set<Cont> containers;

    @Override
    public void copy(MappedContainerComponent other) {
        this.containers = other.containers;
    }

    @MappedContainer
    public static class Cont {
        public String value;
    }
}
