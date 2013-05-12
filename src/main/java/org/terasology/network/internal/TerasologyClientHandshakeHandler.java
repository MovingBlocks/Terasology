package org.terasology.network.internal;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.ClientIdentity;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.identity.IdentityConstants;
import org.terasology.identity.PrivateIdentityCertificate;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.identity.SecretGenerator;
import org.terasology.protobuf.NetData;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 *
 */
public class TerasologyClientHandshakeHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(TerasologyClientHandshakeHandler.class);

    private Config config = CoreRegistry.get(Config.class);
    private TerasologyClientHandler clientHandler;

    private byte[] serverRandom;
    private byte[] clientRandom;
    private byte[] masterSecret;
    private NetData.HandshakeHello serverHello;
    private NetData.HandshakeHello clientHello;

    private boolean requestedCertificate = false;
    private ClientIdentity identity = null;
    private PublicIdentityCertificate serverCertificate = null;

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelOpen(ctx, e);
        clientHandler = ctx.getPipeline().get(TerasologyClientHandler.class);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetData.NetMessage message = (NetData.NetMessage) e.getMessage();
        if (message.hasHandshakeHello()) {
            processServerHello(message.getHandshakeHello(), ctx);
        } else if (message.hasProvisionIdentity()) {
            processNewIdentity(message.getProvisionIdentity(), ctx);
        } else if (message.hasHandshakeVerification()) {
            processHandshakeVerification(message.getHandshakeVerification(), ctx);
        }
    }

    private void processHandshakeVerification(NetData.HandshakeVerification handshakeVerification, ChannelHandlerContext ctx) {
        logger.info("Received server verification");
        if (serverHello == null || clientHello == null) {
            logger.error("Received server verification without requesting it: cancelling authentication");
            ctx.getChannel().close();
            return;
        }

        if (!serverCertificate.verify(Bytes.concat(serverHello.toByteArray(), clientHello.toByteArray()), handshakeVerification.getSignature().toByteArray())) {
            logger.error("Server failed verification: cancelling authentication");
            ctx.getChannel().close();
            return;
        }

        // And we're authenticated.
        ctx.getPipeline().remove(this);
        clientHandler.channelAuthenticated(ctx);
    }

    private void processNewIdentity(NetData.ProvisionIdentity provisionIdentity, ChannelHandlerContext ctx) {
        logger.info("Received identity from server");
        if (!requestedCertificate) {
            logger.error("Received identity without requesting it: cancelling authentication");
            ctx.getChannel().close();
            return;
        }

        try {
            byte[] decryptedCert = null;
            try {
                SecretKeySpec key = new SecretKeySpec(SecretGenerator.generate(masterSecret, SecretGenerator.KEY_EXPANSION, Bytes.concat(clientRandom, serverRandom), IdentityConstants.SYMMETRIC_ENCRYPTION_KEY_LENGTH), IdentityConstants.SYMMETRIC_ENCRYPTION_ALGORITHM);
                Cipher cipher = Cipher.getInstance(IdentityConstants.SYMMETRIC_ENCRYPTION_ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, key);
                decryptedCert = cipher.doFinal(provisionIdentity.getEncryptedCertificates().toByteArray());
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                logger.error("Unexpected error decrypting received certificate, ending connection attempt", e);
                ctx.getChannel().close();
                return;
            }

            NetData.CertificateSet certificateSet = NetData.CertificateSet.parseFrom(decryptedCert);
            NetData.Certificate publicCertData = certificateSet.getPublicCertificate();

            PublicIdentityCertificate publicCert = NetMessageUtil.convert(publicCertData);

            if (!publicCert.verifySignedBy(serverCertificate)) {
                logger.error("Received invalid certificate, not signed by server: cancelling authentication");
                ctx.getChannel().close();
                return;
            }

            PrivateIdentityCertificate privateCert = new PrivateIdentityCertificate(publicCert.getModulus(), new BigInteger(certificateSet.getPrivateExponent().toByteArray()));

            ClientIdentity identity = new ClientIdentity(publicCert, privateCert);
            config.getSecurity().addIdentity(serverCertificate, identity);
            config.save();

            // And we're authenticated.
            ctx.getPipeline().remove(this);
            clientHandler.channelAuthenticated(ctx);

        } catch (InvalidProtocolBufferException e) {
            logger.error("Received invalid certificate data: cancelling authentication", e);
            ctx.getChannel().close();
            return;
        }
    }

    private void processServerHello(NetData.HandshakeHello helloMessage, ChannelHandlerContext ctx) {
        if (serverHello == null) {
            logger.info("Received Server Hello");
            serverHello = helloMessage;
            serverRandom = helloMessage.getRandom().toByteArray();
            NetData.Certificate cert = helloMessage.getCertificate();
            serverCertificate = NetMessageUtil.convert(cert);

            if (!serverCertificate.verifySelfSigned()) {
                logger.error("Received invalid server certificate: cancelling authentication");
                ctx.getChannel().close();
                return;
            }

            clientRandom = new byte[IdentityConstants.SERVER_CLIENT_RANDOM_LENGTH];

            identity = config.getSecurity().getIdentity(serverCertificate);
            if (identity == null) {
                requestIdentity(ctx);
            } else {
                sendCertificate(helloMessage, ctx);
            }

        } else {
            logger.error("Received multiple hello messages from server: cancelling authentication");
            ctx.getChannel().close();
            // TODO: Ensure authentication failed error is displayed to client
        }

    }

    private void sendCertificate(NetData.HandshakeHello helloMessage, ChannelHandlerContext ctx) {
        logger.info("Sending client certificate");
        PublicIdentityCertificate pubClientCert = identity.getPlayerPublicCertificate();

        clientHello = NetData.HandshakeHello.newBuilder()
                .setRandom(ByteString.copyFrom(clientRandom))
                .setCertificate(NetMessageUtil.convert(pubClientCert))
                .setTimestamp(System.currentTimeMillis())
                .build();

        byte[] dataToSign = Bytes.concat(helloMessage.toByteArray(), clientHello.toByteArray());
        byte[] signature = identity.getPlayerPrivateCertificate().sign(dataToSign);

        ctx.getChannel().write(NetData.NetMessage.newBuilder()
                .setHandshakeHello(clientHello)
                .setHandshakeVerification(NetData.HandshakeVerification.newBuilder()
                        .setSignature(ByteString.copyFrom(signature)))
                .build());
    }

    private void requestIdentity(ChannelHandlerContext ctx) {
        logger.info("No existing identity, requesting one");

        byte[] preMasterSecret = new byte[IdentityConstants.PREMASTER_SECRET_LENGTH];
        new SecureRandom().nextBytes(preMasterSecret);
        byte[] encryptedPreMasterSecret = serverCertificate.encrypt(preMasterSecret);

        masterSecret = SecretGenerator.generate(preMasterSecret, SecretGenerator.MASTER_SECRET_LABEL, Bytes.concat(clientRandom, serverRandom), SecretGenerator.MASTER_SECRET_LENGTH);

        ctx.getChannel().write(NetData.NetMessage.newBuilder()
                .setNewIdentityRequest(NetData.NewIdentityRequest.newBuilder()
                        .setPreMasterSecret(ByteString.copyFrom(encryptedPreMasterSecret))
                        .setRandom(ByteString.copyFrom(clientRandom))
                ).build());
        requestedCertificate = true;
    }


}
