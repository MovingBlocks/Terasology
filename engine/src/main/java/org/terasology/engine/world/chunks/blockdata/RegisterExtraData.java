// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.blockdata;

import org.terasology.context.annotation.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods to register extra per-block data fields.
 * These must be inside classes annotated with {@link ExtraDataSystem}.
 * The valid values for bitSize are 4, 8 and 16.
 * The correct format is
 * <pre>
 * {@code @RegisterExtraData(name="exampleModule.grassNutrients", bitSize=8)}
 * public static boolean shouldHaveNutrients(Block block) {
 *     return block.isGrass();
 * }
 * </pre>
 */
@API
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RegisterExtraData {
    String name();
    int bitSize();
}
