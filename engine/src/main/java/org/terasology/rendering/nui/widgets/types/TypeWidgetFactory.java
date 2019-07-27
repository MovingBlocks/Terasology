/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.rendering.nui.widgets.types;

import org.terasology.reflection.TypeInfo;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;

import java.util.Optional;

/**
 * Creates a {@link UIWidget} for objects sharing similar structure.
 * <p>
 * Supports dependency injection via {@link org.terasology.registry.In}.
 */
public interface TypeWidgetFactory {
    /**
     * Creates a {@link UIWidget} bound to an object of the given type. The created widget can
     * be used to edit the object, which must not be null. The object is accessed via an
     * {@link Binding}, so that immutable objects can also be set correctly.
     * <p>
     * To correctly account for immutability, {@link Binding#get() object.get()} must always be
     * used to retrieve the object, and the result must never be cached. If a {@link Binding}
     * must be created for an object that is contained in {@code object} (like a field, for example),
     * it must be created via {@link Binding#makeChildBinding(Binding) object.makeChildBinding()}.
     *
     * TODO: Add must also handle null clause
     *
     * @param <T>     The type of the object.
     * @param binding  A {@link Binding} to the object to create the {@link UIWidget} for.
     * @param type    The {@link TypeInfo} of the type of the object.
     * @param library The {@link TypeWidgetLibrary} to create widgets of other types.
     * @return An {@link Optional} containing the created {@link UIWidget}, else
     * {@link Optional#empty()} if the factory does not support this type of object.
     */
    // TODO: Use TypeInfo
    // TODO: Split into create and bind, cache results of create (if possible)
    //  to support recursive types
    <T> Optional<UIWidget> create(Binding<T> binding, TypeInfo<T> type, TypeWidgetLibrary library);
}
