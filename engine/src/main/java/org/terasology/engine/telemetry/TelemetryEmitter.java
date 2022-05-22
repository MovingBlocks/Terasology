// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry;

import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter;
import com.snowplowanalytics.snowplow.tracker.http.ApacheHttpClientAdapter;
import com.snowplowanalytics.snowplow.tracker.http.HttpClientAdapter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * TelemetryEmitter emit metrics to the telemetry server.
 * @see <a href="https://github.com/snowplow/snowplow/wiki/Java-Tracker#emitters">https://github.com/snowplow/snowplow/wiki/Java-Tracker#emitterss</a>
 */
public class TelemetryEmitter extends BatchEmitter {

    public static final String DEFAULT_COLLECTOR_PROTOCOL = "http";

    public static final String DEFAULT_COLLECTOR_HOST = "utility.terasology.org";

    public static final String DEFAULT_COLLECTOR_OWNER = "Terasology Community";

    public static final String DEFAULT_COLLECTOR_NAME = "TelemetryCollector";

    public static final int DEFAULT_COLLECTOR_PORT = 14654;

    private static final Logger logger = LoggerFactory.getLogger(TelemetryEmitter.class);

    private long closeTimeout = 5;

    protected TelemetryEmitter(Builder<?> builder) {
        super(builder);
    }

    public static Builder<?> builder() {
        return new Builder2();
    }

    public static URL getDefaultCollectorURL(String protocol, String host, int port) {
        URL url = null;
        try {
            url = new URL(protocol, host, port, "");
        } catch (MalformedURLException e) {
            logger.error("Telemetry server URL mal formed", e);
        }
        return url;
    }

    private static HttpClientAdapter getDefaultAdapter(URL url) {

        // Make a new client with custom concurrency rules
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setDefaultMaxPerRoute(50);

        // Make the client
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(manager)
                .build();

        // Build the adapter
        return ApacheHttpClientAdapter.builder()
                .url(url.toString())
                .httpClient(client)
                .build();
    }

    public void changeUrl(URL url) {
        HttpClientAdapter httpClientAdapter = getDefaultAdapter(url);
        this.httpClientAdapter = httpClientAdapter;
    }

    // TODO: remove it if the snowplow unclosed issue is fixed
    @Override
    public void close() {
        flushBuffer();
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(closeTimeout, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(closeTimeout, TimeUnit.SECONDS)) {
                        logger.warn("Executor did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public abstract static class Builder<T extends Builder<T>> extends BatchEmitter.Builder<T> {

        public TelemetryEmitter build() {

            URL url = getDefaultCollectorURL(DEFAULT_COLLECTOR_PROTOCOL, DEFAULT_COLLECTOR_HOST, DEFAULT_COLLECTOR_PORT);
            HttpClientAdapter httpClientAdapter = getDefaultAdapter(url);
            this.httpClientAdapter(httpClientAdapter);

            // TODO: use the proper batch size, 1 for test
            this.batchSize(1);

            return new TelemetryEmitter(this);
        }
    }

    private static class Builder2 extends Builder<Builder2> {

        @Override
        protected Builder2 self() {
            return this;
        }
    }
}
