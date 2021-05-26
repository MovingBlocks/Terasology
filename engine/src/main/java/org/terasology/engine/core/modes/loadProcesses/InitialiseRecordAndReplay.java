// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplaySerializer;
import org.terasology.engine.recording.RecordAndReplayStatus;

/**
 * Initialises Record and Replay if they were selected in the main menu.
 */
public class InitialiseRecordAndReplay extends SingleStepLoadProcess {

    private Context context;
    private RecordAndReplaySerializer recordAndReplaySerializer;
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    public InitialiseRecordAndReplay(Context context) {
        this.context = context;
    }

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

    @Override
    public void begin() {
        this.recordAndReplayCurrentStatus = context.get(RecordAndReplayCurrentStatus.class);
        this.recordAndReplaySerializer = context.get(RecordAndReplaySerializer.class);
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
