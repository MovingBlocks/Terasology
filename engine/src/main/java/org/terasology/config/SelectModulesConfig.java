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
package org.terasology.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.terasology.engine.module.StandardModuleExtension;

public class SelectModulesConfig {
    private Set<StandardModuleExtension> selectedStandardModuleExtensionEnumSet = StandardModuleExtension.booleanPropertySet();
    private boolean localOnly;
    private boolean uncategorized;

    public boolean isLocalOnlySelected() {
        return localOnly;
    }

    public void setLocalOnlySelected(Boolean flag) {
        this.localOnly = flag;
    }

    public void toggleIsLocalOnlySelected() {
        this.localOnly = !localOnly;
    }

    public boolean isUncategorizedSelected() {
        return uncategorized;
    }

    public void setUncategorizedSelected(boolean flag) {
        this.uncategorized = flag;
    }

    public void toggleUncategorizedSelected() {
        this.uncategorized = !uncategorized;
    }

    public boolean isAnyStandardModuleExtensionSelected() {
        return !selectedStandardModuleExtensionEnumSet.isEmpty();
    }

    public Collection<StandardModuleExtension> getSelectedStandardModuleExtensions() {
        return Collections.unmodifiableSet(selectedStandardModuleExtensionEnumSet);
    }

    public boolean isStandardModuleExtensionSelected(StandardModuleExtension standardModuleExtension) {
        return selectedStandardModuleExtensionEnumSet.contains(standardModuleExtension);
    }

    public void toggleStandardModuleExtensionSelected(StandardModuleExtension standardModuleExtension) {
        if (selectedStandardModuleExtensionEnumSet.contains(standardModuleExtension)) {
            selectedStandardModuleExtensionEnumSet.remove(standardModuleExtension);
        } else {
            selectedStandardModuleExtensionEnumSet.add(standardModuleExtension);
        }
    }

    public void unselectStandardModuleExtension(StandardModuleExtension standardModuleExtension) {
        selectedStandardModuleExtensionEnumSet.remove(standardModuleExtension);
    }
}
