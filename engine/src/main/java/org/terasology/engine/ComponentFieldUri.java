/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine;

import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;

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
