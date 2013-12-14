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
package org.terasology.engine.module;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Immortius
 */
public class ModuleSelectionTest {

    private StubModuleManager manager = new StubModuleManager();
    private Module engineModule = new StubModule("engine", Version.create("1.0.0"));
    private Module oldModule = new StubModule("oldModule", Version.create("1.0.0")).addDependency("engine", "0.1.0");
    private Module moduleA = new StubModule("moduleA", Version.create("1.0.0"));
    private Module moduleB = new StubModule("moduleB", Version.create("1.0.0")).addDependency("moduleA", "1.0.0");
    private Module moduleMissingDependency = new StubModule("missingDep", Version.create("1.0.0")).addDependency("missing", "1.0.0");

    @Before
    public void setup() {
        manager.addModule(engineModule);
        manager.addModule(oldModule);
        manager.addModule(moduleA);
        manager.addModule(moduleB);
        manager.addModule(moduleMissingDependency);
    }

    @Test
    public void emptySelectionIsValid() {
        ModuleSelection selection = new ModuleSelection(manager);
        assertTrue(selection.isValid());
    }

    @Test
    public void engineIsInitiallySelected() {
        ModuleSelection selection = new ModuleSelection(manager);
        List<Module> modules = selection.getSelection();
        assertEquals(Lists.newArrayList(engineModule), modules);
    }

    @Test
    public void addValidModule() {
        ModuleSelection selection = new ModuleSelection(manager);
        selection = selection.add(moduleA);
        List<Module> modules = selection.getSelection();
        assertEquals(Lists.newArrayList(engineModule, moduleA), modules);
    }

    @Test
    public void addInvalidModuleResultNotValid() {
        ModuleSelection selection = new ModuleSelection(manager);
        selection = selection.add(oldModule);
        assertFalse(selection.isValid());
        assertEquals("'oldModule:1.0.0' incompatible with 'engine:1.0.0'", selection.getValidationMessages().get(0));
    }

    @Test
    public void addModuleWithNotYetAddedDependency() {
        ModuleSelection selection = new ModuleSelection(manager);
        selection = selection.add(moduleB);
        assertTrue(selection.isValid());
        assertEquals(Lists.newArrayList(engineModule, moduleA, moduleB), selection.getSelection());
    }

    @Test
    public void addModuleMissingDependencyInvalidatesSelection() {
        ModuleSelection selection = new ModuleSelection(manager);
        selection = selection.add(moduleMissingDependency);
        assertFalse(selection.isValid());
        assertEquals("Missing dependency 'missing:[1.0.0-2.0.0)'", selection.getValidationMessages().get(0));
    }

    private class StubModule implements Module {

        private String id;
        private Version version;
        private List<DependencyInfo> dependencies = Lists.newArrayList();

        public StubModule(String id, Version version) {
            this.id = id;
            this.version = version;
        }

        public StubModule addDependency(String moduleId, String minVersion) {
            String maxVersion = Version.create(minVersion).getNextMajorVersion().toString();
            return addDependency(moduleId, minVersion, maxVersion);
        }

        public StubModule addDependency(String moduleId, String minVersion, String maxVersion) {
            DependencyInfo dependency = new DependencyInfo();
            dependency.setId(moduleId);
            dependency.setMinVersion(Version.create(minVersion));
            dependency.setMaxVersion(Version.create(maxVersion));
            dependencies.add(dependency);
            return this;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Version getVersion() {
            return version;
        }

        @Override
        public Reflections getReflections() {
            return null;
        }

        @Override
        public boolean isCodeModule() {
            return false;
        }

        @Override
        public ModuleInfo getModuleInfo() {
            ModuleInfo info = new ModuleInfo();
            info.getDependencies().addAll(dependencies);
            return info;
        }

        @Override
        public boolean isDataAvailable() {
            return false;
        }

        @Override
        public InputStream getData() throws IOException {
            return null;
        }

        @Override
        public long getSize() {
            return 0;
        }

        @Override
        public String toString() {
            return id + ":" + version;
        }
    }

    private static final class StubModuleManager implements ModuleManager {

        private Table<String, Version, Module> modules = HashBasedTable.create();

        public void addModule(Module module) {
            modules.put(module.getId(), module.getVersion(), module);
        }

        @Override
        public List<Module> getModules() {
            return Lists.newArrayList(modules.values());
        }

        @Override
        public List<String> getModuleIds() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Module getModule(String moduleId, Version version) {
            return modules.get(moduleId, version);
        }

        @Override
        public Module getLatestModuleVersion(String id) {
            Module latest = null;
            for (Module module : modules.row(id).values()) {
                if (latest == null || latest.getVersion().compareTo(module.getVersion()) < 0) {
                    latest = module;
                }
            }

            return latest;
        }

        @Override
        public Module getLatestModuleVersion(String id, Version minVersion, Version maxVersion) {
            Module latestInBounds = null;
            for (Module module : modules.row(id).values()) {
                if (module.getVersion().compareTo(minVersion) >= 0 && module.getVersion().compareTo(maxVersion) < 0
                        && (latestInBounds == null || latestInBounds.getVersion().compareTo(module.getVersion()) > 0)) {
                    latestInBounds = module;
                }
            }
            return latestInBounds;
        }

        @Override
        public Iterable<Module> getAllDependencies(Module module) {
            return null;
        }

        @Override
        public Set<String> getDependencyNamesOf(Module context) {
            return null;
        }

        @Override
        public Iterable<Module> getActiveCodeModules() {
            return null;
        }

        @Override
        public boolean isEnabled(Module module) {
            return false;
        }

        @Override
        public List<Module> getCodeModules() {
            return null;
        }

        @Override
        public Module getActiveModule(String id) {
            return null;
        }

        @Override
        public Iterable<Module> getActiveModules() {
            return null;
        }

        @Override
        public Reflections getActiveModuleReflections() {
            return null;
        }

        @Override
        public void disableAllModules() {
        }

        @Override
        public void enableModule(Module module) {
        }

        @Override
        public void enableModuleAndDependencies(Module module) {
        }

        @Override
        public void disableModule(Module module) {
        }

        @Override
        public Reflections loadInactiveReflections() {
            return null;
        }

        @Override
        public void refresh() {
        }

        @Override
        public void applyActiveModules() {
        }
    }
}
