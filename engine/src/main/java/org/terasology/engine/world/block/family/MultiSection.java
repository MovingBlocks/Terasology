/*
 * Copyright 2018 MovingBlocks
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A multi-section is a section that are loaded and applied to one or more other sections.
 * Example: The Sides multi-section provides data that applies to the Left, Right, Front and Back sections.
 * <p>
 *     Each mult-section will  have a collection of sections and each of those sections can override an existing values on the base block when that
 *     family section is used. look in BlockFamilyDefinitionFormat under deserializeSectionDefinitionData for properties that
 *     get override by Multi-Section
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface MultiSection {
    String name();
    String coversSection();
    String[] appliesToSections();
}
