// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.propagation;

/**
 * An enum that describes how propagation rules have changes when blocks are replaced with others
 */
public enum PropagationComparison {
    /**
     * Propagation is restricted in some way it wasn't before
     */
    MORE_RESTRICTED(true, false),
    /**
     * Propagation is identical to before
     */
    IDENTICAL(false, false),
    /**
     * Propagation is strictly more permissive than before
     */
    MORE_PERMISSIVE(false, true);

    private boolean restricting;
    private boolean permitting;

     PropagationComparison(boolean restricts, boolean permits) {
        this.restricting = restricts;
        this.permitting = permits;
    }

    public boolean isRestricting() {
        return restricting;
    }

    public boolean isPermitting() {
        return permitting;
    }
}
