// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.progress

import groovy.transform.CompileStatic
import org.apache.groovy.util.concurrentlinkedhashmap.ConcurrentLinkedHashMap
import org.fusesource.jansi.Ansi

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Provide possible to monitor progress tasks and draw it with ansi codes.
 * <p/>
 * Features:
 * * monitor concurency executing tasks
 * * ansi-enchanced view
 * <p/>
 * Jgit progress usage:
 * {@code
 *    try(ProgressMonitor monitor = new ProgressMonitor()) {
 *          Git.cloneRepository()
 *             // setup as you wish
 *             .setProgressMonitor(monitor.createFor(moduleName))
 *             .call();
 *        monitor.end(monitor); // monitor cannot determinate ending correctly. Jgit's bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=568425
 *    }
 * }
 */
@CompileStatic
class ProgressMonitor implements AutoCloseable {

    public static final int TIME_STEP_MILLISECONDS = 200

    ConcurrentLinkedHashMap<String, ModuleCounter> map = new ConcurrentLinkedHashMap.Builder()
            .maximumWeightedCapacity(200) // max module count
            .build()

    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor()

    ProgressMonitor() {
        println()
        service.scheduleAtFixedRate(
                { draw() } as Runnable,
                0, TIME_STEP_MILLISECONDS, TimeUnit.MICROSECONDS)
    }

    /**
     * Creates progress monitor for watching about jgit operations.
     * @param module
     * @return
     */
    ModuleProgressMonitor createFor(String module) {
        return new ModuleProgressMonitor(module, this)
    }

    void start(String moduleId, int totalTasks){
        // totalTasks is wrong. Bug at JGit https://bugs.eclipse.org/bugs/show_bug.cgi?id=568425
        map.put(moduleId, new ModuleCounter(moduleId, totalTasks))
    }

    void beginTask(String module, String taskName, int work) {
        map[module].setTask(new TaskCounter(taskName, work))
    }

    protected void onUpdate(String module, String taskName, int workCurr) {
        map[module].currentTask.workCurr = workCurr;
    }


    protected void onUpdate(String module, String taskName, int workCurr, int workTotal, int percentDone) {
        def counter = map[module]
        counter.currentTask.workCurr = workCurr
        counter.currentTask.percentDone = percentDone
    }

    protected void onEndTask(String module, String taskName, int workCurr, int workTotal, int percentDone) {
// Cannot determinate task count and last task. Bug at JGit https://bugs.eclipse.org/bugs/show_bug.cgi?id=568425
//        ModuleCounter counter = map[module]
//        if(counter.totalTasks == counter.currentTaskNumber){
//            end(module)
//        }
    }

    protected void onEndTask(String module, String taskName, int workCurr) {
// Cannot determinate task count and last task. Bug at JGit https://bugs.eclipse.org/bugs/show_bug.cgi?id=568425
//        ModuleCounter counter = map[module]
//        if(counter.totalTasks == counter.currentTaskNumber){
//            end(module)
//        }
    }

    @Override
    void close() throws Exception {
        service.shutdownNow()
    }

    class ModuleCounter {
        String moduleId
        int totalTasks
        int currentTaskNumber = 0
        TaskCounter currentTask

        ModuleCounter(String moduleId, int totalTasks) {
            this.moduleId = moduleId
            this.totalTasks = totalTasks
        }

        void setTask(TaskCounter task){
            currentTaskNumber++;
            currentTask = task
        }

    }

    class TaskCounter {
        String taskName
        int workTotal
        int workCurr
        int percentDone

        TaskCounter(String taskName, int workTotal) {
            this.taskName = taskName
            this.workTotal = workTotal
        }
    }

     synchronized void draw() {
        Ansi ansi = new Ansi()


        def precessedLines = map.values().findAll {
            it.currentTask != null
        }.each { counter ->

            ansi.eraseLine(Ansi.Erase.ALL).format(
                    "%-30s:%29s[ %4d%s]",
                    counter.moduleId,
                    counter.currentTask.taskName,
                    counter.currentTask.percentDone,
                    '%').newline()


        }.size()
        if(precessedLines > 0){
            ansi.cursorUp(map.size())
        }
        print ansi.toString()
    }

    synchronized void end(String module) {

        Ansi ansi = new Ansi()
        print ansi.eraseLine(Ansi.Erase.ALL)
                .format("%-60s[ Done ]", module)
                .cursorDownLine(map.size()).newline().cursorUpLine(map.size()).toString()
        map.remove(module)
    }
}