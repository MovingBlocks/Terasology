// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.dag;

import com.google.common.base.Objects;
import org.terasology.engine.rendering.dag.StateChange;

public class SetName implements StateChange {
    private static SetName defaultInstance = new SetName("bar");

    private String name;

    SetName(String name) {
        this.name = name;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetName) && name.equals(((SetName) obj).getName());
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), name);
    }

    public String getName() {
        return name;
    }

    @Override
    public void process() { }
}
