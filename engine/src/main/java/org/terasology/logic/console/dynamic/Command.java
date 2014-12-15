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
package org.terasology.logic.console.dynamic;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.logic.console.Console;
import org.terasology.registry.CoreRegistry;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Limeth
 */
public abstract class Command extends BaseComponentSystem implements ICommand {
    public static final String METHOD_NAME_EXECUTE = "execute";
    public static final String METHOD_NAME_SUGGEST = "suggest";
    public static final char ARRAY_DELIMITER_DEFAULT = ',';
    public static final char ARRAY_DELIMITER_VARARGS = ' ';
    public static final char ARRAY_DELIMITER_ESCAPE_CHARACTER = '\\';
    private static final Logger LOGGER = LoggerFactory.getLogger(Command.class);
    private final String name;
    private final boolean runOnServer;
    private final String description;
    private final String helpText;
    private CommandParameter[] parameters;
    private int requiredParameterCount;
    private Method executionMethod;
    private Method suggestionMethod;
    private String usage;

    public Command(String name, boolean runOnServer, String description, String helpText) {
        this.name = name;
        this.runOnServer = runOnServer;
        this.description = description;
        this.helpText = helpText;

        registerParameters();
        registerExecutionMethod();
        registerSuggestionMethod();
        initUsage();
    }

    public void initialiseMore() {
    }

    @Override
    public final void initialise() {
        Console console = CoreRegistry.get(Console.class);

        initialiseMore();
        console.registerCommand(this);
    }

    protected abstract CommandParameter[] constructParameters();

    private void registerParameters() throws CommandInitializationException {
        CommandParameter[] editableParameters = constructParameters();

        if (editableParameters == null) {
            parameters = new CommandParameter[0];
            return;
        }

        requiredParameterCount = 0;
        boolean optionalFound = false;

        for (int i = 0; i < editableParameters.length; i++) {
            CommandParameter parameter = editableParameters[i];

            if (parameter.isVarargs() && i < editableParameters.length - 1) {
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

        this.parameters = editableParameters;
    }

    private void registerExecutionMethod() throws CommandInitializationException {
        List<Method> methods = findMethods(METHOD_NAME_EXECUTE);

        if (methods.size() < 0) {
            throw new CommandInitializationException("No " + METHOD_NAME_EXECUTE + " method found");
        } else if (methods.size() > 1) {
            throw new CommandInitializationException("More than 1 " + METHOD_NAME_EXECUTE + " methods found");
        }

        Method method = methods.get(0);

        checkArgumentCompatibility(method);

        executionMethod = method;
    }

    private void registerSuggestionMethod() throws CommandInitializationException {
        List<Method> methods = findMethods(METHOD_NAME_SUGGEST);

        if (methods.size() <= 0) {
            if(parameters.length > 0) {
                LOGGER.warn("No {} method found for command class {}.", METHOD_NAME_SUGGEST, getClass().getCanonicalName());
            }

            suggestionMethod = null;
            return;
        } else if (methods.size() > 1) {
            throw new CommandInitializationException("More than 1 " + METHOD_NAME_SUGGEST + " methods found");
        }

        Method method = methods.get(0);

        checkArgumentCompatibility(method);

        suggestionMethod = method;
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

            if (!expectedType.isAssignableFrom(providedType)) {
                throw new CommandInitializationException("Cannot assign command argument from "
                        + providedType.getSimpleName() + " to "
                        + expectedType.getSimpleName() + "; "
                        + "command method parameter index: " + i);
            }
        }
    }

    private void initUsage() {
        StringBuilder builder = new StringBuilder(name);

        for (CommandParameter param : parameters) {
            builder.append(' ').append(param.getUsage());
        }

        usage = builder.toString();
    }

    private Object[] processParameters(List<String> rawParameters, EntityRef sender) throws CommandParameterParseException {
        int singleParameterCount = parameters.length + (endsWithVarargs() ? -1 : 0);
        Object[] processedParameters = new Object[parameters.length + 1];
        processedParameters[0] = sender;

        for (int i = 0; i < singleParameterCount && i < rawParameters.size(); i++) {
            String rawParam = rawParameters.get(i);
            CommandParameter param = parameters[i];
            processedParameters[i + 1] = param.getValue(rawParam);
        }

        if (endsWithVarargs()) {
            int varargsIndex = parameters.length - 1;
            CommandParameter varargsParameter = parameters[varargsIndex];
            Object varargsResult;

            if (rawParameters.size() <= varargsIndex) {
                varargsResult = null;
            } else {
                StringBuilder rawParam = new StringBuilder(rawParameters.get(varargsIndex));

                for (int i = varargsIndex + 1; i < rawParameters.size(); i++) {
                    rawParam.append(ARRAY_DELIMITER_VARARGS).append(rawParameters.get(i));
                }

                varargsResult = varargsParameter.getValue(rawParam.toString());
            }

            processedParameters[requiredParameterCount + 1] = varargsResult;
        }

        return processedParameters;
    }

    @Override
    public final String executeRaw(List<String> rawParameters, EntityRef sender) throws CommandExecutionException {
        Object[] processedParameters;

        try {
            processedParameters = processParameters(rawParameters, sender);
        } catch (CommandParameterParseException e) {
            String warning = "Invalid parameter '" + e.getParameter() + "'";
            String message = e.getMessage();

            if (message != null) {
                warning += ": " + message;
            }

            return warning;
        }

        try {
            Object result = executionMethod.invoke(this, processedParameters);

            return result != null ? String.valueOf(result) : null;
        } catch (Throwable t) {
            throw new CommandExecutionException(t.getCause()); //Skip InvocationTargetException
        }
    }

    @Override
    public final String[] suggestRaw(final String currentValue, List<String> rawParameters, EntityRef sender) throws CommandSuggestionException {
        if(suggestionMethod == null) {
            return null;
        }

        Object[] processedParameters;

        try {
            processedParameters = processParameters(rawParameters, sender);
        } catch (CommandParameterParseException e) {
            String warning = "Invalid parameter '" + e.getParameter() + "'";
            String message = e.getMessage();

            if (message != null) {
                warning += ": " + message;
            }

            throw new CommandSuggestionException(warning);
        }
        try {
            String[] result = (String[]) suggestionMethod.invoke(this, processedParameters);

            if (result == null || result.length <= 0) {
                return result;
            }

            //Only return results starting with currentValue
            String[] filteredResult = filterArray(result, new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return input != null && (currentValue == null || input.startsWith(currentValue));
                }
            });

            return filteredResult;
        } catch (Throwable t) {
            throw new CommandSuggestionException(t.getCause()); //Skip InvocationTargetException
        }
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
    public String getHelpText() {
        return helpText;
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
    public int compareTo(ICommand o) {
        return ICommand.COMPARATOR.compare(this, o);
    }
}
