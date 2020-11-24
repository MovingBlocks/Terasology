// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.serializers.gson;

import com.google.gson.JsonElement;

/**
 */
public class GsonPersistedData extends AbstractGsonPersistedData {

    private final JsonElement element;

    public GsonPersistedData(JsonElement element) {
        this.element = element;
    }

    @Override
    public JsonElement getElement() {
        return element;
    }

    @Override
    public GsonPersistedDataArray getAsArray() {
        return new GsonPersistedDataArray(element.getAsJsonArray());
    }

}
