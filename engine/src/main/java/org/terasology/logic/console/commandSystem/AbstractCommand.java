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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.exceptions.CommandExecutionException;
import org.terasology.logic.console.commandSystem.exceptions.CommandInitializationException;
import org.terasology.logic.console.commandSystem.exceptions.CommandParameterParseException;
import org.terasology.logic.console.commandSystem.exceptions.CommandSuggestionException;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.naming.Name;
import org.terasology.utilities.reflection.SpecificAccessibleObject;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * The core ICommand implementation and command information
 *
 * @author Limeth
 */
public abstract class AbstractCommand implements ConsoleCommand {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCommand.class);
    private final Name name;
    private final String requiredPermission;
    private final boolean runOnServer;
    private final String description;
    private final String helpText;
    private final SpecificAccessibleObject<Method> executionMethod;
    private ImmutableList<CommandParameter> commandParameters;
    private ImmutableList<Parameter> executionMethodParameters;
    private int requiredParameterCount;
    private String usage;

    public AbstractCommand(Name name, String requiredPermission, boolean runOnServer, String description, String helpText,
                           SpecificAccessibleObject<Method> executionMethod) {
        Preconditions.checkNotNull(executionMethod);
        Preconditions.checkNotNull(description);
        Preconditions.checkNotNull(helpText);

        this.name = name;
        this.requiredPermission = requiredPermission != null ? requiredPermission : PermissionManager.OPERATOR_PERMISSION;
        this.runOnServer = runOnServer;
        this.description = description;
        this.helpText = helpText;
        this.executionMethod = executionMethod;

        constructParametersNotNull();
        registerParameters();
        validateExecutionMethod();
        initUsage();
    }

    /**
     * @return A list of parameter types provided to the execution method.
     */
    protected abstract List<Parameter> constructParameters();

    private void constructParametersNotNull() {
        List<Parameter> constructedParameters = constructParameters();

        if (constructedParameters == null || constructedParameters.size() <= 0) {
            commandParameters = ImmutableList.of();
            executionMethodParameters = ImmutableList.of();
            return;
        }

        ImmutableList.Builder<CommandParameter> commandParameterBuilder = ImmutableList.builder();

        for (int i = 0; i < constructedParameters.size(); i++) {
            Parameter type = constructedParameters.get(i);

            if (type == null) {
                throw new CommandInitializationException("Invalid parameter definition #" + i + "; must not be null");
            } else if (type instanceof CommandParameter) {
                commandParameterBuilder.add((CommandParameter) type);
            }
        }

        commandParameters = commandParameterBuilder.build();
        executionMethodParameters = ImmutableList.copyOf(constructedParameters);
    }

    private void registerParameters() throws CommandInitializationException {
        requiredParameterCount = 0;
        boolean optionalFound = false;

        for (int i = 0; i < commandParameters.size(); i++) {
            CommandParameter parameter = commandParameters.get(i);

            if (parameter == null) {
                throw new CommandInitializationException("A command parameter must not be null! Index: " + i);
            }

            if (parameter.isVarargs() && i < commandParameters.size() - 1) {
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

    private void checkArgumentCompatibility(Method method) throws CommandInitializationException {
        Class<?>[] methodParameters = method.getParameterTypes();

        int executionMethodParametersSize = executionMethodParameters.size();
        int methodParameterCount = methodParameters.length;

        for (int i = 0; i < methodParameterCount || i < executionMethodParametersSize; i++) {
            if (i >= methodParameterCount) {
                throw new CommandInitializationException("Missing " + (executionMethodParametersSize - methodParameterCount)
                        + " parameters in method " + method.getName()
                        + ", follow the parameter definitions from the"
                        + " 'constructParameters' method.");
            } else if (i >= executionMethodParametersSize) {
                throw new CommandInitializationException("Too many (" + (methodParameterCount - executionMethodParametersSize)
                        + ") parameters in method " + method.getName()
                        + ", follow the parameter definitions from the"
                        + " 'constructParameters' method.");
            }

            Parameter expectedParameterType = executionMethodParameters.get(i);
            Optional<? extends Class<?>> expectedType = expectedParameterType.getProvidedType();
            Class<?> providedType = methodParameters[i];

            if (providedType.isPrimitive()) {
                providedType = Primitives.wrap(providedType);
            }

            if (expectedType.isPresent() && !expectedType.get().isAssignableFrom(providedType)) {
                throw new CommandInitializationException("Cannot assign command argument from "
                        + providedType.getSimpleName() + " to "
                        + expectedType.get().getSimpleName() + "; "
                        + "command method parameter index: " + i);
            }
        }
    }

    private void validateExecutionMethod() {
        checkArgumentCompatibility(executionMethod.getAccessibleObject());
    }

    private void initUsage() {
        StringBuilder builder = new StringBuilder(name.toString());

        for (CommandParameter param : commandParameters) {
            builder.append(' ').append(param.getUsage());
        }

        usage = builder.toString();
    }

    private Object[] processParametersCommand(List<String> rawParameters, EntityRef sender) throws CommandParameterParseException {
        List<String> joinedParameters = joinVarargs(rawParameters);
        Object[] processedParameters = new Object[commandParameters.size()];

        for (int i = 0; i < joinedParameters.size() && i < rawParameters.size(); i++) {
            String rawParam = joinedParameters.get(i);
            CommandParameter param = commandParameters.get(i);
            processedParameters[i] = param.getValue(rawParam);
        }

        return processedParameters;
    }

    private Object[] processParametersMethod(List<String> rawParameters, EntityRef sender) throws CommandParameterParseException {
        List<String> joinedParameters = joinVarargs(rawParameters);
        Object[] processedParameters = new Object[executionMethodParameters.size()];
        int joinedParameterIndex = 0;

        for (int i = 0; i < executionMethodParameters.size(); i++) {
            Parameter parameterType = executionMethodParameters.get(i);

            if (parameterType instanceof CommandParameter && joinedParameterIndex < joinedParameters.size()) {
                CommandParameter parameter = (CommandParameter) parameterType;
                String joinedParameter = joinedParameters.get(joinedParameterIndex++);

                processedParameters[i] = parameter.getValue(joinedParameter);
            } else if (parameterType == MarkerParameters.SENDER) {
                processedParameters[i] = sender;
            }
        }

        return processedParameters;
    }

    private List<String> joinVarargs(List<String> rawParameters) {
        List<String> result = Lists.newArrayList();
        int singleParameterCount = commandParameters.size() + (endsWithVarargs() ? -1 : 0);

        for (int i = 0; i < singleParameterCount && i < rawParameters.size(); i++) {
            result.add(rawParameters.get(i));
        }

        if (endsWithVarargs()) {
            int varargsIndex = commandParameters.size() - 1;

            if (rawParameters.size() > varargsIndex) {
                StringBuilder rawParam = new StringBuilder(rawParameters.get(varargsIndex));

                for (int i = varargsIndex + 1; i < rawParameters.size(); i++) {
                    rawParam.append(CommandParam.ARRAY_DELIMITER_VARARGS).append(rawParameters.get(i));
                }

                result.add(rawParam.toString());
            }
        }

        return result;
    }

    @Override
    public final String execute(List<String> rawParameters, EntityRef sender) throws CommandExecutionException {
        Object[] processedParameters;

        try {
            processedParameters = processParametersMethod(rawParameters, sender);
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
    public final Set<String> suggest(final String currentValue, List<String> rawParameters, EntityRef sender) throws CommandSuggestionException {
        //Generate an array to be used as a parameter in the 'suggest' method
        Object[] processedParametersWithoutSender;

        try {
            processedParametersWithoutSender = processParametersCommand(rawParameters, sender);
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
                suggestedParameter = commandParameters.get(i);
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

        Class<?> requiredClass = suggestedParameter.getType();

        for (Object resultComponent : result) {
            if (resultComponent == null && requiredClass.isPrimitive()) {
                throw new CommandSuggestionException("The 'suggest' method of command class " + getClass().getCanonicalName()
                        + " returns a collection containing an invalid type. Required: " + requiredClass.getCanonicalName()
                        + "; provided: null");
            } else if (resultComponent != null && !requiredClass.isAssignableFrom(resultComponent.getClass())) {
                throw new CommandSuggestionException("The 'suggest' method of command class " + getClass().getCanonicalName()
                        + " returns a collection containing an invalid type. Required: " + requiredClass.getCanonicalName()
                        + "; provided: " + resultComponent.getClass().getCanonicalName());
            }
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
    public ImmutableList<CommandParameter> getCommandParameters() {
        return commandParameters;
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
    public Name getName() {
        return name;
    }

    public int getRequiredParameterCount() {
        return requiredParameterCount;
    }

    public boolean endsWithVarargs() {
        return commandParameters.size() > 0 && commandParameters.get(commandParameters.size() - 1).isVarargs();
    }

    @Override
    public Object getSource() {
        return executionMethod.getTarget();
    }

    @Override
    public int compareTo(ConsoleCommand o) {
        return ConsoleCommand.COMPARATOR.compare(this, o);
    }

    @Override
    public String getRequiredPermission() {
        return requiredPermission;
    }

    public SpecificAccessibleObject<Method> getExecutionMethod() {
        return executionMethod;
    }
}
