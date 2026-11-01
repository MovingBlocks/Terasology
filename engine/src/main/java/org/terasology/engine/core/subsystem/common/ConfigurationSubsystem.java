// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Lifetime;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.facade.BindsConfiguration;
import org.terasology.engine.config.facade.BindsConfigurationImpl;
import org.terasology.engine.config.facade.InputDeviceConfiguration;
import org.terasology.engine.config.facade.InputDeviceConfigurationImpl;
import org.terasology.engine.config.flexible.AutoConfigManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.identity.CertificateGenerator;
import org.terasology.engine.identity.CertificatePair;
import org.terasology.engine.identity.PrivateIdentityCertificate;
import org.terasology.engine.identity.PublicIdentityCertificate;
import org.terasology.engine.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.gestalt.di.ServiceRegistry;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

import javax.inject.Inject;

/**
 * The configuration subsystem manages Terasology's configuration
 */
public class ConfigurationSubsystem implements EngineSubsystem {
    public static final String SERVER_PORT_PROPERTY = "org.terasology.serverPort";
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationSubsystem.class);

    @Inject
    protected TypeHandlerLibrary typeHandlerLibrary;
    @Inject
    protected TranslationSystem translationSystem;
    @Inject
    protected AutoConfigManager autoConfigManager;

    private Config config;

    @Inject
    public ConfigurationSubsystem() {
    }

    @Override
    public String getName() {
        return "Configuration";
    }

    @Override
    public void preInitialise(ServiceRegistry serviceRegistry) {
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
        logger.info("Video Settings: {}", config.renderConfigAsJson(config.getRendering())); //NOPMD

        serviceRegistry.with(Config.class).lifetime(Lifetime.Singleton).use(() -> config);
        //add facades
        serviceRegistry.with(InputDeviceConfiguration.class).lifetime(Lifetime.Singleton).use(() -> new InputDeviceConfigurationImpl(config));
        serviceRegistry.with(BindsConfiguration.class).lifetime(Lifetime.Singleton).use(() -> new BindsConfigurationImpl(config));
    }

    @Override
    public void initialise(GameEngine engine, ServiceRegistry serviceRegistry) {
        StorageServiceWorker storageServiceWorker = new StorageServiceWorker(config, translationSystem);
        storageServiceWorker.initializeFromConfig();
        serviceRegistry.with(StorageServiceWorker.class).lifetime(Lifetime.Singleton).use(() -> storageServiceWorker);
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
        autoConfigManager.saveConfigsToDisk();
    }

    public Config getConfig() {
        return config;
    }
}
