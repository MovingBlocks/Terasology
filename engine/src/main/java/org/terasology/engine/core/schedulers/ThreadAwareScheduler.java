// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.schedulers;

import reactor.core.scheduler.Scheduler;

/**
 * Scheduler which provides info about thread.
 */
public interface ThreadAwareScheduler extends Scheduler {

    boolean isSchedulerThread(Thread thread);

}
