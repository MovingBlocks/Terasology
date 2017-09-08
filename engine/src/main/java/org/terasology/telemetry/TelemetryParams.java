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
package org.terasology.telemetry;

import com.snowplowanalytics.snowplow.tracker.DevicePlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.sandbox.API;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Terasology desktop game parameters for telemetry. They are needed by snowplow stacks.
 */
@API
public class TelemetryParams {

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
}
