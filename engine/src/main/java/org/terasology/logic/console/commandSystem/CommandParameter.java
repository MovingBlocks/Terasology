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
import com.google.common.primitives.Primitives;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.console.commandSystem.adapter.ParameterAdapterManager;
import org.terasology.logic.console.commandSystem.exceptions.CommandParameterParseException;
import org.terasology.logic.console.commandSystem.exceptions.SuggesterInstantiationException;
import org.terasology.registry.InjectionHelper;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 */
public final class CommandParameter<T> implements Parameter {
    private final String name;
    private final Class<T> type;
    private final CommandParameterSuggester<T> suggester;
    private final boolean required;
    private final ParameterAdapterManager parameterAdapterManager;

    private CommandParameter(String name, Class<T> typeParam, boolean required, CommandParameterSuggester<T> suggester,
                             ParameterAdapterManager parameterAdapterManager) {
        Preconditions.checkNotNull(name, "The parameter name must not be null!");

        if (name.length() <= 0) {
            throw new IllegalArgumentException("The parameter name must not be empty!");
        }

        Preconditions.checkNotNull(typeParam, "The parameter type must not be null!");

        Class<T> resultType;

        if (typeParam.isPrimitive()) {
            if (required) {
                resultType = Primitives.wrap(typeParam);
            } else {
                throw new IllegalArgumentException("An optional parameter must not be primitive!"
                        + " Use " + Primitives.wrap(typeParam).getSimpleName()
                        + " instead of " + typeParam.getSimpleName() + ".");
            }
        } else {
            resultType = typeParam;
        }

        Preconditions.checkNotNull(suggester, "The suggester must not be null!");

        this.name = name;
        this.type = resultType;
        this.suggester = suggester;
        this.required = required;
        this.parameterAdapterManager = parameterAdapterManager;
    }

    public static <T> CommandParameter single(String name, Class<T> type, boolean required,
                                              CommandParameterSuggester<T> suggester,
                                              Context context) {
        if (type.isArray()) {
            throw new IllegalArgumentException("The type of a simple CommandParameterDefinition must not be an array!");
        }
        ParameterAdapterManager parameterAdapterManager = context.get(ParameterAdapterManager.class);
        return new CommandParameter(name, type, required, suggester, parameterAdapterManager);
    }

    public static <T> CommandParameter single(String name, Class<T> type, boolean required,
                                              Class<? extends CommandParameterSuggester<T>> suggesterClass,
                                              Context context)
            throws SuggesterInstantiationException {
        CommandParameterSuggester<T> suggester = optionallyCreateSuggestor(suggesterClass, context);
        return single(name, type, required, suggester, context);
    }

    private static <T> CommandParameterSuggester<T> optionallyCreateSuggestor(
            Class<? extends CommandParameterSuggester<T>> suggestorClass,
            Context context) {
        if (suggestorClass == null) {
            return null;
        }
        return InjectionHelper.createWithConstructorInjection(suggestorClass, context);
    }

    @SuppressWarnings("unchecked")
    public static CommandParameter single(String name, Class<?> type, boolean required,
                                          Context context) {
        return single(name, type, required, (CommandParameterSuggester) null, context);
    }

    public static <T> CommandParameter array(String name, Class<T> childType, Character arrayDelimiter,
                                             boolean required, CommandParameterSuggester<T> suggester,
                                             Context  context) {
        if (childType.isArray()) {
            throw new IllegalArgumentException("The child type of an array CommandParameterDefinition must not be an array!");
        }

        Class<?> type = getArrayClass(childType);
        ParameterAdapterManager parameterAdapterManager = context.get(ParameterAdapterManager.class);
        return new CommandParameter(name, type, required, suggester, parameterAdapterManager);
    }

    public static <T> CommandParameter array(String name, Class<T> childType, Character arrayDelimiter,
                                             boolean required,
                                             Class<? extends CommandParameterSuggester<T>> suggesterClass,
                                             Context context)
            throws SuggesterInstantiationException {
        CommandParameterSuggester<T> suggester = optionallyCreateSuggestor(suggesterClass, context);
        return array(name, childType, arrayDelimiter, required, suggester, context);
    }

    @SuppressWarnings("unchecked")
    public static CommandParameter array(String name, Class<?> childType, Character arrayDelimiter,
                                         boolean required, Context context) {
        return array(name, childType, arrayDelimiter, required, (CommandParameterSuggester) null,
                context);
    }

