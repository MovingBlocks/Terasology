/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.engine;

import java.nio.file.Path;

/**
 * Configures the underlying logback logging framework.
 * @author Martin Steiger
 */
public final class LoggingContext {

    private LoggingContext() {
        // no instances
    }

    public static void initialize(Path logFileFolder) {
        String pathString = logFileFolder.normalize().toString();
        System.setProperty("logFileFolder", pathString);

        // Unfortunately, setting context-based variables works only after initialization
        // has completed. Manual initialization will work but is overriden by the first
        // (static) access to slf4j's StaticLoggerBinder.
        // This default initialization will attempt to create a folder "logFileFolder_IS_UNDEFINED" though.
        // TODO: file a report at logback/slf4j

//        LoggerContext context = new LoggerContext();
//        context.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
//        context.putProperty("targetFolder", pathString);
//        JoranConfigurator configurator = new JoranConfigurator();
//        configurator.setContext(context);
//
//        try {
//            ContextInitializer ci = new ContextInitializer(context);
//            ci.autoConfig();
//        } catch (JoranException e) {
//            e.printStackTrace();
//        }
    }
}
