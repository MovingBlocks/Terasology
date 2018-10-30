/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.terasology.math.AABB;
import org.terasology.math.Region3i;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ModulePathScanner;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.TableModuleRegistry;
import org.terasology.module.sandbox.ModuleSecurityManager;
import org.terasology.module.sandbox.ModuleSecurityPolicy;
import org.terasology.module.sandbox.PermissionSet;
import org.terasology.module.sandbox.StandardPermissionProviderFactory;
import org.terasology.naming.Name;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerFactoryContext;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.inMemory.PersistedInteger;
import org.terasology.persistence.typeHandling.inMemory.PersistedMap;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;
import org.terasology.reflection.TypeInfo;
import org.terasology.utilities.ReflectionUtil;

import java.io.FilePermission;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Policy;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RuntimeDelegatingTypeHandlerSandboxTest {
    private final TypeSerializationLibrary typeSerializationLibrary = mock(TypeSerializationLibrary.class);
    private final Class<?> apiClass = AABB.class;
    private final Class<?> nonApiClass = Region3i.class;

    private ModuleEnvironment moduleEnvironment;
    private Class<?> moduleClass;

    private TypeHandler baseTypeHandlerMock;
    private TypeHandler moduleClassHandlerMock;
    private TypeHandler apiClassHandlerMock;

    @Before
    public void setup() {
        ModuleRegistry registry = new TableModuleRegistry();
        Path modulesPath = Paths.get("engine-tests", "test-modules").toAbsolutePath();
        new ModulePathScanner().scan(registry, modulesPath);

        StandardPermissionProviderFactory permissionProviderFactory = new StandardPermissionProviderFactory();
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("sun.reflect");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.lang");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.util");
        permissionProviderFactory.getBasePermissionSet().addAPIClass(apiClass);
        PermissionSet ioPermissionSet = new PermissionSet();
        ioPermissionSet.addAPIPackage("java.io");
        ioPermissionSet.addAPIPackage("java.nio.file");
        ioPermissionSet.addAPIPackage("java.nio.file.attribute");
        ioPermissionSet.grantPermission(FilePermission.class);
        permissionProviderFactory.addPermissionSet("io", ioPermissionSet);

        Policy.setPolicy(new ModuleSecurityPolicy());
        System.setSecurityManager(new ModuleSecurityManager());

        DependencyResolver resolver = new DependencyResolver(registry);
        moduleEnvironment = new ModuleEnvironment(resolver.resolve(new Name("EmptyClassModule")).getModules(), permissionProviderFactory, Collections.emptyList());

        moduleClass = moduleEnvironment.getSubtypesOf(AutoCloseable.class).iterator().next();

        baseTypeHandlerMock = mock(TypeHandler.class);
        moduleClassHandlerMock = mock(TypeHandler.class);
        apiClassHandlerMock = mock(TypeHandler.class);

        when(baseTypeHandlerMock.deserialize(any()))
                .thenReturn(Optional.of(new PersistedInteger(0)));

        when(moduleClassHandlerMock.deserialize(any()))
                .thenReturn(Optional.of(new PersistedInteger(0)));

        when(apiClassHandlerMock.deserialize(any()))
                .thenReturn(Optional.of(new PersistedInteger(0)));

        when(typeSerializationLibrary.getTypeHandler(eq(Object.class), any()))
                .thenReturn(Optional.of(baseTypeHandlerMock));

        when(typeSerializationLibrary.getTypeHandler(eq(moduleClass), any()))
                .thenReturn(Optional.of(moduleClassHandlerMock));

        when(typeSerializationLibrary.getTypeHandler(eq(apiClass), any()))
                .thenReturn(Optional.of(apiClassHandlerMock));
    }

    @Test
    public void testAccessModuleClassFromEngine() {
        TypeHandlerFactoryContext context = new TypeHandlerFactoryContext(typeSerializationLibrary,
                ReflectionUtil.getComprehensiveEngineClassLoaders(moduleEnvironment));

        RuntimeDelegatingTypeHandler<?> typeHandler = new RuntimeDelegatingTypeHandler<>(mock(TypeHandler.class), TypeInfo.of(AutoCloseable.class), context);

        PersistedData persistedModuleClassInstance = new PersistedMap(
                ImmutableMap.of(
                        RuntimeDelegatingTypeHandler.TYPE_FIELD,
                        new PersistedString(moduleClass.getName()),
                        RuntimeDelegatingTypeHandler.VALUE_FIELD,
                        new PersistedMap(ImmutableMap.of())
                )
        );

        assertTrue(typeHandler.deserialize(persistedModuleClassInstance).isPresent());

        verify(baseTypeHandlerMock, times(0)).deserialize(any());
        verify(moduleClassHandlerMock, times(1)).deserialize(any());
    }

    @Test
    public void testCannotAccessModuleClassFromEngineWithoutClassLoader() {
        TypeHandlerFactoryContext context = new TypeHandlerFactoryContext(typeSerializationLibrary,
                ReflectionUtil.class.getClassLoader());

        RuntimeDelegatingTypeHandler<?> typeHandler = new RuntimeDelegatingTypeHandler<>(mock(TypeHandler.class), TypeInfo.of(AutoCloseable.class), context);

        PersistedData persistedModuleClassInstance = new PersistedMap(
                ImmutableMap.of(
                        RuntimeDelegatingTypeHandler.TYPE_FIELD,
                        new PersistedString(moduleClass.getName()),
                        RuntimeDelegatingTypeHandler.VALUE_FIELD,
                        new PersistedMap(ImmutableMap.of())
                )
        );

        assertFalse(typeHandler.deserialize(persistedModuleClassInstance).isPresent());

        verify(baseTypeHandlerMock, times(0)).deserialize(any());
        verify(moduleClassHandlerMock, times(0)).deserialize(any());
    }

    @Test
    public void testAccessApiClassFromModule() {
        TypeHandlerFactoryContext context = new TypeHandlerFactoryContext(typeSerializationLibrary,
                moduleClass.getClassLoader());

        RuntimeDelegatingTypeHandler<?> typeHandler = new RuntimeDelegatingTypeHandler<>(mock(TypeHandler.class), TypeInfo.of(Object.class), context);

        PersistedData persistedModuleClassInstance = new PersistedMap(
                ImmutableMap.of(
                        RuntimeDelegatingTypeHandler.TYPE_FIELD,
                        new PersistedString(apiClass.getName()),
                        RuntimeDelegatingTypeHandler.VALUE_FIELD,
                        new PersistedMap(ImmutableMap.of())
                )
        );

        assertTrue(typeHandler.deserialize(persistedModuleClassInstance).isPresent());

        verify(baseTypeHandlerMock, times(0)).deserialize(any());
        verify(apiClassHandlerMock, times(1)).deserialize(any());
    }

    @Test
    public void testCannotAccessNonApiClassFromModule() {
        TypeHandlerFactoryContext context = new TypeHandlerFactoryContext(typeSerializationLibrary,
                moduleClass.getClassLoader());

        RuntimeDelegatingTypeHandler<?> typeHandler = new RuntimeDelegatingTypeHandler<>(mock(TypeHandler.class), TypeInfo.of(Object.class), context);

        PersistedData persistedModuleClassInstance = new PersistedMap(
                ImmutableMap.of(
                        RuntimeDelegatingTypeHandler.TYPE_FIELD,
                        new PersistedString(nonApiClass.getName()),
                        RuntimeDelegatingTypeHandler.VALUE_FIELD,
                        new PersistedMap(ImmutableMap.of())
                )
        );

        assertFalse(typeHandler.deserialize(persistedModuleClassInstance).isPresent());

        verify(baseTypeHandlerMock, times(0)).deserialize(any());
        verify(apiClassHandlerMock, times(0)).deserialize(any());
    }
}
