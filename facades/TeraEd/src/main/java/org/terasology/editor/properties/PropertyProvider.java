// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.editor.properties;

import java.util.List;

public interface PropertyProvider<T> {
    /**
     * Adds the properties of this Object to the given property list.
     *
     * @return a list of the properties of this object
     */
    List<Property<T>> getProperties();
}
