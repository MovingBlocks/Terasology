// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

public class BlockUriTypeHandler extends StringRepresentationTypeHandler<BlockUri> {

    private static final Logger logger = LoggerFactory.getLogger(BlockUriTypeHandler.class);

    @Override
    public String getAsString(BlockUri uri) {
        if (uri == null) {
            return "";
        }
        return uri.toString();
    }

    @Override
    public BlockUri getFromString(String representation) {
        BlockUri uri = new BlockUri(representation);
        if (!uri.isValid()) {
            logger.error("Failed to create valid BlockUri from string '{}'", representation);
            // StringRepresentationTypeHandler will turn this 'null' value into an empty Optional
            return null;
        }

        return uri;
    }
}
