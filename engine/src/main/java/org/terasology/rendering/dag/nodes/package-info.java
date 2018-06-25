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
 * This package contains the Renderer's nodes.
 *
 * Nodes are added and removed to/from the RenderGraph. Each node sets its own OpenGL state via StateChange
 * objects and usually, but not necessarily, draws something in one of a number of frame buffer object.
 * The content of the FBOs is then used by other nodes and so on until the last node draws directly on the display.
 *
 * The nodes in this package are those provided by default by the engine, and include highly specific nodes (i.e.
 * ApplyDeferredLightingNode) and reusable nodes such as BlurNode.
 *
 * Eventually some or all of these nodes, together or in groups, might migrate to one or more external modules.
 */
@API
package org.terasology.rendering.dag.nodes;

import org.terasology.module.sandbox.API;
