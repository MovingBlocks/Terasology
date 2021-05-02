// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.terasology.engine.entitySystem.Component;
import org.terasology.reflection.MappedContainer;

import java.util.Set;

public class MappedContainerComponent implements Component {
    public Set<Cont> containers;

    @MappedContainer
    public static class Cont {
        public String value;
    }
}
