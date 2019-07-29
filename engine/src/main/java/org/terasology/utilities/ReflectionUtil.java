/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.utilities;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.reflections.ReflectionUtils;
import org.terasology.engine.SimpleUri;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.Name;
import org.terasology.reflection.TypeInfo;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.engine.SimpleUri;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.Name;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public final class ReflectionUtil {
    private ReflectionUtil() {
    }

    /**
     * Returns true if {@link Type} {@code a} and {@code b} are equal.
     */
    public static boolean typeEquals(Type a, Type b) {
        if (a == b) {
            // also handles (a == null && b == null)
            return true;

        } else if (a instanceof Class) {
            // Class already specifies equals().
            return a.equals(b);

        } else if (a instanceof ParameterizedType) {
            if (!(b instanceof ParameterizedType)) {
                return false;
            }

            // TODO: save a .clone() call
            ParameterizedType pa = (ParameterizedType) a;
            ParameterizedType pb = (ParameterizedType) b;
            return Objects.equals(pa.getOwnerType(), pb.getOwnerType())
                       && pa.getRawType().equals(pb.getRawType())
                       && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());

        } else if (a instanceof GenericArrayType) {
            if (!(b instanceof GenericArrayType)) {
                return false;
            }

            GenericArrayType ga = (GenericArrayType) a;
            GenericArrayType gb = (GenericArrayType) b;
            return typeEquals(ga.getGenericComponentType(), gb.getGenericComponentType());

        } else if (a instanceof WildcardType) {
            if (!(b instanceof WildcardType)) {
                return false;
            }

            WildcardType wa = (WildcardType) a;
            WildcardType wb = (WildcardType) b;
            return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
                       && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

        } else if (a instanceof TypeVariable) {
            if (!(b instanceof TypeVariable)) {
                return false;
            }
            TypeVariable<?> va = (TypeVariable<?>) a;
            TypeVariable<?> vb = (TypeVariable<?>) b;
            return va.getGenericDeclaration() == vb.getGenericDeclaration()
                       && va.getName().equals(vb.getName());

        } else {
            // This isn't a type we support. Could be a generic array type, wildcard type, etc.
            return false;
        }
    }


    /**
     * Attempts to return the type of a parameter of a parameterised field. This uses compile-time information only - the
     * type should be obtained from a field with a the generic types bound.
     *
     * @return The type of the generic parameter at index for the given type, or null if it cannot be obtained.
     */
    // TODO - Improve parameter lookup to go up the inheritance tree more
    public static Type getTypeParameter(Type type, int index) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (index >= parameterizedType.getActualTypeArguments().length) {
            return null;
        }
        return parameterizedType.getActualTypeArguments()[index];
    }

    public static Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return Array.newInstance(getRawType(genericArrayType.getGenericComponentType()), 0).getClass();
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            return getRawType(wildcardType.getUpperBounds()[0]);
        }
        return Object.class;
    }

    public static Method findGetter(Field field) {
        return findGetter(field.getName(), field.getDeclaringClass(), field.getType());
    }

    public static Method findGetter(String propertyName, Class<?> beanClass, Class<?> propertyType) {
        Method result = findGetter(propertyName, beanClass);
        if (result != null && propertyType.equals(result.getReturnType())) {
            return result;
        }
        return null;
    }

    public static Method findGetter(String propertyName, Class<?> beanClass) {
        Method result = findMethod(beanClass, "get" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1));
        if (result != null) {
            result.setAccessible(true);
            return result;
        }
        result = findMethod(beanClass, "is" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1));
        if (result != null) {
            result.setAccessible(true);
            return result;
        }
        return null;
    }

    public static Method findSetter(Field field) {
        return findSetter(field.getName(), field.getDeclaringClass(), field.getType());
    }

    public static Method findSetter(String propertyName, Class<?> beanClass, Class<?> propertyType) {
        String setterName = "set" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
        Method result = findMethod(beanClass, setterName, propertyType);
        if (result != null) {
            result.setAccessible(true);
        }
        return result;
    }

    public static Method findMethod(Class<?> targetType, String methodName, Class<?>... parameters) {
        try {
            return targetType.getMethod(methodName, parameters);
        } catch (NoSuchMethodException me) {
            // We're expecting not to find methods
            return null;
        }
    }

    /**
     * Returns an ordered list of super classes and interfaces for the given class, that have a common base class.
     * The set is ordered with the deepest interface first, through all the interfaces, and then all the super classes.
     *
     * @return an ordered list of super classes and interfaces for the given class, that have a common base class.
     */
    public static <T> List<Class<? extends T>> getInheritanceTree(Class<? extends T> forClass, Class<T> baseClass) {
        Set<Class<? extends T>> result = Sets.newLinkedHashSet();
        for (Class<?> interfaceType : forClass.getInterfaces()) {
            if (baseClass.isAssignableFrom(interfaceType)) {
                addInterfaceToInheritanceTree((Class<? extends T>) interfaceType, baseClass, result);
            }
        }
        addClassToInheritanceTree(forClass, baseClass, result);
        return Lists.newArrayList(result);
    }

    private static <T> void addClassToInheritanceTree(Class<? extends T> element, Class<T> baseClass, Set<Class<? extends T>> result) {
        for (Class<?> interfaceType : element.getInterfaces()) {
            if (baseClass.isAssignableFrom(interfaceType)) {
                addInterfaceToInheritanceTree((Class<? extends T>) interfaceType, baseClass, result);
            }
        }
        if (element.getSuperclass() != null && baseClass.isAssignableFrom(element.getSuperclass())) {
            addClassToInheritanceTree((Class<? extends T>) element.getSuperclass(), baseClass, result);
        }
        result.add(element);
    }

    private static <T> void addInterfaceToInheritanceTree(Class<? extends T> interfaceType, Class<T> baseClass, Set<Class<? extends T>> result) {
        for (Class<?> parentInterface : interfaceType.getInterfaces()) {
            if (UIWidget.class.isAssignableFrom(parentInterface)) {
                addInterfaceToInheritanceTree((Class<? extends T>) parentInterface, baseClass, result);
            }
        }
        result.add(interfaceType);
    }

    public static <T> Type getTypeParameterForSuper(Type target, Class<T> superClass, int index) {
        Class targetClass = getRawType(target);
        Preconditions.checkArgument(superClass.isAssignableFrom(targetClass), "Target must be a child of superClass");

        if (superClass.isInterface()) {
            return getTypeParameterForSuperInterface(target, superClass, index);
        } else {
            return getTypeParameterForSuperClass(target, superClass, index);
        }
    }

    /**
     * Returns the {@link TypeInfo} describing the component type of an array of the given type.
     *
     * @param type The {@link TypeInfo} describing the type of the array.
     * @param <C>  The component type of the array.
     */
    @SuppressWarnings({"unchecked"})
    public static <C> TypeInfo<C> getComponentType(TypeInfo<C[]> type) {
        if (type.getType() instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type.getType();

            Type componentType = arrayType.getGenericComponentType();
            Type resolvedComponentType = resolveType(arrayType, componentType);

            return (TypeInfo<C>) TypeInfo.of(resolvedComponentType);
        }

        return TypeInfo.of((Class<C>) type.getRawType().getComponentType());
    }

    /**
     * Returns the {@link TypeInfo} describing the element type of a {@link Collection}
     * of the given type.
     *
     * @param type The {@link TypeInfo} describing the type of the {@link Collection}.
     * @param <E>  The element type of the {@link Collection}.
     */
    @SuppressWarnings({"unchecked"})
    public static <E> TypeInfo<E> getElementType(TypeInfo<? extends Collection<E>> type) {
        Type elementType = getTypeParameterForSuper(type.getType(), Collection.class, 0);

        return (TypeInfo<E>) TypeInfo.of(elementType);
    }

    private static <T> Type getTypeParameterForSuperClass(Type target, Class<T> superClass, int index) {
        for (Class targetClass = getRawType(target);
             !Object.class.equals(targetClass);
             target = resolveType(target, targetClass.getGenericSuperclass()),
                 targetClass = getRawType(target)) {
            if (superClass.equals(targetClass)) {
                return getTypeParameter(target, index);
            }
        }

        return null;
    }

    private static <T> Type getTypeParameterForSuperInterface(Type target, Class<T> superClass, int index) {
        Class targetClass = getRawType(target);

        if (Object.class.equals(targetClass)) {
            return null;
        }

        if (targetClass.equals(superClass)) {
            return getTypeParameter(target, index);
        }

        Type genericSuperclass = resolveType(target, targetClass.getGenericSuperclass());

        if (!Object.class.equals(genericSuperclass) && genericSuperclass != null) {
            Type fromSuperClass = getTypeParameterForSuperInterface(genericSuperclass, superClass, index);

            if (fromSuperClass != null) {
                return fromSuperClass;
            }
        }

        for (Type genericInterface : targetClass.getGenericInterfaces()) {
            genericInterface = resolveType(target, genericInterface);

            Type typeParameter = getTypeParameterForSuperInterface(genericInterface, superClass, index);

            if (typeParameter != null) {
                return typeParameter;
            }
        }

        return null;
    }

    /**
     * Resolves all {@link TypeVariable}s in {@code type} to concrete types as per the type
     * parameter definitions in {@code contextType}. All {@link TypeVariable}s in {@code type}
     * should have been declared in {@code contextType} or one of its supertypes, otherwise those
     * variables will be resolved to {@link Object Object.class}.
     *
     * @param contextType The {@link Type} which contains all type parameter definitions used in {@code type}.
     * @param type        The {@link Type} whose {@link TypeVariable}s are to be resolved.
     * @return A copy of {@code type} with all {@link TypeVariable}s resolved.
     */
    public static Type resolveType(Type contextType, Type type) {
        Class<?> contextClass = getRawType(contextType);

        // T field;
        if (type instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;

            Type resolvedTypeVariable = resolveTypeVariable(contextType, typeVariable, contextClass);

            if (resolvedTypeVariable == typeVariable) {
                return typeVariable;
            }

            if (resolvedTypeVariable == null) {
                // TypeVariable not specified (i.e. raw type), return Object
                return Object.class;
            }

            return resolveType(contextType, resolvedTypeVariable);
        }

        // List<T> field;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            Type ownerType = parameterizedType.getOwnerType();
            Type resolvedOwnerType = resolveType(contextType, ownerType);

            boolean changed = resolvedOwnerType != ownerType;

            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            Type[] resolvedTypeArguments = resolveTypes(contextType, typeArguments);

            changed |= resolvedTypeArguments != typeArguments;

            if (!changed) {
                return parameterizedType;
            }

            final Type rawType = parameterizedType.getRawType();

            return parameterizedTypeOf(resolvedOwnerType, resolvedTypeArguments, rawType);
        }

        // T[] field || List<T>[] field;
        if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;

            Type componentType = arrayType.getGenericComponentType();
            Type resolvedComponentType = resolveType(contextType, componentType);

            if (resolvedComponentType == componentType) {
                return type;
            } else {
                return new GenericArrayTypeImpl(resolvedComponentType);
            }
        }

        // List<? extends T> field;
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            Type[] lowerBounds = wildcardType.getLowerBounds();
            Type[] upperBounds = wildcardType.getUpperBounds();

            boolean changed = false;

            // Technically not required as language supports only one bound, but generalizing
            Type[] resolvedLowerBounds = resolveTypes(contextType, lowerBounds);
            changed |= resolvedLowerBounds != lowerBounds;

            Type[] resolvedUpperBounds = resolveTypes(contextType, upperBounds);
            changed |= resolvedUpperBounds != upperBounds;

            if (!changed) {
                return wildcardType;
            }

            return new WildcardTypeImpl(resolvedUpperBounds, resolvedLowerBounds);
        }

        return type;
    }

    private static Type[] resolveTypes(Type contextType, Type[] types) {
        Type[] resolvedTypes = new Type[types.length];

        for (int i = 0; i < types.length; i++) {
            resolvedTypes[i] = resolveType(contextType, types[i]);
        }

        return resolvedTypes;
    }

    private static Type resolveTypeVariable(Type contextType, TypeVariable<?> typeVariable, Class<?> contextClass) {
        if (!(typeVariable.getGenericDeclaration() instanceof Class)) {
            // We cannot resolve type variables declared by a method, quit
            return typeVariable;
        }

        return getCascadedGenericDeclaration(typeVariable)
                   .filter(declaration ->
                               declaration.getKey().isAssignableFrom(contextClass))
                   .findAny()
                   .map(declaration ->
                            getTypeParameterForSuper(contextType,
                                declaration.getKey(),
                                declaration.getValue())
                   )
                   // If we couldn't find a declaration in the context, we will not be
                   // able to resolve this type variable, resort to Object.class
                   .orElse(Object.class);
    }

    public static Stream<Type> getGenericSupertypes(Class<?> clazz) {
        return Stream.concat(Stream.of(clazz.getGenericSuperclass()), Stream.of(clazz.getGenericInterfaces()));
    }

    /**
     * Cascades the declaration of the type variable up the inheritance tree and returns the
     * cascaded declaration classes and the corresponding index of the type variable for that
     * declaration class.
     */
    private static Stream<Map.Entry<Class<?>, Integer>> getCascadedGenericDeclaration(TypeVariable<?> typeVariable) {
        assert typeVariable.getGenericDeclaration() instanceof Class<?>;

        Class<?> genericDeclaration = (Class<?>) typeVariable.getGenericDeclaration();

        int typeVariableIndex = Arrays.asList(genericDeclaration.getTypeParameters())
                                    .indexOf(typeVariable);

        return cascadeTypeVariableDeclarationToSupertypes(typeVariableIndex, genericDeclaration);
    }

    private static Stream<Map.Entry<Class<?>, Integer>> cascadeTypeVariableDeclarationToSupertypes(
        int typeVariableIndex, Class<?> declaration) {
        TypeVariable<?> typeVariable = declaration.getTypeParameters()[typeVariableIndex];

        return Stream.concat(
            Stream.of(new AbstractMap.SimpleEntry<>(declaration, typeVariableIndex)),
            getGenericSupertypes(declaration)
                .filter(supertype -> supertype instanceof ParameterizedType)
                .map(supertype -> (ParameterizedType) supertype)
                .flatMap(supertype -> {
                        int superTypeVariableIndex =
                            Arrays.asList(supertype.getActualTypeArguments()).indexOf(typeVariable);

                        if (superTypeVariableIndex == -1) {
                            return Stream.empty();
                        }

                        return cascadeTypeVariableDeclarationToSupertypes(
                            superTypeVariableIndex,
                            getRawType(supertype)
                        );
                    }
                )
        );
    }

    public static Object readField(Object object, String fieldName) {
        Class<?> cls = object.getClass();
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            try {
                final Field field = c.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(object);
            } catch (final NoSuchFieldException e) {
                // Try parent
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    "Cannot access field " + cls.getName() + "." + fieldName, e);
            }
        }
        throw new IllegalArgumentException(
            "Cannot find field " + cls.getName() + "." + fieldName);
    }

    public static ParameterizedTypeImpl parameterizedTypeOf(Type ownerType, Type[] actualTypeArguments, Type rawType) {
        return new ParameterizedTypeImpl(rawType, actualTypeArguments, ownerType);
    }

    /**
     * Returns a parameterized version of the given raw type, if it has generic type parameters.
     * If it has no generic type parameters, the raw type itself is returned.
     */
    public static Type parameterizeRawType(Class<?> rawType) {
        if (rawType == null) {
            return null;
        }

        TypeVariable<?>[] typeParameters = rawType.getTypeParameters();

        if (typeParameters.length == 0) {
            return rawType;
        }

        return parameterizedTypeOf(
            parameterizeRawType(rawType.getEnclosingClass()),
            typeParameters,
            rawType
        );
    }

    /**
     * Returns a parameterized version of the given raw type that has been resolved with the given
     * context type, if it has generic type parameters. If it has no generic type parameters,
     * the raw type itself is returned.
     */
    // TODO: Rename
    public static Type parameterizeandResolveRawType(Type contextType, Class<?> rawType) {
        Type parameterizedType = parameterizeRawType(rawType);
        return resolveType(contextType, parameterizedType);
    }

    public static <T> Set<Class<? extends T>> loadClasses(Iterable<String> subTypes, ClassLoader[] classLoaders) {
        return Lists.newArrayList(subTypes).parallelStream()
                   .map(subtypeName -> (Class<? extends T>) ReflectionUtils.forName(subtypeName, classLoaders))
                   .filter(Objects::nonNull)
                   .collect(Collectors.toSet());
    }

    /**
     * Returns a {@link String} that describes the given {@link Executable}. All generic
     * parameters and return types of the method are first resolved with respect to the
     * {@code declaringType} before being converted to a {@link String}
     *
     * @param declaringType The {@link Type} declaring the given method.
     * @param method        The {@link Executable} describing the given method.
     * @param useSimpleName Whether to use simple names of types or not.
     */
    public static String resolvedMethodToString(Type declaringType,
                                                Executable method,
                                                boolean useSimpleName) {
        try {
            StringBuilder stringBuilder = new StringBuilder();

            TypeVariable[] typeParameters = method.getTypeParameters();
            if (typeParameters.length > 0) {
                boolean first = true;
                stringBuilder.append('<');

                for (TypeVariable parameter : typeParameters) {
                    if (!first) {
                        stringBuilder.append(',');
                    }

                    stringBuilder.append(parameter.toString());
                    first = false;
                }

                stringBuilder.append("> ");
            }

            if (method instanceof Method) {
                Type returnType = resolveType(declaringType, ((Method) method).getGenericReturnType());

                stringBuilder.append(typeToString(returnType, useSimpleName))
                    .append(' ');
                stringBuilder.append(method.getName());
            } else {
                final Class<?> declaringClass = method.getDeclaringClass();
                stringBuilder.append(typeToString(declaringClass, useSimpleName));
            }

            stringBuilder.append('(');
            Type[] unresolvedParameterTypes = method.getGenericParameterTypes();

            for (int i = 0; i < unresolvedParameterTypes.length; ++i) {
                final Type parameterType = resolveType(declaringType, unresolvedParameterTypes[i]);
                String parameterName = typeToString(parameterType, useSimpleName);

                if (method.isVarArgs() && i == unresolvedParameterTypes.length - 1) {
                    parameterName = parameterName.replaceFirst("\\[\\]$", "...");
                }

                stringBuilder.append(parameterName);
                if (i < unresolvedParameterTypes.length - 1) {
                    stringBuilder.append(',');
                }
            }

            stringBuilder.append(')');

            return stringBuilder.toString();
        } catch (Exception var10) {
            return "<" + var10 + ">";
        }
    }

    public static String typeToString(Type type) {
        return typeToString(type, false);
    }

    public static String typeToString(Type type, boolean useSimpleName) {
        if (type instanceof Class) {
            final Class clazz = (Class) type;

            if (useSimpleName) {
                return clazz.getSimpleName();
            }

            return clazz.getTypeName();
        }

        if (type instanceof WildcardType) {
            return WildcardTypeImpl.toString((WildcardType) type, useSimpleName);
        }

        if (type instanceof ParameterizedType) {
            return ParameterizedTypeImpl.toString((ParameterizedType) type, useSimpleName);
        }

        if (type instanceof GenericArrayType) {
            return GenericArrayTypeImpl.toString((GenericArrayType) type, useSimpleName);
        }

        return null;
    }

    public static SimpleUri simpleUriOfType(Type type, ModuleEnvironment moduleEnvironment) {
        Name moduleProvidingType = moduleEnvironment.getModuleProviding(getRawType(type));
        String typeSimpleName = typeToString(type, true);

        return new SimpleUri(moduleProvidingType, typeSimpleName);
    }

    /**
     * Returns the {@link SimpleUri} for a type belonging to the {@link ModuleEnvironment}.
     * If the type does not belong to the module environment, null is returned.
     */
    public static SimpleUri getSimpleUriFor(Type type, ModuleEnvironment environment) {
        Class<?> clazz = getRawType(type);

        if (clazz.getClassLoader() == null) {
            // Loaded with the bootstrap class loader, definitely not part of a module
            return null;
        }

        Name moduleProviding = environment.getModuleProviding(clazz);

        if (moduleProviding == null) {
            return null;
        }

        return new SimpleUri(moduleProviding, clazz.getSimpleName());
    }

    /**
     * Returns the fully qualified {@link SimpleUri} for a type belonging to the {@link ModuleEnvironment}.
     * If the type does not belong to the module environment, null is returned.
     */
    public static SimpleUri getFullyQualifiedSimpleUriFor(Type type, ModuleEnvironment environment) {
        Class<?> clazz = getRawType(type);

        if (clazz.getClassLoader() == null) {
            // Loaded with the bootstrap class loader, definitely not part of a module
            return null;
        }

        Name moduleProviding = environment.getModuleProviding(clazz);

        if (moduleProviding == null) {
            return null;
        }

        return new SimpleUri(moduleProviding, clazz.getTypeName());
    }


    private static class WildcardTypeImpl implements WildcardType {
        private final Type[] upperBounds;
        private final Type[] lowerBounds;

        public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            this.upperBounds = upperBounds;
            this.lowerBounds = lowerBounds;
        }

        public static String toString(WildcardType wildcardType) {
            return toString(wildcardType, false);
        }

        public static String toString(WildcardType wildcardType, boolean useSimpleName) {
            Type[] lowerBounds = wildcardType.getLowerBounds();
            Type[] bounds = lowerBounds;

            StringBuilder stringBuilder = new StringBuilder();

            if (lowerBounds.length > 0) {
                stringBuilder.append("? super ");
            } else {
                Type[] upperBounds = wildcardType.getUpperBounds();
                if (upperBounds.length <= 0 || upperBounds[0].equals(Object.class)) {
                    return "?";
                }

                bounds = upperBounds;
                stringBuilder.append("? extends ");
            }

            boolean isFirstBound = true;

            for (Type bound : bounds) {
                if (!isFirstBound) {
                    stringBuilder.append(" & ");
                }

                isFirstBound = false;
                stringBuilder.append(typeToString(bound, useSimpleName));
            }

            return stringBuilder.toString();
        }

        @Override
        public Type[] getUpperBounds() {
            return upperBounds;
        }

        @Override
        public Type[] getLowerBounds() {
            return lowerBounds;
        }

        public String toString() {
            return toString(this);
        }

        public boolean equals(Object var1) {
            if (!(var1 instanceof WildcardType)) {
                return false;
            } else {
                WildcardType var2 = (WildcardType) var1;
                return Arrays.equals(this.getLowerBounds(), var2.getLowerBounds()) && Arrays.equals(this.getUpperBounds(), var2.getUpperBounds());
            }
        }

        public int hashCode() {
            Type[] var1 = this.getLowerBounds();
            Type[] var2 = this.getUpperBounds();
            return Arrays.hashCode(var1) ^ Arrays.hashCode(var2);
        }
    }

    private static class GenericArrayTypeImpl implements GenericArrayType {
        private final Type genericComponentType;

        private GenericArrayTypeImpl(Type genericComponentType) {
            this.genericComponentType = genericComponentType;
        }

        public static String toString(GenericArrayType type) {
            return toString(type, false);
        }

        public static String toString(GenericArrayType type, boolean useSimpleName) {
            Type genericComponentType = type.getGenericComponentType();

            return typeToString(genericComponentType, useSimpleName) + "[]";
        }

        public Type getGenericComponentType() {
            return this.genericComponentType;
        }

        public String toString() {
            return toString(this);
        }

        public boolean equals(Object var1) {
            if (var1 instanceof GenericArrayType) {
                GenericArrayType var2 = (GenericArrayType) var1;
                return Objects.equals(this.genericComponentType, var2.getGenericComponentType());
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hashCode(this.genericComponentType);
        }
    }

    private static class ParameterizedTypeImpl implements ParameterizedType {
        private final Type[] actualTypeArguments;
        private final Class<?> rawType;
        private final Type ownerType;

        private ParameterizedTypeImpl(Type rawType, Type[] actualTypeArguments, Type ownerType) {
            this.actualTypeArguments = actualTypeArguments;
            this.rawType = (Class<?>) rawType;
            this.ownerType = ownerType != null ? ownerType : this.rawType.getDeclaringClass();
        }

        public static String toString(ParameterizedType type) {
            return toString(type, false);
        }

        public static String toString(ParameterizedType type, boolean useSimpleName) {
            StringBuilder var1 = new StringBuilder();
            if (type.getOwnerType() != null) {
                var1.append(typeToString(type.getOwnerType(), useSimpleName));

                var1.append("$");
                if (type.getOwnerType() instanceof ParameterizedTypeImpl) {
                    final String charSequence = typeToString(((ParameterizedTypeImpl) type.getOwnerType()).rawType, useSimpleName) + "$";
                    final String rawTypeName = typeToString(type.getRawType(), useSimpleName);
                    var1.append(rawTypeName.replace(charSequence, ""));
                } else {
                    var1.append(typeToString(type.getRawType(), true));
                }
            } else {
                var1.append(typeToString(type.getRawType(), useSimpleName));
            }

            if (type.getActualTypeArguments() != null && type.getActualTypeArguments().length > 0) {
                var1.append("<");
                boolean var2 = true;
                Type[] var3 = type.getActualTypeArguments();
                int var4 = var3.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    Type var6 = var3[var5];
                    if (!var2) {
                        var1.append(", ");
                    }

                    var1.append(typeToString(var6, useSimpleName));
                    var2 = false;
                }

                var1.append(">");
            }

            return var1.toString();
        }

        public Type[] getActualTypeArguments() {
            return this.actualTypeArguments.clone();
        }

        public Class<?> getRawType() {
            return this.rawType;
        }

        public Type getOwnerType() {
            return this.ownerType;
        }

        public boolean equals(Object other) {
            if (!(other instanceof ParameterizedType)) {
                return false;
            }

            ParameterizedType otherParameterizedType = (ParameterizedType) other;

            if (this == otherParameterizedType) {
                return true;
            }

            return Objects.equals(this.ownerType, otherParameterizedType.getOwnerType()) &&
                       Objects.equals(this.rawType, otherParameterizedType.getRawType()) &&
                       Arrays.equals(this.actualTypeArguments, otherParameterizedType.getActualTypeArguments());
        }

        public int hashCode() {
            return Arrays.hashCode(this.actualTypeArguments) ^ Objects.hashCode(this.ownerType) ^ Objects.hashCode(this.rawType);
        }

        public String toString() {
            return toString(this);
        }
    }
}
