// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes the sections that a block can use such as top, left, right, bottom
 * but other sections can be used.
 * <p>
 *     Each section will override the existing values on the base block when that family section is used. look in
 *     deserializeSectionDefinitionData under BlockFamilyDefinitionFormat for properties that get override by the
 *     the BlockSection
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface BlockSections {
    String[] value();
}
