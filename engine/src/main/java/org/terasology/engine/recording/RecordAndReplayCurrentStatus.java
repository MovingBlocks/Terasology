// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.recording;

/**
 * Stores the current RecordAndReplayStatus. This class is extremely important since the status is the flag that
 * activates Record and Replay.
 * <br>
 * <h2>Record Workflow</h2> When the user clicks on the "Record" button on the extras menu, the {@link #status}
 * is set to PREPARING_RECORD. When a game is selected to be loaded from the "Record Screen" and the game is completely
 * loaded, the {@link #status} will be set to RECORDING by the {@link org.terasology.engine.core.modes.loadProcesses.InitialiseRecordAndReplay}
 * load process, which will make the {@link org.terasology.engine.entitySystem.event.internal.EventSystemImpl} call the {@link EventCatcher}
 * every time an event is sent.
 *
 * The RECORDING status will also make the {@link DirectionAndOriginPosRecorder} and {@link CharacterStateEventPositionMap}
 * record the data they are supposed to.
 * Every time the game is saved during the recording, {@link org.terasology.engine.persistence.internal.SaveTransaction}'s
 * saveRecordingData() is called and the events in the {@link RecordedEventStore} are saved in an "event file", reseting
 * the store and updating {@link RecordAndReplayUtils}.
 *
 * When the game shutdowns with the RECORDING status, the {@link RecordAndReplayUtils}'s shutdownRequested is set to true
 * and then every recorded data that was not stored yet will be serialized
 * and stored once {@link org.terasology.engine.persistence.internal.SaveTransaction}'s
 * saveRecordingData() method is called right after the game is saved. Once this is done, the {@link #status} is set to NOT_ACTIVATED.
 * <br>
 * <h2>Replay Workflow</h2> When the user clicks on the "Replay" button on the extras menu, the {@link #status}
 * is set to PREPEARING_REPLAY. During the load process of a Replay, the {@link org.terasology.engine.core.modes.loadProcesses.InitialiseWorld}
 * class gets the game path from the "recordings" folder instead of the "saves" one,
 * and the {@link org.terasology.engine.core.bootstrap.EntitySystemSetupUtil} creates an {@link EventSystemReplayImpl}
 * instead of {@link org.terasology.engine.entitySystem.event.internal.EventSystemImpl}.
 *
 * When the game is loading, the InitialiseRecordAndReplay load process will set the {@link #status} to REPLAYING and deserialize
 * the recorded data, updating {@link RecordedEventStore},{@link RecordAndReplayUtils}, {@link CharacterStateEventPositionMap}
 * and {@link DirectionAndOriginPosRecorderList}.
 *
 * With the game loaded and the {@link #status} as REPLAYING, the {@link EventSystemReplayImpl} will load the events from the
 * {@link RecordedEventStore} and play them according to the timestamp, while blocking these events from being sent normally
 * by the user. When the event system replays every event in the store, it checks to see if there are more "event files".
 * If there are, the store is updated and the event system continues to replay the events. It is important to notice that
 * the REPLAYING status also makes the {@link DirectionAndOriginPosRecorderList} and {@link CharacterStateEventPositionMap}
 * to replace some variables for the recorded ones. Once there is no more events to be replayed, the {@link #status} is set to
 * REPLAY_FINISHED and the {@link EventSystemReplayImpl} will work exactly
 * like {@link org.terasology.engine.entitySystem.event.internal.EventSystemImpl}.
 *
 * When the game is shutdown during this state, {@link RecordAndReplayUtils}'s shutdownRequested variable is set to true,
 * and then when {@link org.terasology.engine.persistence.internal.SaveTransaction}'s run() method is called, the {@link #status} will be
 * set to NOT_ACTIVATED and the {@link RecordAndReplayUtils} will be reset. It is important to know that during the
 * REPLAYING and REPLAY_FINISHED state the game is not saved since this is blocked by those statuses.
 */
public class RecordAndReplayCurrentStatus {

    /** Current status of the Record and Replay */
    private RecordAndReplayStatus status;

    public RecordAndReplayCurrentStatus() {
        this.status = RecordAndReplayStatus.NOT_ACTIVATED;
    }

    public RecordAndReplayStatus getStatus() {
        return status;
    }

    public void setStatus(RecordAndReplayStatus status) {
        this.status = status;
    }
}
