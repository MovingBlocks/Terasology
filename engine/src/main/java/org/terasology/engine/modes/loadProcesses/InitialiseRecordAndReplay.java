// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.recording.RecordAndReplaySerializer;
import org.terasology.recording.RecordAndReplayStatus;
import org.terasology.registry.In;

/**
 * Initialises Record and Replay if they were selected in the main menu.
 */
@ExpectedCost(1)
public class InitialiseRecordAndReplay extends SingleStepLoadProcess {

    @In
    private RecordAndReplaySerializer recordAndReplaySerializer;
    @In
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    @Override
    public String getMessage() {
        return "${engine:menu#init-record-replay}";
    }

    @Override
    public boolean step() {
        //Activate record when the preparations are ready
        if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.PREPARING_RECORD) {
            recordAndReplayCurrentStatus.setStatus(RecordAndReplayStatus.RECORDING);
        }

        //Activate the replay when the preparations are ready
        if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.PREPARING_REPLAY) {
            recordAndReplaySerializer.deserializeRecordAndReplayData();
            recordAndReplayCurrentStatus.setStatus(RecordAndReplayStatus.REPLAYING);
        }
        return true;
    }
}
