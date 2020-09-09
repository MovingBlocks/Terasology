// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.stubs;

import org.terasology.engine.entitySystem.Component;

/**
 *
 */
public final class StringComponent implements Component {
    public static String staticValue = "Test";
    public String value;

    public StringComponent() {
    }

    public StringComponent(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StringComponent that = (StringComponent) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
