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

import java.io.IOException;
import java.net.URL;

/**
 * TODO
 */
public final class ApiSession {

    private static final String BASE_PATH = "api/";
    private static final String ENDPOINT_USER_ACCOUNT = BASE_PATH + "user_account";
    private static final String ENDPOINT_SESSION = BASE_PATH + "session";
    private static final String ENDPOINT_CLIENT_IDENTITY = BASE_PATH + "client_identity";

    private final URL serviceURL;
    private final GenericAuthenticatedRequestData session;

    private ApiSession(URL serviceURL, String sessionToken) {
        this.serviceURL = serviceURL;
        this.session = new GenericAuthenticatedRequestData(sessionToken);
    }

    public ApiSession createFromLogin(URL hostURL, String login, String password) throws IOException {
        SessionPOSTRequestData req = new SessionPOSTRequestData(login, password);
        SessionPOSTResponseData res = Request.request(new URL(hostURL, ENDPOINT_SESSION), HttpMethod.POST, req, SessionPOSTResponseData.class);
        ApiSession result = new ApiSession(hostURL, res.token);
        return null;
    }

    private <RES, REQ> RES requestEndpoint(String endpoint, String urlArgument, HttpMethod method, REQ data, Class<RES> responseClass) throws IOException {
        URL url = new URL(serviceURL, endpoint + urlArgument == null ? "" : ("/" + urlArgument));
        return Request.request(new URL(serviceURL, endpoint), method, data, responseClass);
    }

    public void logout() throws IOException {
        requestEndpoint(ENDPOINT_SESSION, session.sessionToken, HttpMethod.DELETE, null, null);
    }

    public String getLoginName() throws IOException {
        return requestEndpoint(ENDPOINT_SESSION, session.sessionToken, HttpMethod.GET, null, SessionGETResponseData.class).login;
    }

    public void getAllIdentities() throws IOException {
        requestEndpoint(ENDPOINT_CLIENT_IDENTITY, null, HttpMethod.GET, null, AllIdentitiesGETResponseData.class);
        //TODO: process result and return data
    }

    public void getIdentity(String serverId) throws IOException {
        requestEndpoint(ENDPOINT_CLIENT_IDENTITY, serverId, HttpMethod.GET, null, SingleIdentityGETResponseData.class);
        //TODO: process result and return data
    }

    public void putIdentity() {

    }


    private static final class SessionPOSTRequestData {
        private String login;
        private String password;
        private SessionPOSTRequestData(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }

    private static final class SessionPOSTResponseData {
        private String token;
    }

    private static final class SessionGETResponseData {
        private String login;
    }

    private static final class GenericAuthenticatedRequestData {
        private String sessionToken;
        private GenericAuthenticatedRequestData(String sessionToken) {
            this.sessionToken = sessionToken;
        }
    }

    private static final class AllIdentitiesGETResponseData {

    }

    private static final class SingleIdentityGETResponseData {

    }

    private static final class PutIdentityPOSTRequestData {

    }
}
