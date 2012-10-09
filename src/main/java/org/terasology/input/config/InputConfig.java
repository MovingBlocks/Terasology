/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.input.config;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import org.lwjgl.input.Keyboard;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.MouseInput;
import org.terasology.utilities.gson.MultimapHandler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * @author Immortius
 */
public class InputConfig {
    private Multimap<String, String> inputMap = HashMultimap.create();

    public InputConfig() {
    }

    public InputConfig(Multimap<String, String> inputMap) {
        this.inputMap.putAll(inputMap);
    }

    public List<Input> getInputs(String id) {
        List<Input> result = Lists.newArrayList();
        for (String inputId : inputMap.get(id)) {
            inputId = inputId.toUpperCase(Locale.ENGLISH);
            if (inputId.startsWith("KEY_")) {
                int keyIndex = Keyboard.getKeyIndex(inputId);
                if (keyIndex != Keyboard.KEY_NONE) {
                    result.add(new Input(InputType.KEY, keyIndex));
                }
            } else if (inputId.startsWith("MOUSE_")) {
                MouseInput input = MouseInput.parse(inputId);
                if (input != MouseInput.MOUSE_NONE) {
                    result.add(input.getInput());
                }
            }
        }
        return result;
    }

    public void setInputs(String id, Input... inputs) {
        inputMap.removeAll(id);
        for (Input input : inputs) {
            inputMap.put(id, input.toString());
        }
    }

    public static void save(File toFile, InputConfig config) throws IOException {
        FileWriter writer = new FileWriter(toFile);
        try {
            new GsonBuilder().registerTypeAdapter(Multimap.class, new MultimapHandler<String>(String.class)).setPrettyPrinting().create().toJson(config.inputMap, writer);
        } finally {
            // JAVA7: better closing support
            writer.close();
        }
    }

    public static InputConfig load(File fromFile) throws IOException {
        FileReader reader = new FileReader(fromFile);
        try {
            return new InputConfig(new GsonBuilder().registerTypeAdapter(Multimap.class, new MultimapHandler<String>(String.class)).create().fromJson(reader, Multimap.class));
        } finally {
            reader.close();
        }
    }
}
