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
     * The ID of the {@link org.terasology.rendering.nui.widgets.UILabel} in the {@link UIWidget} generated
     * by a {@link TypeWidgetFactory} that can be used to label the object being edited.
     *
     * The existence of a widget with this ID in the generated widget means that the generated widget is
     * usually expandable and takes up a lot of space. It is thus possible to label the widget without
     * having to create a separate widget. If a widget with this ID is missing, it means that the
     * generated widget is simple (like a checkbox or a textbox) and does not have space to label itself.
     */
    String LABEL_WIDGET_ID = "objectLabelWidget";

    /**
     * Creates a {@link UIWidget} bound to an object of the given type. The created widget can
     * be used to edit the object, which must not be null. The object is accessed via an
     * {@link Binding}, so that it can be set at the source.
     * <p>
     * <li>To correctly account for changes in the object, {@link Binding#get() binding.get()} must always be
     * used to retrieve the object, and the result must never be cached.</li>
     * <li>If a {@link Binding} must be created for an object that is contained in {@code binding}
     * (like a field, for example), it must be created via
     * {@link Binding#makeChildBinding(Binding) binding.makeChildBinding()}.</li>
     *
     * TODO: Add must also handle null clause
     * TODO: Add "nameable" widget details
     *
     * @param <T>     The type of the object.
     * @param binding  A {@link Binding} to the object to create the {@link UIWidget} for.
     * @param type    The {@link TypeInfo} of the type of the object.
     * @param library The {@link TypeWidgetLibrary} to create widgets of other types.
     * @return An {@link Optional} containing the created {@link UIWidget}, else
     * {@link Optional#empty()} if the factory does not support this type of object.
     */
    // TODO: Split into create and bind, cache results of create (if possible)
    //  to support recursive types
    <T> Optional<UIWidget> create(Binding<T> binding, TypeInfo<T> type, TypeWidgetLibrary library);
}
