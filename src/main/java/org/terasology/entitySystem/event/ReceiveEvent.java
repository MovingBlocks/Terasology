/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.entitySystem.event;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.RegisterMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark up methods that can be registered to receive events through the EventSystem
 * <p/>
 * These methods should have the form
 * <code>public void handlerMethod(EventType event, EntityRef entity)</code>
 *
 * @author Immortius <immortius@gmail.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReceiveEvent {
    /**
     * What components that the entity must have for this method to be invoked
     */
    Class<? extends Component>[] components() default {};

    RegisterMode netFilter() default RegisterMode.ALWAYS;

    int priority() default EventPriority.PRIORITY_NORMAL;
}
