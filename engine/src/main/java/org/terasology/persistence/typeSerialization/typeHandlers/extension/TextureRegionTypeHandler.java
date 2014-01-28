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

import org.terasology.asset.Asset;
import org.terasology.asset.Assets;
import org.terasology.persistence.typeSerialization.typeHandlers.SimpleTypeHandler;
import org.terasology.protobuf.EntityData;
import org.terasology.rendering.assets.texture.TextureRegion;

/**
 * @author Immortius
 */
public class TextureRegionTypeHandler extends SimpleTypeHandler<TextureRegion> {

    @Override
    public EntityData.Value serialize(TextureRegion value) {
        if (value != null && value instanceof Asset) {
            return EntityData.Value.newBuilder().addString(((Asset) value).getURI().toSimpleString()).build();
        }
        return null;
    }

    @Override
    public TextureRegion deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            return Assets.getTextureRegion(value.getString(0));
        }
        return null;
    }
}
