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
package org.terasology.logic.console.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.commands.exceptions.CommandExecutionException;
import org.terasology.logic.console.commands.exceptions.CommandInitializationException;
import org.terasology.logic.console.commands.exceptions.CommandParameterParseException;
import org.terasology.logic.console.commands.exceptions.CommandSuggestionException;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.utilities.reflection.SpecificAccessibleObject;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * The core ICommand implementation and command information
 *
 * @author Limeth
 */
public abstract class AbstractCommand implements Command {
    public static final String METHOD_NAME_EXECUTE = "execute";
    public static final String METHOD_NAME_SUGGEST = "suggest";
    private static final Logger logger = LoggerFactory.getLogger(AbstractCommand.class);
    private final String name;
    private final String requiredPermission;
    private final boolean runOnServer;
    private final String description;
    private final String helpText;
    private final SpecificAccessibleObject<Method> executionMethod;
    private ImmutableList<CommandParameter> parameters;
    private int requiredParameterCount;
    private String usage;

    public AbstractCommand(String name, String requiredPermission, boolean runOnServer, String description, String helpText,
                           SpecificAccessibleObject<Method> executionMethod) {
        Preconditions.checkNotNull(executionMethod);

        this.name = name;
        this.requiredPermission = requiredPermission != null ? requiredPermission : PermissionManager.OPERATOR_PERMISSION;
        this.runOnServer = runOnServer;
        this.description = description;
        this.helpText = helpText;
        this.executionMethod = executionMethod;

        postConstruct();
    }

    public AbstractCommand(String name, String requiredPermission, boolean runOnServer, String description, String helpText,
                           String executionMethodName) {
        this.name = name;
        this.requiredPermission = requiredPermission != null ? requiredPermission : PermissionManager.OPERATOR_PERMISSION;
        this.runOnServer = runOnServer;
        this.description = description;
        this.helpText = helpText;
        this.executionMethod = findExecutionMethod(executionMethodName);

        postConstruct();
    }

    public AbstractCommand(String name, String requiredPermission, boolean runOnServer, String description, String helpText) {
        this(name, requiredPermission, runOnServer, description, helpText, (String) null);
    }

    public AbstractCommand(String name, boolean runOnServer, String description, String helpText) {
        this(name, PermissionManager.OPERATOR_PERMISSION, runOnServer, description, helpText);
    }

    private void postConstruct() {
        this.parameters = constructParametersNotNull();
        registerParameters();
        validateExecutionMethod();
        initUsage();
    }

    protected abstract List<CommandParameter> constructParameters();

    private ImmutableList<CommandParameter> constructParametersNotNull() {
        List<CommandParameter> constructedParameters = constructParameters();

        if (constructedParameters == null) {
            return ImmutableList.of();
        }

        for (int i = 0; i < constructedParameters.size(); i++) {
            if (constructedParameters.get(i) == null) {
                throw new CommandInitializationException("Invalid parameter definition #" + i + "; must not be null");
            }
        }

        return ImmutableList.copyOf(constructedParameters);
    }

    private void registerParameters() throws CommandInitializationException {
        requiredParameterCount = 0;
        boolean optionalFound = false;

        for (int i = 0; i < parameters.size(); i++) {
            CommandParameter parameter = parameters.get(i);

            if (parameter == null) {
                throw new CommandInitializationException("A command parameter must not be null! Index: " + i);
            }

            if (parameter.isVarargs() && i < parameters.size() - 1) {
                throw new CommandInitializationException("A varargs parameter must be at the end. Invalid: " + i + "; " + parameter.getName());
            }

            if (parameter.isRequired()) {
                if (!optionalFound) {
                    requiredParameterCount++;
                } else {
                    throw new CommandInitializationException("A command definition must not contain a required"
                            + " parameter (" + i + "; " + parameter.getName()
                            + ") after an optional parameter.");
                }
            } else if (!optionalFound) {
                optionalFound = true;
            }
        }
    }

