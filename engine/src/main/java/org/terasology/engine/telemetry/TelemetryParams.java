// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.telemetry;

import com.snowplowanalytics.snowplow.tracker.DevicePlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.annotation.API;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Terasology desktop game parameters for telemetry. They are needed by snowplow stacks.
 */
@API
public final class TelemetryParams {

    public static final String APP_ID_TERASOLOGY = "terasology";

    public static final DevicePlatform PLATFORM_DESKTOP = DevicePlatform.Desktop;

    /**
     * The user id is based on user's MAC address. Normally it differs from one to another.
     * We hash the MAC address to protect personnel information.
     */
    public static String userId;

    private static final Logger logger = LoggerFactory.getLogger(TelemetryParams.class);

    static {
        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
            byte[] macAddress = networkInterface.getHardwareAddress();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(macAddress);
            userId = Base64.getEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            logger.error("Exception when getting MAC address", e);
        }
    }

    /**
     * Private constructor to hide the implicit public one for the util class.
     */
    private TelemetryParams() {

    }
}
