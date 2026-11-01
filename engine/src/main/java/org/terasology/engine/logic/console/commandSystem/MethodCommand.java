// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.engine.utilities.reflection.SpecificAccessibleObject;
import org.terasology.gestalt.naming.Name;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

public final class MethodCommand extends AbstractCommand {
    private static final Logger logger = LoggerFactory.getLogger(MethodCommand.class);
    private static final String ENTITY_REF_NAME = "org.terasology.engine.entitySystem.entity.EntityRef";

    private MethodCommand(Name name, String requiredPermission, boolean runOnServer, String description,
                          String helpText,
                          SpecificAccessibleObject<Method> executionMethod, Context context) {
        super(name, requiredPermission, runOnServer, description, helpText, executionMethod, context);
    }

    /**
     * Creates a new {@code ReferencedCommand} to a specific method annotated with {@link Command}.
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
     * Registers all available command methods annotated with {@link Command}.
     */
    public static void registerAvailable(Object provider, Console console, Context context) {
        Predicate<? super Method> predicate = Predicates.<Method>and(ReflectionUtils.withModifier(Modifier.PUBLIC),
                ReflectionUtils.withAnnotation(Command.class));

        Class<?> providerClass = provider.getClass();
        Set<Method> commandMethods = ReflectionUtils.getAllMethods(providerClass, predicate);
        for (Method method : commandMethods) {
            String methodName = method.getName();
            String canonicalMethodName = method.getDeclaringClass().getCanonicalName();
            if (!hasSenderAnnotation(method)) {
                logger.atError().log("Command {} provided by {} contains a EntityRef without @Sender annotation, may cause a " +
                        "NullPointerException", methodName, providerClass.getSimpleName());
            }
            logger.debug("Registering command method {} in class {}", methodName, canonicalMethodName);
            try {
                SpecificAccessibleObject<Method> specificMethod = new SpecificAccessibleObject<>(method, provider);
                MethodCommand command = referringTo(specificMethod, context);
                console.registerCommand(command);
                logger.debug("Registered command method {} in class {}", methodName, canonicalMethodName);
            } catch (RuntimeException t) {
                logger.error("Failed to load command method {} in class {}", methodName, canonicalMethodName, t);
            }
        }
    }

    private static boolean hasSenderAnnotation(Method method) {
        final Class[] paramTypes = method.getParameterTypes();
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < paramAnnotations.length; i++) {
            if (paramTypes[i].getTypeName().equals(ENTITY_REF_NAME)) {
                if (paramAnnotations[i].length == 0) {
                    return false;
                } else {
                    for (Annotation annotation : paramAnnotations[i]) {
                        if (annotation instanceof Sender) {
                            return true;
                        }
                    }
                }
            }
        }
        return true;
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
                CommandParameterSuggester suggester = InjectionHelper.createWithConstructorInjection(suggesterClass,
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
