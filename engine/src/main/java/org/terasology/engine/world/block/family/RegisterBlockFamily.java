// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;


import org.terasology.context.annotation.Index;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a {@link BlockFamily} to be registered in the environment.
 * This annotation takes one argument, which is the name of the BlockFamily to be registered.<br><br>
 * Examples:<br><br>
 * <code>@RegisterBlockFamily("example")</code><br>
 * In this case, a block family named "example" will be registered.<br><br>
 * <code>@RegisterBlockFamily("painting")</code><br>
 * <code>@BlockSections({"first", "second", "third"})</code><br>
 * In this case, a block family named "painting" which has three different sections named "first, "second" and "third" will be registered.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Index
public @interface RegisterBlockFamily {
    String value();
}
