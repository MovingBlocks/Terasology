// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.event;

/**
 * Additional for {@link ReceiveEvent}.
 * Activate performance monitoring for Event Handler.
 */
public @interface Activity {
    String value();
}
