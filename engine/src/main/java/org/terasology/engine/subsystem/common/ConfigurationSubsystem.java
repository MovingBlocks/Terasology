// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.common;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.facade.BindsConfiguration;
import org.terasology.config.facade.BindsConfigurationImpl;
import org.terasology.config.facade.InputDeviceConfiguration;
import org.terasology.config.facade.InputDeviceConfigurationImpl;
import org.terasology.config.flexible.AutoConfigManager;
import org.terasology.config.flexible.AutoConfigTypeHandlerFactory;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.identity.CertificateGenerator;
import org.terasology.identity.CertificatePair;
import org.terasology.identity.PrivateIdentityCertificate;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.persistence.serializers.Serializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.gson.GsonPersistedData;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataReader;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataSerializer;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataWriter;

/**
 * The configuration subsystem manages Terasology's configuration
 */
public class ConfigurationSubsystem implements EngineSubsystem {
    public static final String SERVER_PORT_PROPERTY = "org.terasology.serverPort";
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationSubsystem.class);

    private Config config;
    private AutoConfigManager autoConfigManager;

    @Override
    public String getName() {
        return "Configuration";
    }

    @Override
    public void preInitialise(Context rootContext) {
        config = new Config(rootContext);
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
        //add facades
        rootContext.put(InputDeviceConfiguration.class, new InputDeviceConfigurationImpl(config));
        rootContext.put(BindsConfiguration.class, new BindsConfigurationImpl(config));
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        // TODO: Put here because of TypeHandlerLibrary dependency,
        //  might need to move to preInitialise or elsewhere
        TypeHandlerLibrary typeHandlerLibrary = rootContext.get(TypeHandlerLibrary.class);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Serializer<GsonPersistedData> serializer = new Serializer<>(
                typeHandlerLibrary,
                new GsonPersistedDataSerializer(),
                new GsonPersistedDataWriter(gson),
                new GsonPersistedDataReader(gson)
        );
        autoConfigManager = new AutoConfigManager(serializer);
        typeHandlerLibrary.addTypeHandlerFactory(new AutoConfigTypeHandlerFactory(typeHandlerLibrary));
        rootContext.put(AutoConfigManager.class, autoConfigManager);

        autoConfigManager.loadConfigsIn(rootContext);
    }

    @Override
    public void postInitialise(Context rootContext) {
        StorageServiceWorker storageServiceWorker = new StorageServiceWorker(rootContext);
        storageServiceWorker.initializeFromConfig();
        rootContext.put(StorageServiceWorker.class, storageServiceWorker);
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

}
