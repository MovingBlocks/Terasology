// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.gson;

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
