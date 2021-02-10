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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Utility class to perform requests to the service API.
 */
final class ServiceApiRequest {

    private static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(BigInteger.class, BigIntegerBase64Serializer.getInstance()).create();

    private ServiceApiRequest() {
    }

    private static boolean isSuccessful(int code) {
        return code >= 200 && code < 300;
    }

    private static boolean isRedirect(int code) {
        return code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP;
    }

    private static void parseError(HttpURLConnection conn) throws IOException, StorageServiceException {
        try (InputStreamReader errResponse = new InputStreamReader(conn.getErrorStream())) {
            try {
                throw new StorageServiceException(GSON.fromJson(errResponse, ErrorResponseData.class).error);
            } catch (JsonSyntaxException e) {
                throw new StorageServiceException();
            }
        }
    }

    public static <REQUEST, RESPONSE> RESPONSE request(HttpURLConnection firstConn, HttpMethod method, String sessionToken, REQUEST data, Class<RESPONSE> responseClass)
            throws IOException, StorageServiceException {
        HttpURLConnection conn = null;
        int followedRedirects = 0;
        do {
            if (conn == null) {
                conn = firstConn;
            } else if (followedRedirects >= 8) {
                throw new IOException("Reached max limit of HTTP redirects");
            } else {
                String redirectUrl = conn.getHeaderField("Location");
                if (redirectUrl == null) {
                    throw new IOException("An HTTP redirect status code was received, but no Location header was specified");
                }
                redirectUrl = URLDecoder.decode(redirectUrl, "UTF-8");

                //needed if the Location header specifies a relative redirect, e.g. "Location: /some/path/on/the/same/server"
                URL baseUrl = conn.getURL();
                //if redirectUrl is already absolute (contains authority), baseUrl will be ignored (see javadoc for the URL(URL, String) constructor for more info)
                URL target = new URL(baseUrl, redirectUrl);

                conn = (HttpURLConnection) target.openConnection();
                followedRedirects++;
            }
            conn.setRequestMethod(method.name());
            conn.setUseCaches(false);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            if (sessionToken != null) {
                conn.setRequestProperty("Session-Token", sessionToken);
            }
            if (data != null) {
                try (OutputStream request = conn.getOutputStream()) {
                    request.write(GSON.toJson(data).getBytes());
                }
            }
            conn.connect();
        } while (isRedirect(conn.getResponseCode()));

        if (!isSuccessful(conn.getResponseCode())) {
            parseError(conn);
        }
        try (InputStream response = conn.getInputStream()) {
            if (responseClass != null) {
                return GSON.fromJson(new InputStreamReader(response), responseClass);
            } else {
                return null;
            }
        }
    }

    public static <REQUEST, RESPONSE> RESPONSE request(URL url, HttpMethod method, String sessionToken, REQUEST data, Class<RESPONSE> responseClass)
            throws IOException, StorageServiceException {
        return request((HttpURLConnection) url.openConnection(), method, sessionToken, data, responseClass);
    }

    private static class ErrorResponseData {
        private String error;
    }
}