    private SpecificAccessibleObject<Method> findExecutionMethod(String methodName) throws CommandInitializationException {
        String lookupName = methodName != null ? methodName : METHOD_NAME_EXECUTE;
        List<Method> methods = findMethods(lookupName);

        if (methods.size() < 0) {
            throw new CommandInitializationException("No " + lookupName + " method found");
        } else if (methods.size() > 1) {
            throw new CommandInitializationException("More than 1 " + lookupName + " methods found");
        }

        Method method = methods.get(0);

        return new SpecificAccessibleObject<>(method, this);
    }

    private List<Method> findMethods(String methodName) {
        Class<?> clazz = getClass();
        Method[] methods = clazz.getDeclaredMethods();

        List<Method> result = Lists.newArrayList();

        for (Method method : methods) {
            String currentMethodName = method.getName();

            if (currentMethodName.equals(methodName)) {
                result.add(method);
            }
        }

        return result;
    }

    private void checkArgumentCompatibility(Method method) throws CommandInitializationException {
        Class<?>[] methodParameters = method.getParameterTypes();

        if (methodParameters[0] != EntityRef.class) {
            throw new CommandInitializationException("The first parameter of method " + method.getName() + " in"
                    + " a command class must be an EntityRef (sender)");
        }

        int passableParameterCount = methodParameters.length - 1;

        for (int i = 0; i < passableParameterCount || i < parameters.size(); i++) {
            if (i >= passableParameterCount) {
                throw new CommandInitializationException("Missing " + (parameters.size() - passableParameterCount)
                        + " parameters in method " + method.getName()
                        + ", follow the parameter definitions from the"
                        + " 'constructParameters' method.");
            } else if (i >= parameters.size()) {
                throw new CommandInitializationException("Too many (" + (parameters.size() - passableParameterCount)
                        + ") parameters in method " + method.getName()
                        + ", follow the parameter definitions from the"
                        + " 'constructParameters' method.");
            }

            Class<?> expectedType = parameters.get(i).getTypeRaw();
            Class<?> providedType = methodParameters[i + 1];

            if (providedType.isPrimitive()) {
                providedType = CommandParameter.PRIMITIVES_TO_WRAPPERS.get(providedType);
            }

            if (!expectedType.isAssignableFrom(providedType)) {
                throw new CommandInitializationException("Cannot assign command argument from "
                        + providedType.getSimpleName() + " to "
                        + expectedType.getSimpleName() + "; "
                        + "command method parameter index: " + i);
            }
        }
    }

    private void validateExecutionMethod() {
        checkArgumentCompatibility(executionMethod.getAccessibleObject());
    }

    private void initUsage() {
        StringBuilder builder = new StringBuilder(name);

        for (CommandParameter param : parameters) {
            builder.append(' ').append(param.getUsage());
        }

        usage = builder.toString();
    }

    private Object[] processParameters(List<String> rawParameters, EntityRef sender, boolean includeSender) throws CommandParameterParseException {
        int senderRelativeIndex = includeSender ? 1 : 0;
        int singleParameterCount = parameters.size() + (endsWithVarargs() ? -1 : 0);
        Object[] processedParameters = new Object[parameters.size() + senderRelativeIndex];

        if (includeSender) {
            processedParameters[0] = sender;
        }

        for (int i = 0; i < singleParameterCount && i < rawParameters.size(); i++) {
            String rawParam = rawParameters.get(i);
            CommandParameter param = parameters.get(i);
            processedParameters[i + senderRelativeIndex] = param.getValue(rawParam);
        }

        if (endsWithVarargs()) {
            int varargsIndex = parameters.size() - 1;
            CommandParameter varargsParameter = parameters.get(varargsIndex);
            Object varargsResult;

            if (rawParameters.size() <= varargsIndex) {
                varargsResult = null;
            } else {
                StringBuilder rawParam = new StringBuilder(rawParameters.get(varargsIndex));

                for (int i = varargsIndex + senderRelativeIndex; i < rawParameters.size(); i++) {
                    rawParam.append(org.terasology.logic.console.commands.referenced.CommandParameter.ARRAY_DELIMITER_VARARGS).append(rawParameters.get(i));
                }

                varargsResult = varargsParameter.getValue(rawParam.toString());
            }

            processedParameters[varargsIndex + senderRelativeIndex] = varargsResult;
        }

        return processedParameters;
    }

