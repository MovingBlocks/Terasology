## Unit Tests

The build server runs `gradlew unitTest` to run [JUnit](https://junit.org/junit5/) tests included in your module. 

## Integration Tests

Unit tests that are each focused on the behavior of a small amount of code
are the best at giving you fast feedback and a precise description of what's wrong.

Sometimes that's not enough, and you also need ***integration tests***:
scenarios that test the integration of your system with the engine
and the other modules it depends on.

Terasology provides extensions to JUnit that will help set up a fully configured engine and module environment for your integration tests: [`engine-tests:org.terasology.engine.integrationenvironment`](https://github.com/MovingBlocks/Terasology/tree/develop/engine-tests/src/main/java/org/terasology/engine/integrationenvironment)

🚧 **TODO**: set up a javadoc server that includes classes from `/engine-tests/src/main/`.

## Logging

Terasology can produce a _lot_ of log output, especially during integration tests.

You can edit the file `src/test/resources/logback-test.xml` to adjust which log messages will get recorded.
This is a configuration file for Logback-Classic version 1.2. Refer to the [Logback manual](https://logback.qos.ch/manual/) for details.

If your module does not have `logback-test.xml` at all, 
run `groovyw module refresh` to get a default configuration you can use as a starting point.

Logback configuration files in your module will only apply when running tests,
not while running the game itself.
The configuration for the game is in [`/facades/PC/src/main/resources/logback.xml`](https://github.com/MovingBlocks/Terasology/blob/develop/facades/PC/src/main/resources/logback.xml).

## See also

* [Developing Modules](Developing-Modules.md) - how to edit an existing module, or create a new one
* [Modding API](Modding-API.md) - the methods a module is allowed to call
* [Module Dependencies](Module-Dependencies.md) - how to deal with modules that depend on another
* [Dealing with Forks](Dealing-with-Forks.md) - work with your own copies of GitHub repos you can write to
