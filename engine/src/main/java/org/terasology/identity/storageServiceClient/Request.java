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
package org.terasology.identity.storageServiceClient;


import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utility class to perform requests to the service
 */
final class Request {

    private static Gson gson = new Gson();

    private Request() {
    }

    public static <REQUEST, RESPONSE> RESPONSE request(HttpURLConnection conn, HttpMethod method, REQUEST data, Class<RESPONSE> responseClass) throws IOException {
        try (OutputStream request = conn.getOutputStream(); InputStream response = conn.getInputStream()) {
            conn.setRequestMethod(method.name());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false);
            conn.setDoOutput(true);

            if (data != null) {
                request.write(gson.toJson(data).getBytes());
            }
            request.close();

            return gson.fromJson(new InputStreamReader(response), responseClass);
        }
    }

    public static <REQUEST, RESPONSE> RESPONSE request(URL url, HttpMethod method, REQUEST data, Class<RESPONSE> responseClass) throws IOException {
        return request((HttpURLConnection) url.openConnection(), method, data, responseClass);
    }
}
