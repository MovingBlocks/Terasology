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
package org.terasology.core.logic.blockDropGrammar;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.Component;
import org.terasology.reflection.MappedContainer;

import java.util.List;
import java.util.Map;

/**
 */
public final class BlockDropGrammarComponent implements Component {
    public List<String> blockDrops;
    public List<String> itemDrops;

    public Map<String, DropDefinition> droppedWithTool = Maps.newLinkedHashMap();

    @MappedContainer
    public static class DropDefinition {
        public List<String> blockDrops;
        public List<String> itemDrops;
    }
}
