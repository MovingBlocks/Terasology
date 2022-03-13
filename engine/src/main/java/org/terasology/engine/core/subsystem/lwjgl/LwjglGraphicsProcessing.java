// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.subsystem.lwjgl;

public interface LwjglGraphicsProcessing {
    void asynchToDisplayThread(Runnable action);
}
