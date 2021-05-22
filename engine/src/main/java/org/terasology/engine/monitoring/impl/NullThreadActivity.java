// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.impl;

import org.terasology.engine.monitoring.ThreadActivity;

/**
 */
public class NullThreadActivity implements ThreadActivity {
    @Override
    public void close() {
    }
}
