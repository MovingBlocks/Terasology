/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.propagation;

/**
 */
public enum PropagationComparison {
    /**
     * Lighting is restricted in some way it wasn't before
     */
    MORE_RESTRICTED(true, false),
    /**
     * Lighting is identical to before
     */
    IDENTICAL(false, false),
    /**
     * Lighting is strictly more permissive than before
     */
    MORE_PERMISSIVE(false, true);

    private boolean restricting;
    private boolean permitting;

    private PropagationComparison(boolean restricts, boolean permits) {
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
