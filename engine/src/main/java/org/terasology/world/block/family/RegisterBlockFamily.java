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
public @interface RegisterBlockFamily {
    String value();
}
