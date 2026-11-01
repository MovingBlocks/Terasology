// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

/**
 * This package provides a low-level system for describing classes and fields, with support for construction and field access.
 * Essentially it is a simplified reflection
 * framework.
 * <br><br>
 * To support this functionality, a copy-strategy library is used to provide copying support for types. This is used instead of cloning because
 * <ol>
 *     <li>Not all types support cloning, including types outside of our control</li>
 *     <li>Cloning doesn't allow for possible performance improvements though runtime generated code</li>
 *     <li>Cloning is generally a poorly implemented feature of Java - copy constructors and methods are preferred</li>
 * </ol>
 * <br><br>
 * Additionally, ReflectFactory is used to provide support for construction and field access, to allow for alternate implementations.
 */
@API package org.terasology.engine.audio.events;

import org.terasology.context.annotation.API;
