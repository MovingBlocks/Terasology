// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

/**
 * This package includes a number of Frame Buffer managers (fbms).
 *
 * Frame Buffer managers are responsible for the instantiation, storage, provision and eventually disposal
 * of Frame Buffer Objects - the buffers used by the renderer for reading and writing rendering data.
 *
 * Different managers handle buffers differently: some buffers are immutable during the life-cycle of
 * the renderer while others are regenerated when some criteria are fullfilled, i.e. a change in
 * screen resolution.
 *
 * External module may use the engine-provided managers available through this package or create their own.
 */
@API
package org.terasology.engine.rendering.opengl.fbms;

import org.terasology.context.annotation.API;
