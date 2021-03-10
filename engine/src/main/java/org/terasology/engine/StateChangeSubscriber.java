// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

/**
 * An interface for subscribers to engine state changes
 */
@FunctionalInterface
public interface StateChangeSubscriber {

    void onStateChange();
}
