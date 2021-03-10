// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Indicates that this {@link WorldRasterizer} depends on the output/result of other rasterizers, and should
 *  be executed after them.
 *  <p><em>Note:</em> This only handles first-order dependencies (e.g. A depends on B) and will not work with
 *  higher order dependencies (e.g. A depends on B which depends on C).</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiresRasterizer {
    Class<? extends WorldRasterizer>[] value();
}
