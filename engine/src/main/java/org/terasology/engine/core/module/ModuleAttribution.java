// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for reporting when a {@link ModuleEnvironment} cannot say which module a class came from.
 */
public final class ModuleAttribution {

    private ModuleAttribution() {
        // static utility class, no instance needed
    }

    /**
     * Describe a class the environment turned up but cannot attribute to any of its modules.
     * <p>
     * That means the environment's class index and its modules disagree: something indexed the
     * class, but no module's class predicate claims it. The two facts needed to tell those apart -
     * where the class was loaded from, and which modules were actually asked - are otherwise
     * invisible at the point of failure, and reconstructing them costs hours.
     * <p>
     * Callers should build this only on the failure path; it is not free.
     *
     * @param type the class that could not be attributed
     * @param environment the environment that was asked
     * @return a description naming the class, its code source, and the environment's modules
     */
    public static String describeUnattributedClass(Class<?> type, ModuleEnvironment environment) {
        String codeSource;
        try {
            codeSource = String.valueOf(type.getProtectionDomain().getCodeSource().getLocation());
        } catch (NullPointerException | SecurityException e) {
            codeSource = "unknown";
        }
        List<String> moduleIds = new ArrayList<>();
        for (Name id : environment.getModuleIdsOrderedByDependencies()) {
            moduleIds.add(id.toString());
        }
        return String.format("%s (loaded from %s; environment modules: %s)",
                type.getName(), codeSource, moduleIds);
    }
}
