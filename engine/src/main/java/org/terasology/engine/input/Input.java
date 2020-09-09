// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

import org.terasology.nui.input.InputType;

/**
 * The description of an input, whether key, mouse button or mouse wheel. Immutable.
 */
public interface Input {

    /**
     * Returns the type of input, whether key, mouse button, mouse wheel, or controller.
     *
     * @return The input type.
     */
    InputType getType();

    /**
     * Returns the id of the button, key, or other input represented.
     *
     * @return The input id.
     */
    int getId();

    /**
     * Returns the identifying name of the button, key, or other input represented.
     *
     * @return The input name.
     */
    String getName();

    /**
     * Returns the display name of the button, key or other input represented.
     *
     * @return The display name.
     */
    String getDisplayName();
}
