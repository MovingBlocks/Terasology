// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.slf4j.Logger;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helpers for reporting when a {@link ModuleEnvironment} cannot say which module a class came from.
 */
public final class ModuleAttribution {

    /** How many distinct classes {@link #reportUnattributedOnce} names before it falls quiet. */
    private static final int DISTINCT_CLASS_REPORT_LIMIT = 20;

    /**
     * How long the throttle stays quiet before reporting afresh.
     * <p>
     * Long enough that one startup's worth of failures counts as a single burst, short enough that
     * a later unrelated one is still heard.
     */
    private static final long REPORT_RESET_MS = 60_000;

    private static final Set<String> reportedClasses = ConcurrentHashMap.newKeySet();
    private static final AtomicBoolean limitAnnounced = new AtomicBoolean();
    private static volatile long lastReportMs;

    private ModuleAttribution() {
        // static utility class, no instance needed
    }

    /**
     * Report a class the environment cannot attribute, at most once per class and not many times.
     * <p>
     * For callers on paths hot enough that reporting every occurrence would drown the log - type
     * handling runs this per type, per serialization - or where a null is usually legitimate and
     * only occasionally a real fault. Names at most {@value #DISTINCT_CLASS_REPORT_LIMIT} distinct
     * classes, says so when it stops, and starts over after {@value #REPORT_RESET_MS}ms of quiet.
     * <p>
     * The throttle is deliberately crude, and deliberately static: it exists to make a
     * currently-invisible problem visible without making it unbearable, and should go away once
     * the underlying attribution failures are fixed rather than becoming permanent furniture.
     *
     * @param logger the caller's logger, so the message is attributed to where it happened
     * @param what a short description of what could not be done, e.g. "build a type URI"
     * @param type the class that could not be attributed
     * @param environment the environment that was asked
     */
    public static void reportUnattributedOnce(Logger logger, String what, Class<?> type, ModuleEnvironment environment) {
        long now = System.currentTimeMillis();
        if (now - lastReportMs > REPORT_RESET_MS) {
            reportedClasses.clear();
            limitAnnounced.set(false);
        }
        lastReportMs = now;

        if (!reportedClasses.add(type.getName())) {
            return;
        }
        if (reportedClasses.size() > DISTINCT_CLASS_REPORT_LIMIT) {
            if (limitAnnounced.compareAndSet(false, true)) {
                logger.warn("More than {} classes could not be attributed to a module. Further reports are"
                        + " suppressed until this stops happening.", DISTINCT_CLASS_REPORT_LIMIT);
            }
            return;
        }
        logger.warn("Could not {} - no module provides {}", what, describeUnattributedClass(type, environment));
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
