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
package org.terasology.rendering.nui.skin;

import com.google.common.collect.Maps;
import org.terasology.assets.AssetData;

import java.util.Map;

/**
 */
public class UISkinData implements AssetData {
    Map<String, UIStyleFamily> skinFamilies;

    public UISkinData(Map<String, UIStyleFamily> families) {
        skinFamilies = Maps.newHashMap(families);
    }

    public UIStyleFamily getFamily(String familyName) {
        return skinFamilies.get(familyName);
    }
}
