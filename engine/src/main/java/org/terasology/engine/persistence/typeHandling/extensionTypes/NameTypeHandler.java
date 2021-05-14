// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.terasology.gestalt.naming.Name;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

/**
 */
public class NameTypeHandler extends StringRepresentationTypeHandler<Name> {

    @Override
    public String getAsString(Name item) {
        if (item == null) {
            return "";
        }
        return item.toString();
    }

    @Override
    public Name getFromString(String representation) {
        return new Name(representation);
    }
}
