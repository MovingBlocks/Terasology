// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.module;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.gestalt.di.index.CompoundClassIndex;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleMetadata;
import org.terasology.gestalt.module.ModuleRegistry;
import org.terasology.gestalt.module.TableModuleRegistry;
import org.terasology.gestalt.module.dependencyresolution.DependencyResolver;
import org.terasology.gestalt.module.dependencyresolution.ResolutionResult;
import org.terasology.gestalt.module.resources.EmptyFileSource;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.Version;

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
        return new Module(metadata, new EmptyFileSource(), Collections.emptyList(), new CompoundClassIndex(), (c) -> false);
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
