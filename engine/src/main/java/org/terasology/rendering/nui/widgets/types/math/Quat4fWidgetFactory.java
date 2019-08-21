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

import org.terasology.math.geom.Quat4f;
import org.terasology.reflection.TypeInfo;
import org.terasology.rendering.nui.widgets.types.RegisterTypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetBuilder;
import org.terasology.rendering.nui.widgets.types.TypeWidgetFactory;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;

import java.util.Optional;

@RegisterTypeWidgetFactory
public class Quat4fWidgetFactory implements TypeWidgetFactory {
    @Override
    public <T> Optional<TypeWidgetBuilder<T>> create(TypeInfo<T> type, TypeWidgetLibrary library) {
        if (!Quat4f.class.equals(type.getRawType())) {
            return Optional.empty();
        }

        // TODO: Possibly use euler angles or another easier-to-use format
        TypeWidgetBuilder<Quat4f> builder = new Quat4fWidgetBuilder(library)
                                                           .addAllFields();

        return Optional.of((TypeWidgetBuilder<T>) builder);
    }

    private static class Quat4fWidgetBuilder extends LabeledNumberFieldRowBuilder<Quat4f, Float> {
        public Quat4fWidgetBuilder(TypeWidgetLibrary library) {
            super(Quat4f.class, float.class, library);
        }

        @Override
        protected Quat4f getDefaultValue() {
            return new Quat4f(Quat4f.IDENTITY);
        }
    }
}
