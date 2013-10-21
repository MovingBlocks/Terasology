import java.nio.file.Path
import java.nio.file.Paths

import static ch.qos.logback.classic.Level.*
import org.terasology.engine.paths.PathManager
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.status.OnConsoleStatusListener
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.ConsoleAppender

statusListener(OnConsoleStatusListener)

appender("FILE", FileAppender) {
    Path path = PathManager.getInstance().getLogPath()
    if (path == null) {
        path = Paths.get("logs");
    }
    file = path.resolve("Terasology.log").toFile()
    append = false
    encoder(PatternLayoutEncoder) {
        Pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        Pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

root(DEBUG, ["CONSOLE", "FILE"])
logger("org.terasology", INFO)


