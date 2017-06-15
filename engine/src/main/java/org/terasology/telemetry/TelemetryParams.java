/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.telemetry;

import com.snowplowanalytics.snowplow.tracker.DevicePlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Terasology desktop game parameters for telemetry. They are needed by snowplow stacks.
 */
public class TelemetryParams {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryParams.class);

    public final static String APP_ID_TERASOLOGY = "terasology";

    public final static DevicePlatform PLATFORM_DESKTOP = DevicePlatform.Desktop;

    public final static URL TELEMETRY_SERVER_URL = urlInit("http","localhost",80);

    private static URL urlInit(String protocal, String host, int port) {
        URL url = null;
        try {
            url = new URL(protocal, host, port, "");
        } catch (MalformedURLException e) {
            logger.error("telemetry url mal formed");
            e.printStackTrace();
        }
        return url;
    }
}