    public static <T> CommandParameter array(String name, Class<T> childType, boolean required,
                                             CommandParameterSuggester<T> suggester,
                                             Context context) {
        return array(name, childType, null, required, suggester, context);
    }

    public static <T> CommandParameter array(String name, Class<T> childType, boolean required,
                                             Class<? extends CommandParameterSuggester<T>> suggesterClass,
                                             Context context)
            throws SuggesterInstantiationException {
        CommandParameterSuggester<T> suggester = optionallyCreateSuggestor(suggesterClass, context);
        return array(name, childType, required, suggester, context);
    }

    @SuppressWarnings("unchecked")
    public static CommandParameter array(String name, Class<?> childType, boolean required,
                                         Context context) {
        return array(name, childType, required, (CommandParameterSuggester) null, context);
    }

    public static <T> CommandParameter varargs(String name, Class<T> childType, boolean required,
                                               CommandParameterSuggester<T> suggester,
                                               Context context) {
        return array(name, childType, required, suggester, context);
    }

    public static <T> CommandParameter varargs(String name, Class<T> childType, boolean required,
                                               Class<? extends CommandParameterSuggester<T>> suggesterClass,
                                               Context context)
            throws SuggesterInstantiationException {
        return varargs(name, childType, required, optionallyCreateSuggestor(suggesterClass, context), context);
    }

    @SuppressWarnings("unchecked")
    public static CommandParameter varargs(String name, Class<?> childType, boolean required,
                                           Context context) {
        return varargs(name, childType, required, (CommandParameterSuggester) null, context);
    }

    /**
     * @param clazz The child class of the array class returned
     * @return The array class of {@code clazz}
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T[]> getArrayClass(Class<T> clazz) {
        return (Class<? extends T[]>) Array.newInstance(clazz, 0).getClass();
    }

    public Object getValue(String param) throws CommandParameterParseException {
        if (!isArray()) {
            return parse(param);
        } else {
            return getArrayValue(Arrays.asList(param));
        }
    }

    public Object getArrayValue(List<String> params) throws CommandParameterParseException {
        Object arrayInstance = Array.newInstance(getType(), params.size());
        for (int i = 0; i < params.size(); ++i) {
            Array.set(arrayInstance, i, parse(params.get(i)));
        }
        return arrayInstance;
    }

    private Object parse(String string) throws CommandParameterParseException {
        Class<?> childType = getTypeNotPrimitive();

        if (parameterAdapterManager.isAdapterRegistered(childType)) {
            try {
                return parameterAdapterManager.parse(childType, string);
            } catch (Error | Exception e) {
                throw new CommandParameterParseException("An error occurred while parsing " + getType().getCanonicalName(), e, string);
            }
        }

        throw new CommandParameterParseException("Cannot parse a " + childType.getCanonicalName(), string);
    }

    public String convertToString(Object object) {
        return parameterAdapterManager.convertToString(object, (Class<? super Object>) getType());
    }

    public Set<T> suggest(EntityRef sender, Object... parameters) {
        return suggester.suggest(sender, parameters);
    }

    public String getUsage() {
        String simpleTypeName = getType().getSimpleName();
        StringBuilder usage = new StringBuilder(simpleTypeName);

        if (hasName()) {
            usage.append(' ').append(getName());
        }

        if (isArray()) {
            usage.insert(0, '(');
            usage.append("...)");
        } else if (isRequired()) {
            usage.insert(0, '<');
            usage.append('>');
        } else {
            usage.insert(0, '(');
            usage.append(')');
        }

        return usage.toString();
    }

    public boolean isArray() {
        return type.isArray();
    }

    public boolean hasName() {
        return name.length() >= 0;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        if (type.isArray()) {
            return type.getComponentType();
        }

        return type;
    }

    public Class<?> getTypeNotPrimitive() {
        Class<?> componentType = getType();

        if (componentType.isPrimitive()) {
            return Primitives.wrap(componentType);
        } else {
            return componentType;
        }
    }

    public Class<T> getTypeRaw() {
        return type;
    }

    public CommandParameterSuggester<T> getSuggester() {
        return suggester;
    }

    public boolean isRequired() {
        return required && !isArray();
    }

    @Override
    public Optional<? extends Class<?>> getProvidedType() {
        return Optional.of(getTypeRaw());
    }
}
