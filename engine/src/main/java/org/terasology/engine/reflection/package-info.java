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
 * This package provides a low-level system for describing classes and fields, with support for construction and field access. Essentially it is a simplified reflection
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
package org.terasology.reflection;
