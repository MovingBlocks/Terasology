/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.console.commandSystem.adapter;

import org.terasology.gestalt.module.sandbox.API;

/**
 * Used for providing parameters to {@code execute} and {@code suggest} methods of {@link org.terasology.logic.console.commandSystem.AbstractCommand}
 *
 */
@API
public interface ParameterAdapter<T> {
    T parse(String raw);
    String convertToString(T value);
}
