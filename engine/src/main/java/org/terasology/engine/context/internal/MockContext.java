// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.context.internal;

import org.terasology.engine.context.Context;

public class MockContext implements Context {
    @Override
    public <T> T get(Class<T> type) {
        return null;
    }

    @Override
    public <T, U extends T> void put(Class<T> type, U object)  {
    }
}
