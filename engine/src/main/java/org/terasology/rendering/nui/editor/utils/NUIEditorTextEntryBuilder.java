/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.editor.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import org.terasology.rendering.nui.widgets.UITextEntry;
import org.terasology.rendering.nui.widgets.treeView.JsonTree;
import org.terasology.rendering.nui.widgets.treeView.JsonTreeValue;

import java.util.Map;

/**
 * A utility class to create {@link UITextEntry} instances used as inline editors
 * in the NUI editor.
 */
public final class NUIEditorTextEntryBuilder {

    private NUIEditorTextEntryBuilder() {
    }

    /**
     * @param formatter A text entry formatter.
     * @param parser    A text entry parser.
     * @return A {@link UITextEntry} with the given formatter and parser.
     */
    private static UITextEntry<JsonTree> createEditorEntry(UITextEntry.Formatter<JsonTree> formatter,
                                                           UITextEntry.Parser<JsonTree> parser) {
        UITextEntry<JsonTree> editorEntry = new UITextEntry<>();
        editorEntry.setFormatter(formatter);
        editorEntry.setParser(parser);
        editorEntry.subscribe(widget -> editorEntry.onLoseFocus());
        return editorEntry;
    }

    /**
     * @return A {@link UITextEntry} to be used to edit a JSON value node.
     */
    public static UITextEntry<JsonTree> createValueEditor() {
        UITextEntry.Formatter<JsonTree> formatter = value -> value.getValue().toString();

        UITextEntry.Parser<JsonTree> parser = value -> {
            try {
                Double valueDouble = Double.parseDouble(value);
                return new JsonTree(new JsonTreeValue(null, valueDouble, JsonTreeValue.Type.VALUE));
            } catch (NumberFormatException e) {
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    return new JsonTree(new JsonTreeValue(null, Boolean.parseBoolean(value), JsonTreeValue.Type.VALUE));
                } else {
                    return new JsonTree(new JsonTreeValue(null, value, JsonTreeValue.Type.VALUE));
                }
            }
        };

        return createEditorEntry(formatter, parser);
    }

    /**
     * @return A {@link UITextEntry} to be used to edit a JSON key/value node.
     */
    public static UITextEntry<JsonTree> createKeyValueEditor() {
        UITextEntry.Formatter<JsonTree> formatter = value -> {
            JsonObject jsonObject = new JsonObject();

            String jsonKey = value.getValue().getKey();
            Object jsonValue = value.getValue().getValue();

            if (jsonValue instanceof Boolean) {
                jsonObject.addProperty(jsonKey, (Boolean) jsonValue);
            } else if (jsonValue instanceof Number) {
                jsonObject.addProperty(jsonKey, (Number) jsonValue);
            } else if (jsonValue instanceof String) {
                jsonObject.addProperty(jsonKey, (String) jsonValue);
            } else {
                jsonObject.addProperty(jsonKey, (Character) jsonValue);
            }

            String jsonString = new Gson().toJson(jsonObject);
            return jsonString.substring(1, jsonString.length() - 1);
        };

        UITextEntry.Parser<JsonTree> parser = value -> {
            String jsonString = String.format("{%s}", value);
            try {
                JsonElement jsonElement = new JsonParser().parse(jsonString);
                Map.Entry keyValuePair = jsonElement.getAsJsonObject().entrySet().iterator().next();

                String jsonKey = (String) keyValuePair.getKey();
                JsonTreeValue parsedNode;
                if (keyValuePair.getValue() == null) {
                    parsedNode = new JsonTreeValue(jsonKey, null, JsonTreeValue.Type.KEY_VALUE_PAIR);
                } else {
                    JsonPrimitive jsonValue = (JsonPrimitive) keyValuePair.getValue();
                    if (jsonValue.isBoolean()) {
                        parsedNode = new JsonTreeValue(jsonKey, jsonValue.getAsBoolean(), JsonTreeValue.Type.KEY_VALUE_PAIR);
                    } else if (jsonValue.isNumber()) {
                        parsedNode = new JsonTreeValue(jsonKey, jsonValue.getAsNumber(), JsonTreeValue.Type.KEY_VALUE_PAIR);
                    } else {
                        parsedNode = new JsonTreeValue(jsonKey, jsonValue.getAsString(), JsonTreeValue.Type.KEY_VALUE_PAIR);
                    }
                }
                return new JsonTree(parsedNode);
            } catch (JsonSyntaxException e) {
                return null;
            }
        };

        return createEditorEntry(formatter, parser);
    }

    /**
     * @return A {@link UITextEntry} to be used to edit a JSON array node.
     */
    public static UITextEntry<JsonTree> createArrayEditor() {
        UITextEntry.Formatter<JsonTree> formatter = value -> value.getValue().getKey();

        UITextEntry.Parser<JsonTree> parser = value -> new JsonTree(new JsonTreeValue(value, null, JsonTreeValue.Type.ARRAY));

        return createEditorEntry(formatter, parser);
    }

    /**
     * @return A {@link UITextEntry} to be used to edit a JSON object node.
     */
    public static UITextEntry<JsonTree> createObjectEditor() {
        UITextEntry.Formatter<JsonTree> formatter = value -> value.getValue().getKey();

        UITextEntry.Parser<JsonTree> parser = value -> new JsonTree(new JsonTreeValue(value, null, JsonTreeValue.Type.OBJECT));

        return createEditorEntry(formatter, parser);
    }
}
