# Record and Replay Code Details

## Recording and Replay workflow

For more details about the workflow, check [RecordAndReplayCurrentStatus](https://github.com/MovingBlocks/Terasology/blob/develop/engine/src/main/java/org/terasology/recording/RecordAndReplayCurrentStatus.java)'s Javadoc.

### Recording a Game

![](https://github.com/iaronaraujo/images/blob/master/Recording%20Workflow.png)

### Replaying a Recording

![](https://github.com/iaronaraujo/images/blob/master/Replaying%20Workflow.png)

## Record and Replay classes in the Context

### Added on `TerasologyEngine`

- CharacterStateEventPositionMap
- DirectionAndOriginPosRecorderList
- RecordAndReplayCurrentStatus
- RecordAndReplayUtils

### Added on `EntitySystemSetupUtil`

- EventSystem (`EventSystemReplayImpl` if it's a Replay)
- RecordAndReplaySerializer
