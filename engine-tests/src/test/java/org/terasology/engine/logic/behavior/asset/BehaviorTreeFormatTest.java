// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.asset;

import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Test;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.module.resources.FileReference;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for https://github.com/MovingBlocks/Terasology/issues/5099. gestalt (MovingBlocks/gestalt#169)
 * isolates unchecked load failures on its own now, so this just needs to throw with the bad node named.
 */
public class BehaviorTreeFormatTest {

    @Test
    public void malformedTreeThrowsNamingTheBadNode() {
        BehaviorTreeFormat format = new BehaviorTreeFormat();
        ResourceUrn urn = new ResourceUrn("engine:malformed");
        List<AssetDataFile> source = Collections.singletonList(new AssetDataFile(jsonFile("{ selector: [success, null, success] }")));

        JsonParseException exception = assertThrows(JsonParseException.class, () -> format.load(urn, source));

        assertTrue(exception.getMessage().contains("selector"));
    }

    private FileReference jsonFile(String json) {
        return new FileReference() {
            @Override
            public String getName() {
                return "malformed.behavior";
            }

            @Override
            public List<String> getPath() {
                return Collections.emptyList();
            }

            @Override
            public InputStream open() {
                return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            }
        };
    }
}
