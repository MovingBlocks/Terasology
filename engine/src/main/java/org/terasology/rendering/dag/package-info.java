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
 * A Directed Acyclic Graph (DAG) is the type of data structure that is at the core of the Renderer.
 *
 * More specifically, the Renderer uses a RenderGraph object storing a DAG of nodes. The connections
 * between nodes establish aspects such as the order in which nodes are processed.
 *
 * Out of the RenderGraph, a tasklist is generated. The tasklist is what is executed every frame to
 * generate the rendering shown on the user's display.
 *
 * This package includes all the classes used by the renderer in the context of the DAG.
 * In particular, it includes a package with all the engine-provided nodes and a package
 * with all the engine-provided state changes that the nodes can take advantage of.
 *
 * External modules can take advantage of the existing nodes and state changes or use their owns.
 *
 * This whole package may one day be extracted from the engine and live in its own external module.
 */
@API
package org.terasology.rendering.dag;

import org.terasology.gestalt.module.sandbox.API;
