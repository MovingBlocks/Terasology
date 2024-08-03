// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

/**
 * This package includes a number of StateChange objects that can be used within Nodes.
 *
 * Nodes use StateChange objects to create the OpenGL state they require - starting from Terasology's default OpenGL state.
 * Redundant state changes (identical state changes requested by consecutive nodes) are automatically eliminated.
 * State changes are automatically undone when they are no longer necessary, to restore Terasology's default OpenGL state.
 */
@API
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.context.annotation.API;
