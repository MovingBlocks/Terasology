// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.SimpleUri;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

public class SimpleUriTypeHandler extends StringRepresentationTypeHandler<SimpleUri> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleUriTypeHandler.class);

    @Override
    public String getAsString(SimpleUri uri) {
        if (uri == null) {
            return "";
        }
        return uri.toString();
    }

    @Override
    public SimpleUri getFromString(String representation) {
        SimpleUri uri = new SimpleUri(representation);
        if (!uri.isValid()) {
            logger.error("Failed to create valid SimpleURI from string '{}'", representation);
            // StringRepresentationTypeHandler will turn this 'null' value into an empty Optional
            return null;
        }

        return uri;
    }
}
