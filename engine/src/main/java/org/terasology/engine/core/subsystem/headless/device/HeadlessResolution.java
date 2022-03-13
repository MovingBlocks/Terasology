// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.device;

import org.terasology.engine.core.subsystem.Resolution;

public final class HeadlessResolution implements Resolution {

    private static final HeadlessResolution INSTANCE = new HeadlessResolution();

    private HeadlessResolution() {
    }

    public static HeadlessResolution getInstance() {
        return INSTANCE;
    }
}
