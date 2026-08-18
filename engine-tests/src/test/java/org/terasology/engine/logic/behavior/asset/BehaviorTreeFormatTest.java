// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.asset;

import org.junit.jupiter.api.Test;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.module.resources.FileReference;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for https://github.com/MovingBlocks/Terasology/issues/5099. A malformed behavior tree must
 * fail this single asset's load with the checked {@link IOException} the {@code AssetFileFormat} contract expects
 * - that's what lets gestalt's asset-loading machinery isolate the failure to just this asset (log it, move on)
 * instead of an unchecked exception blowing past that safety net and crashing whatever triggered the load.
 */
public class BehaviorTreeFormatTest {

    @Test
    public void malformedTreeFailsWithCheckedIOExceptionNotAnUncheckedOne() {
        BehaviorTreeFormat format = new BehaviorTreeFormat();
        ResourceUrn urn = new ResourceUrn("engine:malformed");
        List<AssetDataFile> source = Collections.singletonList(new AssetDataFile(jsonFile("{ selector: [success, null, success] }")));

        IOException exception = assertThrows(IOException.class, () -> format.load(urn, source));

        assertTrue(exception.getMessage().contains("engine:malformed"));
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
