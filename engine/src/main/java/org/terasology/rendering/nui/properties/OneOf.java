/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.properties;

/**
 * Created by synopia on 03.01.14.
 */

import org.terasology.module.sandbox.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@API
public interface OneOf {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @API
    public @interface Enum {
        String label() default "";
        
        String description() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @API
    public @interface List {
        String label() default "";
        
        String description() default "";

        String[] items();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @API
    public @interface Provider {
        String label() default "";
        
        String description() default "";

        String name();
    }
}
