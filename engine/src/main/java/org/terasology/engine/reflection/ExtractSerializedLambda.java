// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.reflection;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;

@FunctionalInterface
public interface ExtractSerializedLambda {
    SerializedLambda of(Serializable lambda);
}
