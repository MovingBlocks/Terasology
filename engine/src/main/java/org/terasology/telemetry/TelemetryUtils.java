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

import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.events.Unstructured;

/**
 * Utils methods for telemetry.
 */
public class TelemetryUtils {

    /**
     * track a metric.
     * @param emitter Emitter sending telemetry to the server.
     * @param nameSpace The name the class tracking this metric.
     * @param metric The new metric.
     */
    public static void trackMetric(Emitter emitter, String nameSpace, Unstructured metric) {
        // initialise tracker
        Tracker tracker = new Tracker.TrackerBuilder(emitter, nameSpace, TelemetryParams.APP_ID_TERASOLOGY)
                .platform(TelemetryParams.PLATFORM_DESKTOP)
                .build();

        tracker.track(metric);
    }
}
