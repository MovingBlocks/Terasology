// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

public interface ModuleExtension {

    String getKey();

    Class<?> getValueType();
}

