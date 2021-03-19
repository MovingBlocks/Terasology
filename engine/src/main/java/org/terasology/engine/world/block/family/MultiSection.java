// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;

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