    @Override
    public final String executeRaw(List<String> rawParameters, EntityRef sender) throws CommandExecutionException {
        Object[] processedParameters;

        try {
            processedParameters = processParameters(rawParameters, sender, true);
        } catch (CommandParameterParseException e) {
            String warning = "Invalid parameter '" + e.getParameter() + "'";
            String message = e.getMessage();

            if (message != null) {
                warning += ": " + message;
            }

            return warning;
        }

        try {
            Object result = executionMethod.getAccessibleObject().invoke(executionMethod.getTarget(), processedParameters);

            return result != null ? String.valueOf(result) : null;
        } catch (Throwable t) {
            throw new CommandExecutionException(t.getCause()); //Skip InvocationTargetException
        }
    }

    @Override
    public final Set<String> suggestRaw(final String currentValue, List<String> rawParameters, EntityRef sender) throws CommandSuggestionException {
        //Generate an array to be used as a parameter in the 'suggest' method
        Object[] processedParametersWithoutSender;

        try {
            processedParametersWithoutSender = processParameters(rawParameters, sender, false);
        } catch (CommandParameterParseException e) {
            String warning = "Invalid parameter '" + e.getParameter() + "'";
            String message = e.getMessage();

            if (message != null) {
                warning += ": " + message;
            }

            throw new CommandSuggestionException(warning);
        }

        //Get the suggested parameter to compare the result with
        CommandParameter suggestedParameter = null;

        for (int i = 0; i < processedParametersWithoutSender.length; i++) {
            if (processedParametersWithoutSender[i] == null) {
                suggestedParameter = parameters.get(i);
                break;
            }
        }

        if (suggestedParameter == null) {
            return Sets.newHashSet();
        }

        Set<Object> result = null;

        try {
            result = suggestedParameter.suggest(sender, processedParametersWithoutSender);
        } catch (Throwable t) {
            throw new CommandSuggestionException(t.getCause()); //Skip InvocationTargetException
        }

        if (result == null) {
            return Sets.newHashSet();
        }

        if (result.getClass().getComponentType() != suggestedParameter.getType()) {
            Class<?> requiredComponentClass = suggestedParameter.getType();
            Class<?> requiredClass = Array.newInstance(requiredComponentClass, 0).getClass();
            Class<?> providedClass = result.getClass();

            throw new CommandSuggestionException("The 'suggest' method of command class " + getClass().getCanonicalName()
                    + " returns a suggestion of an invalid type. Required: " + requiredClass.getCanonicalName()
                    + "; provided: " + providedClass.getCanonicalName());
        }

        Set<String> composedResult = composeAll(result, suggestedParameter);

        //Only return results starting with currentValue
        return Sets.filter(composedResult, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input != null && (currentValue == null || input.startsWith(currentValue));
            }
        });
    }

    private static Set<String> composeAll(Set<Object> collection, CommandParameter parameter) {
        Set<String> result = Sets.newHashSetWithExpectedSize(collection.size());

        for (Object component : collection) {
            result.add(parameter.composeSingle(component));
        }

        return result;
    }

    @Override
    public ImmutableList<CommandParameter> getParameters() {
        return parameters;
    }

    @Override
    public boolean isRunOnServer() {
        return runOnServer;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }

    @Override
    public String getHelpText() {
        return helpText;
    }

    @Override
    public boolean hasHelpText() {
        return helpText != null && !helpText.isEmpty();
    }

    public String getUsage() {
        return usage;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getRequiredParameterCount() {
        return requiredParameterCount;
    }

    public boolean endsWithVarargs() {
        return parameters.size() > 0 && parameters.get(parameters.size() - 1).isVarargs();
    }

    @Override
    public Object getSource() {
        return executionMethod.getTarget();
    }

    @Override
    public int compareTo(Command o) {
        return Command.COMPARATOR.compare(this, o);
    }

    @Override
    public String getRequiredPermission() {
        return requiredPermission;
    }

    public SpecificAccessibleObject<Method> getExecutionMethod() {
        return executionMethod;
    }
}
