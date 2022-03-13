// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

/**
 * Creates {@link DelayedEntityRef} objects and may save them to actually bind them later.
 */
@FunctionalInterface
public interface DelayedEntityRefFactory {
    DelayedEntityRef createDelayedEntityRef(long id);

}
