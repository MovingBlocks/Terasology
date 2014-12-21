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
package org.terasology.logic.console.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.internal.exceptions.CommandExecutionException;
import org.terasology.logic.console.internal.exceptions.CommandInitializationException;
import org.terasology.logic.console.internal.exceptions.CommandParameterParseException;
import org.terasology.logic.console.internal.exceptions.CommandSuggestionException;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.utilities.reflection.SpecificAccessibleObject;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;

/**
 * The core ICommand implementation and command information
 *
 * @author Limeth
 */
public abstract class Command implements ICommand {
    public static final String METHOD_NAME_EXECUTE = "execute";
    public static final String METHOD_NAME_SUGGEST = "suggest";
    public static final char ARRAY_DELIMITER_DEFAULT = ',';
    public static final char ARRAY_DELIMITER_VARARGS = ' ';
    public static final char ARRAY_DELIMITER_ESCAPE_CHARACTER = '\\';
    private static final Logger LOGGER = LoggerFactory.getLogger(Command.class);
    private final String name;
    private final String requiredPermission;
    private final boolean runOnServer;
    private final String description;
    private final String helpText;
    private final SpecificAccessibleObject<Method> executionMethod;
    private CommandParameter[] parameters;
    private int requiredParameterCount;
    private String usage;

    public Command(String name, String requiredPermission, boolean runOnServer, String description, String helpText,
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

    public Command(String name, String requiredPermission, boolean runOnServer, String description, String helpText,
                   String executionMethodName) {
        this.name = name;
        this.requiredPermission = requiredPermission != null ? requiredPermission : PermissionManager.OPERATOR_PERMISSION;
        this.runOnServer = runOnServer;
        this.description = description;
        this.helpText = helpText;
        this.executionMethod = findExecutionMethod(executionMethodName);

        postConstruct();
    }

    public Command(String name, String requiredPermission, boolean runOnServer, String description, String helpText) {
        this(name, requiredPermission, runOnServer, description, helpText, (String) null);
    }

    public Command(String name, boolean runOnServer, String description, String helpText) {
        this(name, PermissionManager.OPERATOR_PERMISSION, runOnServer, description, helpText);
    }

    private void postConstruct() {
        this.parameters = constructParametersNotNull();
        registerParameters();
        validateExecutionMethod();
        initUsage();
    }

    protected abstract CommandParameter[] constructParameters();

    private CommandParameter[] constructParametersNotNull() {
        CommandParameter[] constructedParameters = constructParameters();
        return constructedParameters != null ? constructedParameters : new CommandParameter[0];
    }

    private void registerParameters() throws CommandInitializationException {
        requiredParameterCount = 0;
        boolean optionalFound = false;

        for (int i = 0; i < parameters.length; i++) {
            CommandParameter parameter = parameters[i];

            if (parameter == null) {
                throw new CommandInitializationException("A command parameter must not be null! Index: " + i);
            }

            if (parameter.isVarargs() && i < parameters.length - 1) {
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

    private SpecificAccessibleObject<Method> findSuggestionMethod(String methodName) throws CommandInitializationException {
        String lookupName = methodName != null ? methodName : METHOD_NAME_SUGGEST;
        List<Method> methods = findMethods(lookupName);

        if (methods.size() <= 0) {
            LOGGER.warn("No '{}' method found for command '{}' in class {}", lookupName, name, getClass().getCanonicalName());
            return null;
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

        for (int i = 0; i < passableParameterCount || i < parameters.length; i++) {
            if (i >= passableParameterCount) {
                throw new CommandInitializationException("Missing " + (parameters.length - passableParameterCount)
                        + " parameters in method " + method.getName()
                        + ", follow the parameter definitions from the"
                        + " 'constructParameters' method.");
            } else if (i >= parameters.length) {
                throw new CommandInitializationException("Too many (" + (parameters.length - passableParameterCount)
                        + ") parameters in method " + method.getName()
                        + ", follow the parameter definitions from the"
                        + " 'constructParameters' method.");
            }

            Class<?> expectedType = parameters[i].getTypeRaw();
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
        int singleParameterCount = parameters.length + (endsWithVarargs() ? -1 : 0);
        Object[] processedParameters = new Object[parameters.length + senderRelativeIndex];

        if (includeSender) {
            processedParameters[0] = sender;
        }

        for (int i = 0; i < singleParameterCount && i < rawParameters.size(); i++) {
            String rawParam = rawParameters.get(i);
            CommandParameter param = parameters[i];
            processedParameters[i + senderRelativeIndex] = param.getValue(rawParam);
        }

        if (endsWithVarargs()) {
            int varargsIndex = parameters.length - 1;
            CommandParameter varargsParameter = parameters[varargsIndex];
            Object varargsResult;

            if (rawParameters.size() <= varargsIndex) {
                varargsResult = null;
            } else {
                StringBuilder rawParam = new StringBuilder(rawParameters.get(varargsIndex));

                for (int i = varargsIndex + senderRelativeIndex; i < rawParameters.size(); i++) {
                    rawParam.append(ARRAY_DELIMITER_VARARGS).append(rawParameters.get(i));
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
    public final String[] suggestRaw(final String currentValue, List<String> rawParameters, EntityRef sender) throws CommandSuggestionException {
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
                suggestedParameter = parameters[i];
                break;
            }
        }

        if (suggestedParameter == null) {
            return null;
        }

        Object[] result = null;

        try {
            result = suggestedParameter.suggest(sender, processedParametersWithoutSender);
        } catch (Throwable t) {
            throw new CommandSuggestionException(t.getCause()); //Skip InvocationTargetException
        }

        if (result == null) {
            return null;
        }

        if (result.getClass().getComponentType() != suggestedParameter.getType()) {
            Class<?> requiredComponentClass = suggestedParameter.getType();
            Class<?> requiredClass = Array.newInstance(requiredComponentClass, 0).getClass();
            Class<?> providedClass = result.getClass();

            throw new CommandSuggestionException("The 'suggest' method of command class " + getClass().getCanonicalName()
                    + " returns a suggestion of an invalid type. Required: " + requiredClass.getCanonicalName()
                    + "; provided: " + providedClass.getCanonicalName());
        }

        String[] composedResult = composeAll(result, suggestedParameter);

        //Only return results starting with currentValue
        return filterArray(composedResult, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input != null && (currentValue == null || input.startsWith(currentValue));
            }
        });
    }

    private static String[] composeAll(Object[] array, CommandParameter parameter) {
        String[] result = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            result[i] = parameter.composeSingle(array[i]);
        }

        return result;
    }

    private <T> T[] filterArray(T[] array, Predicate<T> predicate) {
        Class<?> componentClass = array.getClass().getComponentType();
        int filteredSize = 0;

        for (T element : array) {
            if (predicate.apply(element)) {
                filteredSize++;
            }
        }

        @SuppressWarnings("unchecked")
        T[] filteredArray = (T[]) Array.newInstance(componentClass, filteredSize);
        int insertionIndex = 0;

        for (T element : array) {
            if (predicate.apply(element)) {
                filteredArray[insertionIndex++] = element;
            }
        }

        return filteredArray;
    }

    @Override
    public CommandParameter[] getParameters() {
        CommandParameter[] clone = new CommandParameter[parameters.length];

        System.arraycopy(parameters, 0, clone, 0, parameters.length);

        return clone;
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
        return parameters.length > 0 && parameters[parameters.length - 1].isVarargs();
    }

    @Override
    public Object getSource() {
        return executionMethod.getTarget();
    }

    @Override
    public int compareTo(ICommand o) {
        return ICommand.COMPARATOR.compare(this, o);
    }

    @Override
    public String getRequiredPermission() {
        return requiredPermission;
    }

    public SpecificAccessibleObject<Method> getExecutionMethod() {
        return executionMethod;
    }
}
