// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.terasology.engine.core.SimpleUri;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

public class SimpleUriTypeHandler extends StringRepresentationTypeHandler<SimpleUri> {

    @Override
    public String getAsString(SimpleUri uri) {
        if (uri == null) {
            return "";
        }
        return uri.toString();
    }

    @Override
    public SimpleUri getFromString(String representation) {
        return new SimpleUri(representation);
    }
}
