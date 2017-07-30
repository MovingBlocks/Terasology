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
package org.terasology.engine.module;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.terasology.engine.TerasologyConstants;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModuleDownloadListGeneratorTest {

    @Test(expected = DependencyResolutionFailedException.class)
    public void testResolverFailed() throws DependencyResolutionFailedException {
        ModuleRegistry localRegistry = buildRegistry("1.0.0", buildSimpleModule("myModule", "1.0.0"));
        DependencyResolver resolver = mockResolver(false);
        ModuleDownloadListGenerator listGenerator = new ModuleDownloadListGenerator(localRegistry, null, resolver);
        buildList(listGenerator);
    }

    @Test(expected = DependencyResolutionFailedException.class)
    public void testEngineVersionNotSupported() throws DependencyResolutionFailedException {
        ModuleRegistry localRegistry = buildRegistry("1.0.0");
        DependencyResolver resolver = mockResolver(true, buildEngineModule("2.0.0"));
        ModuleDownloadListGenerator listGenerator = new ModuleDownloadListGenerator(localRegistry, null, resolver);
        buildList(listGenerator);
    }

    @Test
    public void testSingleModuleNoUpdate() throws DependencyResolutionFailedException {
        ModuleRegistry localRegistry = buildRegistry("1.0.0", buildSimpleModule("myModule", "1.0.0"));
        DependencyResolver resolver = mockResolver(true, buildSimpleModule("myModule", "1.0.0"), buildEngineModule("1.0.0"));
        ModuleDownloadListGenerator listGenerator = new ModuleDownloadListGenerator(localRegistry, null, resolver);
        assertEquals(Collections.emptySet(), buildList(listGenerator));
    }

    @Test
    public void testSingleModuleNeedsUpdate() throws DependencyResolutionFailedException {
        Module moduleV1 = buildSimpleModule("myModule", "1.0.0");
        Module moduleV2 = buildSimpleModule("myModule", "2.0.0");
        ModuleRegistry localRegistry = buildRegistry("1.0.0", moduleV1);
        DependencyResolver resolver = mockResolver(true, moduleV2, buildEngineModule("1.0.0"));
        ModuleDownloadListGenerator listGenerator = new ModuleDownloadListGenerator(localRegistry, null, resolver);
        assertEquals(makeSet(moduleV2), buildList(listGenerator));
    }

    private <T> Set<T> makeSet(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    private DependencyResolver mockResolver(boolean success, Module... resolutionResult) {
        DependencyResolver result = mock(DependencyResolver.class);
        when(result.resolve(any(Name.class))).thenReturn(new ResolutionResult(success, makeSet(resolutionResult)));
        return result;
    }

    private Set<Module> buildList(ModuleDownloadListGenerator generatorWithMockedResolver) throws DependencyResolutionFailedException {
        return generatorWithMockedResolver.getAllModulesToDownloadFor(buildSimpleModule(""));
    }

    private Module buildSimpleModule(String name) {
        return buildSimpleModule(name, null);
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
