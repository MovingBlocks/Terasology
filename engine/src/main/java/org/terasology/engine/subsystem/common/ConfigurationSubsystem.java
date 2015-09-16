/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.engine.subsystem.common;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.identity.CertificateGenerator;
import org.terasology.identity.CertificatePair;
import org.terasology.identity.PrivateIdentityCertificate;
import org.terasology.identity.PublicIdentityCertificate;

/**
 * The configuration subsystem manages Terasology's configuration
 */
public class ConfigurationSubsystem implements EngineSubsystem {
    public static final String SERVER_PORT_PROPERTY = "org.terasology.serverPort";
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationSubsystem.class);
    private Config config;

    @Override
    public String getName() {
        return "Configuration";
    }

    @Override
    public void preInitialise(Context rootContext) {
        config = new Config();
        config.load();

        String serverPortProperty = System.getProperty(SERVER_PORT_PROPERTY);
        if (serverPortProperty != null) {
            try {
                config.getNetwork().setServerPort(Integer.parseInt(serverPortProperty));
            } catch (NumberFormatException e) {
                logger.error("Failed to set server port to invalid value: {}", serverPortProperty);
            }
        }

        if (Iterables.isEmpty(config.getDefaultModSelection().listModules())) {
            config.getDefaultModSelection().addModule(TerasologyConstants.CORE_GAMEPLAY_MODULE);
        }

        checkServerIdentity();

        // TODO: Move to display subsystem
        logger.info("Video Settings: {}", config.renderConfigAsJson(config.getRendering()));
        rootContext.put(Config.class, config);
    }

    private void checkServerIdentity() {
        if (!validateServerIdentity()) {
            CertificateGenerator generator = new CertificateGenerator();
            CertificatePair serverIdentity = generator.generateSelfSigned();
            config.getSecurity().setServerCredentials(serverIdentity.getPublicCert(), serverIdentity.getPrivateCert());
            config.save();
        }
    }

    private boolean validateServerIdentity() {
        PrivateIdentityCertificate privateCert = config.getSecurity().getServerPrivateCertificate();
        PublicIdentityCertificate publicCert = config.getSecurity().getServerPublicCertificate();

        if (privateCert == null || publicCert == null) {
            return false;
        }

        // Validate the signature
        if (!publicCert.verifySelfSigned()) {
            logger.error("Server signature is not self signed! Generating new server identity.");
            return false;
        }

        return true;
    }

    @Override
    public void shutdown() {
        config.save();
    }

}
