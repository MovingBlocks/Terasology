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

package org.terasology.world.chunks.blockdata;

import org.terasology.gestalt.module.sandbox.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods to register extra per-block data fields.
 * These must be inside classes annotated with {@link org.terasology.world.chunks.blockdata.ExtraDataSystem}.
 * The valid values for bitSize are 4, 8 and 16.
 * The correct format is
 * {@code
 * @RegisterExtraData(name="exampleModule.grassNutrients", bitSize=8)
 * public static boolean shouldHaveNutrients(Block block) {
 *     return block.isGrass();
 * }
 * }.
 */
@API
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RegisterExtraData {
    String name();
    int bitSize();
}
