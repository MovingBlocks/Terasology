/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.i18n.assets;

import java.util.Locale;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;

/**
 * TODO Type description
 */
public abstract class I18n extends Asset<I18nData> {

    protected I18n(ResourceUrn urn, AssetType<?, I18nData> assetType) {
        super(urn, assetType);
    }

    public String translate(String id, String string, Locale german) {
        // TODO Auto-generated method stub
        return null;
    }

}
