// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

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
package org.terasology.engine.rendering.dag;

import org.terasology.context.annotation.API;
