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
package org.terasology.engine.subsystem.common;

import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestCallback;
import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import com.snowplowanalytics.snowplow.tracker.http.ApacheHttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.registry.In;
import org.terasology.registry.InjectionHelper;
import org.terasology.telemetry.Metrics;
import org.terasology.telemetry.TelemetryParams;
import org.terasology.telemetry.TelemetryUtils;

import java.util.List;

/**
 * This is a telemetry engine system.
 */
public class TelemetrySubSystem implements EngineSubsystem {

    @In
    private Config config;

    private Metrics metrics;

    private Emitter emitter;

    // Name space to identify the tracker
    private final String NAMESPACE_TRACKER = this.getClass().toString();

    private static final Logger logger = LoggerFactory.getLogger(TelemetrySubSystem.class);

    @Override
    public String getName() {
        return "Telemetry";
    }

    @Override
    public void preInitialise(Context rootContext) {

        // add metrics to context, this helps show metric values in ui
        metrics = new Metrics();
        rootContext.put(Metrics.class, metrics);

        // add snowplow emitter to context, contributors can use this emitter to emit other event
        emitterInit();
        rootContext.put(Emitter.class,emitter);
    }

    private void emitterInit() {

        // Make a new client with custom concurrency rules
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setDefaultMaxPerRoute(50);

        // Make the client
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(manager)
                .build();

        // Build the adapter
        HttpClientAdapter adapter = ApacheHttpClientAdapter.builder()
                .url(TelemetryParams.TELEMETRY_SERVER_URL.toString())
                .httpClient(client)
                .build();

        // Make a RequestCallback
        RequestCallback callback = new RequestCallback() {

            public void onSuccess(int successCount) {
                logger.info("Success sent, successCount: " + successCount);
            }

            public void onFailure(int successCount, List<TrackerPayload> failedEvents) {
                logger.warn("Failure, successCount: " + successCount + "\nfailedEvent:\n" + failedEvents.toString());
            }
        };

        // initialise emitter
        emitter = BatchEmitter.builder()
                .httpClientAdapter(adapter) // Required
                .threadCount(20) // Default is 50
                .requestCallback(callback)
                .bufferSize(1)
                .build();
    }

    @Override
    public void postInitialise(Context rootContext) {

        metrics.initialise();
        InjectionHelper.inject(this, rootContext);

        if (config.getTelemetryConfig().isEnableTelemetry()) {

            Unstructured systemContextMetric = metrics.getSystemContextMetric().getMetric();
            TelemetryUtils.trackMetric(emitter,NAMESPACE_TRACKER,systemContextMetric);
        }
    }
}
