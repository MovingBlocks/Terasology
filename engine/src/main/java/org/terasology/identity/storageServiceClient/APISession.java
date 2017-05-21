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

import org.terasology.identity.ClientIdentity;
import org.terasology.identity.PublicIdentityCertificate;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Represents a communication session with the service API.
 */
final class APISession {

    private static final String BASE_PATH = "api/";
    private static final String ENDPOINT_SESSION = BASE_PATH + "session";
    private static final String ENDPOINT_CLIENT_IDENTITY = BASE_PATH + "client_identity";

    private final URL serviceURL;
    private final GenericAuthenticatedRequestData session;

    public APISession(URL serviceURL, String sessionToken) {
        this.serviceURL = serviceURL;
        this.session = new GenericAuthenticatedRequestData(sessionToken);
    }

    public static APISession createFromLogin(URL hostURL, String login, String password) throws IOException, StorageServiceException {
        SessionPOSTRequestData req = new SessionPOSTRequestData(login, password);
        SessionPOSTResponseData res = ServiceAPIRequest.request(new URL(hostURL, ENDPOINT_SESSION), HttpMethod.POST, req, SessionPOSTResponseData.class);
        return new APISession(hostURL, res.token);
    }

    private <RES, REQ> RES requestEndpoint(String endpoint, String urlArgument, HttpMethod method, REQ data, Class<RES> responseClass)
            throws IOException, StorageServiceException {
        URL url = new URL(serviceURL, endpoint + (urlArgument == null ? "" : ("/" + urlArgument)));
        return ServiceAPIRequest.request(url, method, data, responseClass);
    }

    public void logout() throws IOException, StorageServiceException {
        requestEndpoint(ENDPOINT_SESSION, session.sessionToken, HttpMethod.DELETE, null, null);
    }

    public String getLoginName() throws IOException, StorageServiceException {
        return requestEndpoint(ENDPOINT_SESSION, session.sessionToken, HttpMethod.GET, null, SessionGETResponseData.class).login;
    }

    public String getSessionToken() {
        return session.sessionToken;
    }

    public Map<PublicIdentityCertificate, ClientIdentity> getAllIdentities() throws IOException, StorageServiceException {
        AllIdentitiesGETResponseData res = requestEndpoint(ENDPOINT_CLIENT_IDENTITY, null, HttpMethod.GET, session, AllIdentitiesGETResponseData.class);
        return IdentityBundle.listToMap(res.clientIdentities);
    }

    public void putIdentity(PublicIdentityCertificate serverCert, ClientIdentity clientIdentity) throws IOException, StorageServiceException {
        PutIdentityPOSTRequestData req = new PutIdentityPOSTRequestData(session, serverCert, clientIdentity);
        requestEndpoint(ENDPOINT_CLIENT_IDENTITY, null, HttpMethod.POST, req, null);
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

    private static class GenericAuthenticatedRequestData {
        private String sessionToken;
        private GenericAuthenticatedRequestData(String sessionToken) {
            this.sessionToken = sessionToken;
        }
    }

    private static final class AllIdentitiesGETResponseData {
        private List<IdentityBundle> clientIdentities;
    }

    private static final class PutIdentityPOSTRequestData extends GenericAuthenticatedRequestData {
        private IdentityBundle clientIdentity;
        private PutIdentityPOSTRequestData(GenericAuthenticatedRequestData authData, PublicIdentityCertificate server, ClientIdentity client) {
            super(authData.sessionToken);
            clientIdentity = new IdentityBundle(server, client);
        }
    }
}
