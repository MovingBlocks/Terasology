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
package org.terasology.rendering.nui.widgets.types.math;

import org.terasology.math.geom.Vector3i;
import org.terasology.reflection.TypeInfo;
import org.terasology.rendering.nui.widgets.types.RegisterTypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;

import java.util.Optional;

@RegisterTypeWidgetFactory
public class Vector3iWidgetFactory implements TypeWidgetFactory {
    @Override
    public <T> Optional<TypeWidgetBuilder<T>> create(TypeInfo<T> type, TypeWidgetLibrary library) {
        if (!Vector3i.class.equals(type.getRawType())) {
            return Optional.empty();
        }

        TypeWidgetBuilder<Vector3i> builder = new Vector3iWidgetBuilder(library)
                                                  .addAllFields();

        return Optional.of((TypeWidgetBuilder<T>) builder);
    }

    private static class Vector3iWidgetBuilder extends LabeledNumberFieldRowBuilder<Vector3i, Integer> {
        public Vector3iWidgetBuilder(TypeWidgetLibrary library) {
            super(Vector3i.class, int.class, library);
        }

        @Override
        protected Vector3i getDefaultValue() {
            return Vector3i.zero();
        }
    }
}
