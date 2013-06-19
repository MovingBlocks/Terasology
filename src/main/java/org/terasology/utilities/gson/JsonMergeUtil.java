package org.terasology.utilities.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * @author Immortius
 */
public final class JsonMergeUtil {
    private JsonMergeUtil() {
    }

    public static void mergeOnto(JsonObject from, JsonObject to) {
        for (Map.Entry<String, JsonElement> entry : from.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                if (!to.has(entry.getKey())) {
                    to.add(entry.getKey(), entry.getValue());
                }
            } else {
                if (!to.has(entry.getKey())) {
                    to.add(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
