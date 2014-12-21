/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.console.commands.referenced;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commands.CommandParameter;
import org.terasology.logic.console.commands.CommandParameterSuggester;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.reflection.SpecificAccessibleObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * @author Limeth
 */
public final class ReferencedCommand extends org.terasology.logic.console.commands.Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferencedCommand.class);

    private ReferencedCommand(String name, String requiredPermission, boolean runOnServer, String description, String helpText,
                              SpecificAccessibleObject<Method> executionMethod) {
        super(name, requiredPermission, runOnServer, description, helpText, executionMethod);
    }

    /**
     * Creates a new {@code ReferencedCommand} to a specific method
     * annotated with {@link org.terasology.logic.console.commands.referenced.Command}.
     *
     * @param specificMethod The method to reference to
     * @return The command reference object created
     */
    public static ReferencedCommand referringTo(SpecificAccessibleObject<Method> specificMethod) {
        Method method = specificMethod.getAccessibleObject();
        Command commandAnnotation = method.getAnnotation(Command.class);

        Preconditions.checkNotNull(commandAnnotation);

        String name = commandAnnotation.value();

        if (name.length() <= 0) {
            name = method.getName();
        }

        return new ReferencedCommand(
                name,
                commandAnnotation.requiredPermission(),
                commandAnnotation.runOnServer(),
                commandAnnotation.shortDescription(),
                commandAnnotation.helpText(),
                specificMethod
        );
    }

    /**
     * Registers all available command methods annotated with {@link Command}.
     */
    public static void registerAvailable(Object provider) {
        Predicate<? super Method> predicate = Predicates.<Method>and(ReflectionUtils.withModifier(Modifier.PUBLIC), ReflectionUtils.withAnnotation(Command.class));
        Set<Method> commandMethods = ReflectionUtils.getAllMethods(provider.getClass(), predicate);
        Console console = CoreRegistry.get(Console.class);

        for (Method method : commandMethods) {
            LOGGER.debug("Registering referenced command method {} in class {}", method.getName(), method.getDeclaringClass().getCanonicalName());
            try {
                SpecificAccessibleObject<Method> specificMethod = new SpecificAccessibleObject<>(method, provider);
                ReferencedCommand command = referringTo(specificMethod);
                console.registerCommand(command);
                LOGGER.debug("Registered referenced command method {} in class {}", method.getName(), method.getDeclaringClass().getCanonicalName());
            } catch (Throwable t) {
                LOGGER.error("Failed to load referenced command method {} in class {}", method.getName(), method.getDeclaringClass().getCanonicalName(), t);
            }
        }
    }

    /**
     * Registers this command to the console.
     *
     * @return {@code this}, the same object
     */
    public ReferencedCommand register() {
        Console console = CoreRegistry.get(Console.class);

        console.registerCommand(this);

        return this;
    }

    @Override
    protected CommandParameter[] constructParameters() {
        SpecificAccessibleObject<Method> specificExecutionMethod = getExecutionMethod();
        Method executionMethod = specificExecutionMethod.getAccessibleObject();
        Class<?>[] methodParameters = executionMethod.getParameterTypes();
        Annotation[][] methodParameterAnnotations = executionMethod.getParameterAnnotations();
        CommandParameter[] parameters = new CommandParameter[methodParameters.length - 1];

        for (int i = 1; i < methodParameters.length; i++) {
            parameters[i - 1] = getParameterFor(methodParameters[i], methodParameterAnnotations[i]);
        }

        return parameters;
    }

    private static CommandParameter getParameterFor(Class<?> type, Annotation[] annotations) {
        String name = null;
        Character arrayDelimiter = null;
        Class<? extends CommandParameterSuggester> suggesterClass = org.terasology.logic.console.commands.referenced.CommandParameter.NullCommandParameterSuggester.class;
        boolean required = true;

        for (Annotation annotation : annotations) {
            if (annotation instanceof org.terasology.logic.console.commands.referenced.CommandParameter) {
                org.terasology.logic.console.commands.referenced.CommandParameter parameterAnnotation
                        = (org.terasology.logic.console.commands.referenced.CommandParameter) annotation;
                name = parameterAnnotation.value();
                arrayDelimiter = parameterAnnotation.arrayDelimiter();
                suggesterClass = parameterAnnotation.suggester();
                required = parameterAnnotation.required();
                break;
            }
        }

        CommandParameterSuggester suggester;

        if (suggesterClass != org.terasology.logic.console.commands.referenced.CommandParameter.NullCommandParameterSuggester.class) {
            try {
                suggester = suggesterClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            suggester = null;
        }

        if (type.isArray()) {
            Class<?> childType = type.getComponentType();

            return CommandParameter.array(name, childType, arrayDelimiter, required, suggester);
        } else {
            return CommandParameter.single(name, type, required, suggester);
        }
    }
}
