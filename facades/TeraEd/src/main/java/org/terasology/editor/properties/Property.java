// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.editor.properties;

public interface Property<T> {

    T getValue();

    void setValue(T value);

    Class<T> getValueType();

    String getTitle();
}
