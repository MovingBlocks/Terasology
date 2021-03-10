// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.subscribables;

/**
 * Originally created to provide Observable-like functionality to config classes,
 * it can be used by any class that wishes to be monitored for both general and specific
 * changes to its properties.
 */
public interface Subscribable extends GeneralSubscribable, SpecificSubscribable {

}
