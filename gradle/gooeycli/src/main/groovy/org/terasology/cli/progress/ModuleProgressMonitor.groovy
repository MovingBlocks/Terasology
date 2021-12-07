// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.progress

import org.eclipse.jgit.lib.BatchingProgressMonitor

class ModuleProgressMonitor extends BatchingProgressMonitor {

    String moduleId
    ProgressMonitor delegate

    ModuleProgressMonitor(String moduleId, ProgressMonitor delegate) {
        this.moduleId = moduleId
        this.delegate = delegate
    }

    @Override
    void beginTask(String title, int work) {
        delegate.beginTask(moduleId, title, work)
        super.beginTask(title, work)
    }

    @Override
    void start(int totalTasks) {
       delegate.start(moduleId,totalTasks)
    }

    @Override
    protected void onUpdate(String taskName, int workCurr) {
        delegate.onUpdate(moduleId,taskName,workCurr)
    }

    @Override
    protected void onEndTask(String taskName, int workCurr) {
        delegate.onEndTask(moduleId,taskName,workCurr)
    }

    @Override
    protected void onUpdate(String taskName, int workCurr, int workTotal, int percentDone) {
        delegate.onUpdate(moduleId,taskName,workCurr,workTotal,percentDone)
    }

    @Override
    protected void onEndTask(String taskName, int workCurr, int workTotal, int percentDone) {
        delegate.onEndTask(moduleId,taskName,workCurr,workTotal,percentDone)
    }
}
