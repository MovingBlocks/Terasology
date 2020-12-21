// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.subsystem;

public interface DisplayDeviceInfo {
    String getOpenGlVendor();

    String getOpenGLVersion();

    String getOpenGLRenderer();
}
