// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.subsystem.lwjgl;

public interface LwjglGraphicsProcessing {
    void asynchToDisplayThread(Runnable action);
}
