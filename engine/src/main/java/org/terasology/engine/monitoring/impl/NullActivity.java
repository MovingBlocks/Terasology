// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.impl;

import org.terasology.engine.monitoring.Activity;

/**
 * An activity that does nothing when it is closed.
 */
public class NullActivity implements Activity {
    @Override
    public void close() {
    }
}
