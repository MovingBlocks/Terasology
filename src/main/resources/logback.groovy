import static ch.qos.logback.classic.Level.*
import org.terasology.logic.manager.PathManager
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.status.OnConsoleStatusListener
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.ConsoleAppender

statusListener(OnConsoleStatusListener)

appender("FILE", FileAppender) {
    file = new File(PathManager.getInstance().getLogPath(), "Terasology.log").getAbsolutePath()
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


