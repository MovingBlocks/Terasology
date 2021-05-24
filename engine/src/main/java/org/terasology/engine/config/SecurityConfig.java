// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.terasology.engine.identity.ClientIdentity;
import org.terasology.engine.identity.PrivateIdentityCertificate;
import org.terasology.engine.identity.PublicIdentityCertificate;

import java.lang.reflect.Type;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.Collections;
import java.util.Map;


public class SecurityConfig {
    public static final SecurityPermission PRIVATE_CERTIFICATE_ACCESS_PERMISSION = new SecurityPermission("PRIVATE_CERTIFICATE_ACCESS_PERMISSION");
    public static final SecurityPermission CERTIFICATE_WRITE_PERMISSION = new SecurityPermission("CERTIFICATE_WRITE_PERMISSION");

    private PublicIdentityCertificate serverPublicCertificate;
    private PrivateIdentityCertificate serverPrivateCertificate;
    private Map<PublicIdentityCertificate, ClientIdentity> clientCertificates = Maps.newHashMap();

    public PublicIdentityCertificate getServerPublicCertificate() {
        return serverPublicCertificate;
    }

    private void checkPermission(Permission permission) {
        if (System.getSecurityManager() != null) {
            System.getSecurityManager().checkPermission(permission);
        }
    }

    public PrivateIdentityCertificate getServerPrivateCertificate() {
        checkPermission(PRIVATE_CERTIFICATE_ACCESS_PERMISSION);
        return serverPrivateCertificate;
    }

    public void setServerCredentials(PublicIdentityCertificate publicCert, PrivateIdentityCertificate privateCert) {
        checkPermission(CERTIFICATE_WRITE_PERMISSION);
        this.serverPublicCertificate = publicCert;
        this.serverPrivateCertificate = privateCert;
    }

    public ClientIdentity getIdentity(PublicIdentityCertificate serverCertificate) {
        return clientCertificates.get(serverCertificate);
    }

    public Map<PublicIdentityCertificate, ClientIdentity> getAllIdentities() {
        return Collections.unmodifiableMap(clientCertificates);
    }

    public void addIdentity(PublicIdentityCertificate serverCertificate, ClientIdentity identity) {
        checkPermission(CERTIFICATE_WRITE_PERMISSION);
        clientCertificates.put(serverCertificate, identity);
    }

    public void clearIdentities() {
        checkPermission(CERTIFICATE_WRITE_PERMISSION);
        clientCertificates.clear();
    }

    public static class Handler implements JsonSerializer<SecurityConfig>, JsonDeserializer<SecurityConfig> {

        public static final String SERVER_PUBLIC_CERTIFICATE = "serverPublicCertificate";
        public static final String SERVER_PRIVATE_CERTIFICATE = "serverPrivateCertificate";
        public static final String CLIENT_IDENTITIES = "clientIdentities";

        @Override
        public SecurityConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SecurityConfig result = new SecurityConfig();
            JsonObject jsonObject = json.getAsJsonObject();
            if (jsonObject.has(SERVER_PUBLIC_CERTIFICATE)) {
                result.serverPublicCertificate = context.deserialize(jsonObject.getAsJsonObject(SERVER_PUBLIC_CERTIFICATE), PublicIdentityCertificate.class);
            }
            if (jsonObject.has(SERVER_PRIVATE_CERTIFICATE)) {
                result.serverPrivateCertificate = context.deserialize(jsonObject.getAsJsonObject(SERVER_PRIVATE_CERTIFICATE), PrivateIdentityCertificate.class);
            }
            if (jsonObject.has(CLIENT_IDENTITIES)) {
                JsonArray clientArray = jsonObject.getAsJsonArray(CLIENT_IDENTITIES);
                for (JsonElement jsonEntry : clientArray) {
                    ClientEntry entry = context.deserialize(jsonEntry, ClientEntry.class);
                    result.addIdentity(entry.server, new ClientIdentity(entry.clientPublic, entry.clientPrivate));
                }
            }
            return result;
        }

        @Override
        public JsonElement serialize(SecurityConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.add(SERVER_PUBLIC_CERTIFICATE, context.serialize(src.serverPublicCertificate));
            result.add(SERVER_PRIVATE_CERTIFICATE, context.serialize(src.serverPrivateCertificate));
            JsonArray clientArray = new JsonArray();
            for (Map.Entry<PublicIdentityCertificate, ClientIdentity> entry : src.clientCertificates.entrySet()) {
                ClientEntry clientEntry = new ClientEntry();
                clientEntry.server = entry.getKey();
                clientEntry.clientPublic = entry.getValue().getPlayerPublicCertificate();
                clientEntry.clientPrivate = entry.getValue().getPlayerPrivateCertificate();
                clientArray.add(context.serialize(clientEntry));
            }
            result.add(CLIENT_IDENTITIES, clientArray);
            return result;
        }
    }

    private static class ClientEntry {
        public PublicIdentityCertificate server;
        public PublicIdentityCertificate clientPublic;
        public PrivateIdentityCertificate clientPrivate;
    }
}
