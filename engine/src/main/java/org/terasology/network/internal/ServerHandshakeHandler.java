/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.network.internal;

import com.google.protobuf.ByteString;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.registry.CoreRegistry;
import org.terasology.identity.BadEncryptedDataException;
import org.terasology.identity.CertificateGenerator;
import org.terasology.identity.CertificatePair;
import org.terasology.identity.IdentityConstants;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.protobuf.NetData;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Authentication handler for the server end of the handshake
 */
public class ServerHandshakeHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandshakeHandler.class);

    private Config config = CoreRegistry.get(Config.class);
    private ServerConnectionHandler serverConnectionHandler;
    private byte[] serverRandom = new byte[IdentityConstants.SERVER_CLIENT_RANDOM_LENGTH];
    private NetData.HandshakeHello serverHello;

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelOpen(ctx, e);
        serverConnectionHandler = ctx.getPipeline().get(ServerConnectionHandler.class);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        logger.info("Sending Server Hello");

        PublicIdentityCertificate serverPublicCert = config.getSecurity().getServerPublicCertificate();
        new SecureRandom().nextBytes(serverRandom);

        serverHello = NetData.HandshakeHello.newBuilder()
                .setRandom(ByteString.copyFrom(serverRandom))
                .setCertificate(NetMessageUtil.convert(serverPublicCert))
                .setTimestamp(System.currentTimeMillis())
                .build();

        e.getChannel().write(NetData.NetMessage.newBuilder()
                .setHandshakeHello(serverHello)
                .build());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetData.NetMessage message = (NetData.NetMessage) e.getMessage();
        if (message.hasNewIdentityRequest()) {
            processNewIdentityRequest(message.getNewIdentityRequest(), ctx);
        } else if (message.hasHandshakeHello() && message.hasHandshakeVerification()) {
            processClientHandshake(message.getHandshakeHello(), message.getHandshakeVerification(), ctx);
        }
    }

    private void processClientHandshake(NetData.HandshakeHello clientHello, NetData.HandshakeVerification handshakeVerification, ChannelHandlerContext ctx) {
        logger.info("Received client certificate");
        PublicIdentityCertificate clientCert = NetMessageUtil.convert(clientHello.getCertificate());

        if (!clientCert.verifySignedBy(config.getSecurity().getServerPublicCertificate())) {
            logger.error("Received invalid client certificate, ending connection attempt");
            ctx.getChannel().close();
            return;
        }

        byte[] clientSignature = handshakeVerification.getSignature().toByteArray();
        byte[] signatureData = HandshakeCommon.getSignatureData(serverHello, clientHello);
        if (!clientCert.verify(signatureData, clientSignature)) {
            logger.error("Received invalid verification signature, ending connection attempt");
            ctx.getChannel().close();
            return;
        }

        logger.info("Sending server verification");
        byte[] serverSignature = config.getSecurity().getServerPrivateCertificate().sign(signatureData);
        ctx.getChannel().write(NetData.NetMessage.newBuilder()
                .setHandshakeVerification(NetData.HandshakeVerification.newBuilder()
                        .setSignature(ByteString.copyFrom(serverSignature))).build());

        // Identity has been established, inform the server handler and withdraw from the pipeline
        ctx.getPipeline().remove(this);
        serverConnectionHandler.channelAuthenticated(clientCert);
    }

    private void processNewIdentityRequest(NetData.NewIdentityRequest newIdentityRequest, ChannelHandlerContext ctx) {
        logger.info("Received new identity request");
        try {
            byte[] preMasterSecret = config.getSecurity().getServerPrivateCertificate().decrypt(newIdentityRequest.getPreMasterSecret().toByteArray());
            byte[] masterSecret = HandshakeCommon.generateMasterSecret(preMasterSecret, newIdentityRequest.getRandom().toByteArray(), serverRandom);

            // Generate a certificate pair for the client
            CertificatePair clientCertificates = new CertificateGenerator().generate(config.getSecurity().getServerPrivateCertificate());

            NetData.CertificateSet certificateData = NetData.CertificateSet.newBuilder()
                    .setPublicCertificate(NetMessageUtil.convert(clientCertificates.getPublicCert()))
                    .setPrivateExponent(ByteString.copyFrom(clientCertificates.getPrivateCert().getExponent().toByteArray()))
                    .build();

            byte[] encryptedCert = null;
            try {
                SecretKeySpec key = HandshakeCommon.generateSymmetricKey(masterSecret, newIdentityRequest.getRandom().toByteArray(), serverRandom);
                Cipher cipher = Cipher.getInstance(IdentityConstants.SYMMETRIC_ENCRYPTION_ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, key);
                encryptedCert = cipher.doFinal(certificateData.toByteArray());
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                logger.error("Unexpected error encrypting certificate for sending, ending connection attempt", e);
                ctx.getChannel().close();
                return;
            }

            ctx.getChannel().write(NetData.NetMessage.newBuilder()
                    .setProvisionIdentity(NetData.ProvisionIdentity.newBuilder()
                            .setEncryptedCertificates(ByteString.copyFrom(encryptedCert)))
                    .build());

            // Identity has been established, inform the server handler and withdraw from the pipeline
            ctx.getPipeline().remove(this);
            serverConnectionHandler.channelAuthenticated(clientCertificates.getPublicCert());
        } catch (BadEncryptedDataException e) {
            logger.error("Received invalid encrypted pre-master secret, ending connection attempt");
            ctx.getChannel().close();
        }
    }
}
