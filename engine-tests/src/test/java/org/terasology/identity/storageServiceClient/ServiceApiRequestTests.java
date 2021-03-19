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
package org.terasology.engine.identity.storageServiceClient;


import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceApiRequestTests {

    @Test
    public void testRequest() throws IOException, StorageServiceException {
        Gson gson = new Gson();
        HttpURLConnection mockedConn = mock(HttpURLConnection.class);
        ByteArrayOutputStream receivedRequest = new ByteArrayOutputStream();
        ByteArrayInputStream response = new ByteArrayInputStream("{\"fieldA\":\"response\", \"fieldB\": 1}".getBytes());

        when(mockedConn.getOutputStream()).thenReturn(receivedRequest);
        when(mockedConn.getInputStream()).thenReturn(response);
        when(mockedConn.getResponseCode()).thenReturn(200);

        DummySerializableObject reqData = new DummySerializableObject("request", 0);
        DummySerializableObject resData = ServiceApiRequest.request(mockedConn, HttpMethod.GET, null, reqData, DummySerializableObject.class);
        assertEquals(gson.toJson(reqData), receivedRequest.toString());
        assertEquals(new DummySerializableObject("response", 1), resData);
    }

    static final class DummySerializableObject {
        private String fieldA;
        private int fieldB;

        private DummySerializableObject(String fieldA, int fieldB) {
            this.fieldA = fieldA;
            this.fieldB = fieldB;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof DummySerializableObject)) {
                return false;
            }
            DummySerializableObject o = (DummySerializableObject) other;
            return fieldA.equals(o.fieldA) && fieldB == o.fieldB;
        }
    }
}
