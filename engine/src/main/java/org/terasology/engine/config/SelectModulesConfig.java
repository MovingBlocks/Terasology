// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.terasology.engine.core.module.StandardModuleExtension;

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
