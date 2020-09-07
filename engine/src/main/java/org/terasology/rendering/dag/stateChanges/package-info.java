/*
 * Copyright 2018 MovingBlocks
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

/**
 * This package includes a number of StateChange objects that can be used within Nodes.
 *
 * Nodes use StateChange objects to create the OpenGL state they require - starting from Terasology's default OpenGL state.
 * Redundant state changes (identical state changes requested by consecutive nodes) are automatically eliminated.
 * State changes are automatically undone when they are no longer necessary, to restore Terasology's default OpenGL state.
 */
@API
package org.terasology.rendering.dag.stateChanges;

import org.terasology.gestalt.module.sandbox.API;
