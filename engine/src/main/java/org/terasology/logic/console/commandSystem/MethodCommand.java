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
package org.terasology.logic.console.commandSystem;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.naming.Name;
import org.terasology.registry.InjectionHelper;
import org.terasology.utilities.reflection.SpecificAccessibleObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

/**
 */
public final class MethodCommand extends AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(MethodCommand.class);

    private MethodCommand(Name name, String requiredPermission, boolean runOnServer, String description, String helpText,
                          SpecificAccessibleObject<Method> executionMethod, Context context) {
        super(name, requiredPermission, runOnServer, description, helpText, executionMethod, context);
    }

    /**
     * Creates a new {@code ReferencedCommand} to a specific method
     * annotated with {@link org.terasology.logic.console.commandSystem.annotations.Command}.
     *
     * @param specificMethod The method to reference to
     * @return The command reference object created
     */
    public static MethodCommand referringTo(SpecificAccessibleObject<Method> specificMethod,
                                            Context context) {
        Method method = specificMethod.getAccessibleObject();
        Command commandAnnotation = method.getAnnotation(Command.class);

        Preconditions.checkNotNull(commandAnnotation);

        String nameString = commandAnnotation.value();

        if (nameString.length() <= 0) {
            nameString = method.getName();
        }

        Name name = new Name(nameString);

        return new MethodCommand(
                name,
                commandAnnotation.requiredPermission(),
                commandAnnotation.runOnServer(),
                commandAnnotation.shortDescription(),
                commandAnnotation.helpText(),
                specificMethod, context
        );
    }

    /**
     * Registers all available command methods annotated with {@link org.terasology.logic.console.commandSystem.annotations.Command}.
     */
    public static void registerAvailable(Object provider, Console console, Context context) {
        Predicate<? super Method> predicate = Predicates.<Method>and(ReflectionUtils.withModifier(Modifier.PUBLIC), ReflectionUtils.withAnnotation(Command.class));
        Set<Method> commandMethods = ReflectionUtils.getAllMethods(provider.getClass(), predicate);

        for (Method method : commandMethods) {
            logger.debug("Registering command method {} in class {}", method.getName(), method.getDeclaringClass().getCanonicalName());
            try {
                SpecificAccessibleObject<Method> specificMethod = new SpecificAccessibleObject<>(method, provider);
                MethodCommand command = referringTo(specificMethod, context);
                console.registerCommand(command);
                logger.debug("Registered command method {} in class {}", method.getName(), method.getDeclaringClass().getCanonicalName());
            } catch (RuntimeException t) {
                logger.error("Failed to load command method {} in class {}", method.getName(), method.getDeclaringClass().getCanonicalName(), t);
            }
        }
    }

    @Override
    protected List<Parameter> constructParameters(Context context) {
        SpecificAccessibleObject<Method> specificExecutionMethod = getExecutionMethod();
        Method executionMethod = specificExecutionMethod.getAccessibleObject();
        Class<?>[] methodParameters = executionMethod.getParameterTypes();
        Annotation[][] methodParameterAnnotations = executionMethod.getParameterAnnotations();
        List<Parameter> parameters = Lists.newArrayListWithExpectedSize(methodParameters.length);

        for (int i = 0; i < methodParameters.length; i++) {
            parameters.add(getParameterTypeFor(methodParameters[i], methodParameterAnnotations[i],
                    context));
        }

        return parameters;
    }

    private static Parameter getParameterTypeFor(Class<?> type, Annotation[] annotations,
                                                 Context context) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof CommandParam) {
                CommandParam parameterAnnotation
                        = (CommandParam) annotation;
                String name = parameterAnnotation.value();
                Class<? extends CommandParameterSuggester> suggesterClass = parameterAnnotation.suggester();
                boolean required = parameterAnnotation.required();
                CommandParameterSuggester  suggester = InjectionHelper.createWithConstructorInjection(suggesterClass,
                        context);

                if (type.isArray()) {
                    Class<?> childType = type.getComponentType();

                    return CommandParameter.array(name, childType, required, suggester, context);
                } else {
                    return CommandParameter.single(name, type, required, suggester, context);
                }
            } else if (annotation instanceof Sender) {
                return MarkerParameters.SENDER;
            }
        }

        return MarkerParameters.INVALID;
    }
}
