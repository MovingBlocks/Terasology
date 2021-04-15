// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.console.commandSystem.annotations;

import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.console.commandSystem.CommandParameterSuggester;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * Used to specify information and behavior of a command parameter
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CommandParam {

    /**
     * @return The parameter name
     */
    String value() default "";

    /**
     * @return Whether the argument value is required to be present on command execution or not
     */
    boolean required() default true;

    /**
     * @return The class used for suggesting values for this parameter
     */
    Class<? extends CommandParameterSuggester> suggester() default EmptyCommandParameterSuggester.class;

    /**
     * A class representing no suggester - needed, because Annotations fields don't accept nulls
     */
    final class EmptyCommandParameterSuggester implements CommandParameterSuggester {
        @Override
        public Set<Object> suggest(EntityRef sender, Object[] resolvedParameters) {
            return Sets.newHashSet();
        }
    }
}
