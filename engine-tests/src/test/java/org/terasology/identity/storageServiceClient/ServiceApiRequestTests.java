// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
