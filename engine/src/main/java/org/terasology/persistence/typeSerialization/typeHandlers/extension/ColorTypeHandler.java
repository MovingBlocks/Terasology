/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeSerialization.typeHandlers.extension;

import org.terasology.persistence.typeSerialization.typeHandlers.SimpleTypeHandler;
import org.terasology.protobuf.EntityData;
import org.terasology.rendering.nui.Color;

/**
 * @author synopia
 */
public class ColorTypeHandler extends SimpleTypeHandler<Color> {
    @Override
    public EntityData.Value serialize(Color value) {
        return EntityData.Value.newBuilder().addFloat(value.rf()).addFloat(value.gf()).addFloat(value.bf()).addFloat(value.af()).build();
    }

    @Override
    public Color deserialize(EntityData.Value value) {
        if (value.getFloatCount() > 3) {
            return new Color(value.getFloat(0), value.getFloat(1), value.getFloat(2), value.getFloat(3));
        }
        return null;
    }
}
