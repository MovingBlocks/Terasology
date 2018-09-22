/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.DeserializationException;
import org.terasology.persistence.typeHandling.PersistedData;

/**
 */
public class GsonDeserializationContext implements DeserializationContext {

    private JsonDeserializationContext context;

    public GsonDeserializationContext(JsonDeserializationContext context) {
        this.context = context;
    }

    @Override
    public <T> T deserializeAs(PersistedData data, Class<T> type) {
        GsonPersistedData gsonData = (GsonPersistedData) data;
        try {
            return context.deserialize(gsonData.getElement(), type);
        } catch (JsonParseException jpe) {
            throw new DeserializationException("Failed to deserialize data as " + type, jpe);
        }
    }

}
