# Setup a headless server

This can be achieved with different approaches.

1. For all operating systems with installed java 11
   - Download the Omega Release
   - execute: `java -jar Terasology.jar --headless`
   - Then you get a Terasology server running at port 25777
   - For configuration of the server look at the typical place for the config
2. For Linux servers with installed **docker** daemon
   - Take a look at: https://hub.docker.com/r/qwick/terasology/
   - And please report any issues, **relating to the docker image**, at https://github.com/qwc/docker-terasology
3. Start a headless server from source
   - `gradlew server`
   - or via Intellij run configuration `TerasologyPC (Headless)`
