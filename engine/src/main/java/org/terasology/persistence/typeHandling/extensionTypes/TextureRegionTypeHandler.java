/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.persistence.typeHandling.extensionTypes;

import org.terasology.asset.Asset;
import org.terasology.asset.Assets;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;
import org.terasology.rendering.assets.TextureRegion;

/**
 * @author Immortius
 */
public class TextureRegionTypeHandler extends StringRepresentationTypeHandler<TextureRegion> {
    @Override
    public String getAsString(TextureRegion item) {
        if (item instanceof Asset) {
            return ((Asset) item).getURI().toSimpleString();
        }
        return "";
    }

    @Override
    public TextureRegion getFromString(String representation) {
        return Assets.getTextureRegion(representation);
    }
}
