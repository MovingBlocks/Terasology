// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity.storageServiceClient;

import org.terasology.engine.identity.ClientIdentity;
import org.terasology.engine.identity.PublicIdentityCertificate;

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
    private final String sessionToken;

    APISession(URL serviceURL, String sessionToken) {
        this.serviceURL = serviceURL;
        this.sessionToken = sessionToken;
    }

    static APISession createFromLogin(URL hostURL, String login, String password) throws IOException, StorageServiceException {
        SessionPostRequestData req = new SessionPostRequestData(login, password);
        SessionPostResponseData res = ServiceApiRequest.request(new URL(hostURL, ENDPOINT_SESSION), HttpMethod.POST,
                null, req, SessionPostResponseData.class);
        return new APISession(hostURL, res.token);
    }

    private <RES, REQ> RES requestEndpoint(String endpoint, String urlArgument, HttpMethod method, REQ data, Class<RES> responseClass)
            throws IOException, StorageServiceException {
        URL url = new URL(serviceURL, endpoint + (urlArgument == null ? "" : ("/" + urlArgument)));
        return ServiceApiRequest.request(url, method, sessionToken, data, responseClass);
    }

    void logout() throws IOException, StorageServiceException {
        requestEndpoint(ENDPOINT_SESSION, null, HttpMethod.DELETE, null, null);
    }

    String getLoginName() throws IOException, StorageServiceException {
        return requestEndpoint(ENDPOINT_SESSION, null, HttpMethod.GET, null, SessionGetResponseData.class).login;
    }

    String getSessionToken() {
        return sessionToken;
    }

    Map<PublicIdentityCertificate, ClientIdentity> getAllIdentities() throws IOException, StorageServiceException {
        AllIdentitiesGetResponseData res = requestEndpoint(ENDPOINT_CLIENT_IDENTITY, null, HttpMethod.GET,
                null, AllIdentitiesGetResponseData.class);
        return IdentityBundle.listToMap(res.clientIdentities);
    }

    void putIdentity(PublicIdentityCertificate serverCert, ClientIdentity clientIdentity) throws IOException, StorageServiceException {
        PutIdentityPostRequestData req = new PutIdentityPostRequestData(serverCert, clientIdentity);
        requestEndpoint(ENDPOINT_CLIENT_IDENTITY, null, HttpMethod.POST, req, null);
    }


    private static final class SessionPostRequestData {
        private String login;
        private String password;
        private SessionPostRequestData(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }

    private static final class SessionPostResponseData {
        private String token;
    }

    private static final class SessionGetResponseData {
        private String login;
    }

    private static final class AllIdentitiesGetResponseData {
        private List<IdentityBundle> clientIdentities;
    }

    private static final class PutIdentityPostRequestData {
        private IdentityBundle clientIdentity;
        private PutIdentityPostRequestData(PublicIdentityCertificate server, ClientIdentity client) {
            this.clientIdentity = new IdentityBundle(server, client);
        }
    }
}
