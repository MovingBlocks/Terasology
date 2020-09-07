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
package org.terasology.rendering.opengl.fbms;

import org.terasology.gestalt.module.sandbox.API;
