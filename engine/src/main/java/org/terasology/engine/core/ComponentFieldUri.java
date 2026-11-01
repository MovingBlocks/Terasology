// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import org.terasology.context.annotation.API;
import org.terasology.gestalt.naming.Name;

import java.util.Objects;

/**
 * A URI to identify a field of a component in terasology.
 * These URIs are always in the form: {@literal <module-name>:<object-name>.<fieldName>}. They are case-insensitive (using
 * English casing), and have a "normalized" form that is lower case.
 *
 */
@API
public final class ComponentFieldUri implements Uri {

    public static final String FIELD_SEPARATOR = ".";
    private final SimpleUri componentUri;
    private final String fieldName;


    public ComponentFieldUri(SimpleUri componentUri, String fieldName) {
        this.componentUri = componentUri;
        this.fieldName = fieldName;
    }

    public ComponentFieldUri(String textVersion) {
        int seperatorIndex = textVersion.indexOf(FIELD_SEPARATOR, 2);

        if (seperatorIndex != -1) {
            componentUri = new SimpleUri(textVersion.substring(0, seperatorIndex));
            fieldName = textVersion.substring(seperatorIndex + 1);
        } else {
            // create invalid uri
            fieldName = null;
            componentUri = new SimpleUri();
        }
    }

    public Name getObjectName() {
        return componentUri.getObjectName();
    }

    @Override
    public boolean isValid() {
        return componentUri.isValid() && fieldName != null;
    }

    @Override
    public Name getModuleName() {
        return componentUri.getModuleName();
    }

    public SimpleUri getComponentUri() {
        return componentUri;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComponentFieldUri that = (ComponentFieldUri) o;

        if (!componentUri.equals(that.componentUri)) {
            return false;
        }
        return Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        int result = componentUri.hashCode();
        result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "";
        }
        return componentUri + FIELD_SEPARATOR + fieldName;
    }
}
