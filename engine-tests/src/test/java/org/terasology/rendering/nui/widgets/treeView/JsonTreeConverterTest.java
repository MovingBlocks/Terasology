// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.nui.widgets.treeView;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.terasology.nui.widgets.treeView.JsonTreeConverter;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class JsonTreeConverterTest {
    @Test
    public void testTreeAdapter() {
        File file = new File(getClass().getClassLoader().getResource("jsonTreeConverterInput.json").getFile());
        String content = null;
        try {
            content = Files.toString(file, Charsets.UTF_8);
        } catch (IOException e) {
            fail("Could not load input file");
        }
        JsonElement element = new JsonParser().parse(content);
        assertEquals(element, JsonTreeConverter.deserialize(JsonTreeConverter.serialize(element)));
    }
}
