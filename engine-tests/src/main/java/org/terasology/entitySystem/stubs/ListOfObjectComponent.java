/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.entitySystem.stubs;

import java.util.ArrayList;
import java.util.List;

import org.terasology.entitySystem.Component;
import org.terasology.reflection.MappedContainer;

public class ListOfObjectComponent implements Component {
    public String shortName;
    public List<SubElement> elements = new ArrayList<>();

    @MappedContainer
    public static class SubElement {
        public String id;
        public String type;
    }
}
