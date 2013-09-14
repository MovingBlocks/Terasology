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

/**
 * This package provides a system for loading and managing Assets - these are resources used by Terasology that are loaded from modules.
 * <p/>
 * The asset system features support for:
 * <ul>
 *      <li>Multiple file formats for an asset (with an AssetLoader per format)</li>
 *      <li>Multiple asset implementations, for different core system implementations (e.g. LWJGL vs Libdgx)</li>
 * </ul>
 */
// TODO: More javadoc
@API package org.terasology.asset;

import org.terasology.engine.API;
