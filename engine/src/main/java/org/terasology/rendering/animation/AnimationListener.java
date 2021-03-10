// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.animation;

/*
 * Notified on significant animation events.
 * All methods are empty by default.
 */
public interface AnimationListener {
    default void onStart() { }
    default void onEnd() { }
}
