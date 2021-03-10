/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.engine.core.module;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.module.BaseModule;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.ResolutionResult;
import org.terasology.module.TableModuleRegistry;
import org.terasology.naming.Name;
import org.terasology.naming.Version;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModuleDownloadListGeneratorTest {

    @Test
    public void testResolverFailed() throws DependencyResolutionFailedException {
        ModuleRegistry localRegistry = buildRegistry("1.0.0", buildSimpleModule("myModule", "1.0.0"));
        DependencyResolver resolver = mockResolver(false);
        ModuleDownloadListGenerator listGenerator = new ModuleDownloadListGenerator(localRegistry, resolver);
        Assertions.assertThrows(DependencyResolutionFailedException.class,
                ()-> buildList(listGenerator));
    }

    @Test
    public void testSingleModuleNoUpdate() throws DependencyResolutionFailedException {
        ModuleRegistry localRegistry = buildRegistry("1.0.0", buildSimpleModule("myModule", "1.0.0"));
        DependencyResolver resolver = mockResolver(true, buildSimpleModule("myModule", "1.0.0"), buildEngineModule("1.0.0"));
        ModuleDownloadListGenerator listGenerator = new ModuleDownloadListGenerator(localRegistry, resolver);
        assertEquals(Collections.emptySet(), buildList(listGenerator));
    }

    @Test
    public void testSingleModuleNeedsUpdate() throws DependencyResolutionFailedException {
        Module moduleV1 = buildSimpleModule("myModule", "1.0.0");
        Module moduleV2 = buildSimpleModule("myModule", "2.0.0");
        ModuleRegistry localRegistry = buildRegistry("1.0.0", moduleV1);
        DependencyResolver resolver = mockResolver(true, moduleV2, buildEngineModule("1.0.0"));
        ModuleDownloadListGenerator listGenerator = new ModuleDownloadListGenerator(localRegistry, resolver);
        assertEquals(Collections.singleton(moduleV2), buildList(listGenerator));
    }

    @Test
    public void testMultipleModulesPartialUpdate() throws DependencyResolutionFailedException {
        Module moduleAV1 = buildSimpleModule("myModuleA", "1.0.0");
        Module moduleBV1 = buildSimpleModule("myModuleB", "1.0.0");
        Module moduleBV2 = buildSimpleModule("myModuleB", "2.0.0");
        ModuleRegistry localRegistry = buildRegistry("1.0.0", moduleAV1, moduleBV1);
        DependencyResolver resolver = mockResolver(true, moduleBV1, moduleBV2, buildEngineModule("1.0.0"));
        ModuleDownloadListGenerator listGenerator = new ModuleDownloadListGenerator(localRegistry, resolver);
        assertEquals(Collections.singleton(moduleBV2), buildList(listGenerator));
    }

    private DependencyResolver mockResolver(boolean success, Module... resolutionResult) {
        DependencyResolver result = mock(DependencyResolver.class);
        DependencyResolver.ResolutionBuilder builder = mock(DependencyResolver.ResolutionBuilder.class);
        when(builder.requireVersion(any(), any())).thenReturn(builder);
        when(builder.requireAll(any(Name[].class))).thenReturn(builder);
        when(builder.build()).thenReturn(new ResolutionResult(success, new HashSet<>(Arrays.asList(resolutionResult))));
        when(result.builder()).thenReturn(builder);
        return result;
    }

    private Set<Module> buildList(ModuleDownloadListGenerator generatorWithMockedResolver) throws DependencyResolutionFailedException {
        return generatorWithMockedResolver.getAllModulesToDownloadFor();
    }

    private Module buildSimpleModule(String id, String version) {
        ModuleMetadata metadata = new ModuleMetadata();
        metadata.setId(new Name(id));
        if (version != null) {
            metadata.setVersion(new Version(version));
        }
        return new BaseModule(Collections.emptyList(), metadata) {
            @Override
            public ImmutableList<URL> getClasspaths() {
                return null;
            }

            @Override
            public boolean isOnClasspath() {
                return false;
            }

            @Override
            public boolean isCodeModule() {
                return false;
            }
        };
    }

    private Module buildEngineModule(String version) {
        return buildSimpleModule(TerasologyConstants.ENGINE_MODULE.toString(), version);
    }

    private ModuleRegistry buildRegistry(String engineVersion, Module... modules) {
        ModuleRegistry result = new TableModuleRegistry();
        result.add(buildEngineModule(engineVersion));
        result.addAll(Arrays.asList(modules));
        return result;
    }
}
