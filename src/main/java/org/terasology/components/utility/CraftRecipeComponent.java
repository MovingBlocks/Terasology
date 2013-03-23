/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.components.utility;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.Component;

import java.util.List;
import java.util.Map;

public class CraftRecipeComponent implements Component {
    public boolean fullMatch = true;
    public Map<String, List<String>> recipe = Maps.newHashMap();
    public Map<String, String> refinement = Maps.newHashMap();
    public byte resultCount = 1;
}
