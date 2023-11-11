# Replay Tests

## Overview

With the addition of the Record & Replay feature, a new module that uses replays for testing purposes was created: [TestReplayModule](https://github.com/Terasology/TestReplayModule). With this module, it is now possible to record a play session and then replay them in a testing environment to test the game. This type of test is called "Replay Test" and they 
 are JUnit tests that check the game right after a replay is loaded, during a replay and also when the replay ends.

This wiki page has the purpose to explain how Replay Tests work and also how to write one of these tests. There is also an [examples package](https://github.com/Terasology/TestReplayModule/blob/master/src/test/java/org/terasology/replayTests/examples) with some examples in the module's main repository.


## Writing Replay Tests

First, it is necessary to download the TestReplayModule since it has the classes necessary to create a ReplayTest. Instructions on how to fetch a module [here](https://github.com/MovingBlocks/Terasology/wiki/Developing-Modules). It is also important to put the recording that will be tested in the "assets/recordings" module's folder. The replay tests should be created in the [replayTest package](https://github.com/Terasology/TestReplayModule/blob/master/src/test/java/org/terasology/replayTests)

There are two ways of writing replay tests: making the test class extend [AcceptanceTestEnvironment](https://github.com/Terasology/TestReplayModule/blob/master/src/main/java/org/terasology/AcceptanceTestEnvironment.java) or by having a class have a [ReplayTestingEnvironment](https://github.com/Terasology/TestReplayModule/blob/master/src/main/java/org/terasology/ReplayTestingEnvironment.java).

### Using AcceptanceTestEnvironment

After creating a class that extends `AcceptanceTestEnvironment`, it is necessary to implement three abstract methods. Each method is supposed to have checks that will happen in a distinct moment of a replay:
- Right after a replay is loaded, which means the RecordAndReplayCurrentStatus changes from PREPEARING_REPLAY to REPLAYING;
- During the replay, right after the initial checks are done and the status is still REPLAYING.
- On its ending, when the status changes from REPLAYING to REPLAY_FINISHED.

Note that it is not necessary to fill every one of the three methods since it may not be desired to test the replay on each of these phases.

**Example:**


    @Override
    protected void testOnReplayStart() throws Exception {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        TestUtils.waitUntil(() -> localPlayer.isValid());
        character = localPlayer.getCharacterEntity();
        initialPosition = new Vector3f(19.79358f, 13.511584f, 2.3982882f);
        LocationComponent location = character.getComponent(LocationComponent.class);
        assertEquals(initialPosition, location.getLocalPosition()); // check initial position.

    }

    @Override
    protected void testDuringReplay() throws Exception {
        EventSystemReplayImpl eventSystem = (EventSystemReplayImpl) CoreRegistry.get(EventSystem.class);
        TestUtils.waitUntil(() -> eventSystem.getLastRecordedEventIndex() >= 1810); // tests in the middle of a replay needs "checkpoints" like this.
        LocationComponent location = character.getComponent(LocationComponent.class);
        assertNotEquals(initialPosition, location.getLocalPosition()); // checks that the player is not on the initial position after they moved.
    }

    @Override
    protected void testOnReplayEnd() throws Exception {
        LocationComponent location = character.getComponent(LocationComponent.class);
        Vector3f finalPosition = new Vector3f(25.189344f, 13.406443f, 8.6651945f);
        assertEquals(finalPosition, location.getLocalPosition()); // checks final position
    }

Notice that to make efficient tests in `testDuringReplay` it is necessary to add "checkpoints" with `waitUntil` so that something can be tested after a certain event is reproduced.

With the three methods implemented, the only thing left is to create a test method that will call `AcceptanceTestEnvironment`'s `runTest` method. This method receives two parameters: The title of the recording and a boolean that indicates if the engine created for the test should be headless.


    @Test
    public void run() {
        runTest("Example", true);
    }

Since `AcceptanceTestEnvironment` has a `ReplayTestingEnvironment`'s subclass, it is possible to use `ReplayTestingEnvironment`'s public methods in the test.

### Using ReplayTestingEnvironment

This approach is more complex than the previous one but offers more control. First create a class and make it have an instance of `ReplayTestingEnvironment`. This environment class is responsible for starting the game in a replay state while using an instance of TerasologyEngine that can be accessed by the test class.

The next step should be to create a thread that runs `ReplayTestingEnvironment`'s `openReplay` method with the name of the recording to be played, so when the thread run, it will open a replay while the testing can be done in the main thread.


    private ReplayTestingEnvironment environment = new ReplayTestingEnvironment();
 
    /** To test the replay while it is executing, it is necessary to create a thread that will run the replay. */
    private Thread replayThread = new Thread() {

        @Override
        public void run() {
            try {
                //This is the title of the replay to be played. It is generally the name of the folder in the 'recordings' directory.
                String replayTitle = "REPLAY_TITLE";

                /*
                This opens the game and executes the replay desired for testing. The first parameter of the "openReplay" 
 method is the replay title,
                and the second one is if the replay should be headless.
                 */
                environment.openReplay(replayTitle, true);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    };


Write a public method with `@Test` annotation as you would with a normal JUnit test. In this test, make the thread call its `start` method. As it can be seen in the code below, there are two important `waitUntil` statements that divide the code and therefore should be in every replay test. The first `waitUntil` waits for the replay to be fully loaded. The asserts that check the initial values of a replay should be written right after this `waitUntil`. The second `waitUntil` runs until the replay ends. Therefore, the asserts that check values after a replay is finished should be placed right after it. It is also possible to check something in the middle of a replay. To do that, it is necessary to write tests with "check points" between the two `waitUntil`. These "checkpoints" are `waitUntil` that waits for a certain event to be sent during a replay. For an example of one of these tests, check [ExampleReplayTest](https://github.com/Terasology/TestReplayModule/blob/master/src/test/java/org/terasology/replayTests/examples/ExampleReplayTest).


    @Test
    public void testTemplate() {
        replayThread.start();

        TestUtils.waitUntil(() -> (environment.isInitialised() && environment.getRecordAndReplayStatus() == RecordAndReplayStatus.REPLAYING));

        TestUtils.waitUntil(() -> environment.getRecordAndReplayStatus() == RecordAndReplayStatus.REPLAY_FINISHED);
        
    }


When the tests finish their execution, it is important to close the replay properly. To do so, it is necessary to create a new public method with the `@After` annotation that calls the environment's `shutdown` method, resets the `GameThread` then waits for the thread to end by calling its `join` method.


    @After
    public void closeReplay() throws Exception {
        //these last three lines are important to correctly shutdown the game after the tests are done.
        environment.getHost().shutdown();
        GameThread.reset();
        replayThread.join();
    }


## Suggestions and Important notes

* It is recommended to use the `AcceptanceTestEnvironment` approach since it is easier to write replay tests using it.
* Replay tests with headed engines should have a "@Ignore" annotation since Jenkins cannot run headed tests.
* Besides the `openReplay` method in the `ReplayTestingEnvironment` class, there is also an `openMainMenu` method, which opens the game in the main menu instead of opening the replay. To test replays using this method, the user has to manually select a replay to run.
* There is an [InvertedExampleReplayTest](https://github.com/Terasology/TestReplayModule/blob/master/src/test/java/org/terasology/replayTests/examples/InvertedExampleReplayTest.java) class that makes the engine run on the main thread while the tests run on another thread. This class tests exactly the same thing as the ExampleReplayTest and was created to test some things out and to show that it is possible to structure the tests in a way different from the template.
