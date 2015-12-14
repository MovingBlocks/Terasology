/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.world.block.family;

import com.google.common.collect.ImmutableSet;
import org.terasology.util.Varargs;

/**
 * A multi-section is a section that are loaded and applied to one or more other sections. Example: The Sides multi-section provides data that applies to the Left, Right,
 * Front and Back sections.
 */
public class MultiSection {
    private final String name;
    private final ImmutableSet<String> appliesToSections;

    public MultiSection(String name, String coversSection, String ... otherSections) {
        this.name = name;
        this.appliesToSections = Varargs.combineToSet(coversSection, otherSections);
    }

    /**
     * @return The name of the section
     */
    public String getName() {
        return name;
    }

    /**
     * @return A set of sections this section is applied to
     */
    public ImmutableSet<String> getAppliesToSections() {
        return appliesToSections;
    }
}
