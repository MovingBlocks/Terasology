package org.terasology.network.internal;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.identity.*;
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
 *
 */
public class TerasologyServerHandshakeHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(TerasologyServerHandshakeHandler.class);

    private Config config = CoreRegistry.get(Config.class);
    private TerasologyServerHandler serverHandler;
    private byte[] serverRandom = new byte[IdentityConstants.SERVER_CLIENT_RANDOM_LENGTH];

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception{
        super.channelOpen(ctx, e);
        serverHandler = ctx.getPipeline().get(TerasologyServerHandler.class);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        logger.info("Sending Server Hello");

        PublicIdentityCertificate serverPublicCert = config.getSecurity().getServerPublicCertificate();
        new SecureRandom().nextBytes(serverRandom);

        e.getChannel().write(NetData.NetMessage.newBuilder()
                .setHandshakeHello(NetData.HandshakeHello.newBuilder()
                        .setRandom(ByteString.copyFrom(serverRandom))
                        .setCertificate(NetData.Certificate.newBuilder()
                                .setId(serverPublicCert.getId())
                                .setModulus(ByteString.copyFrom(serverPublicCert.getModulus().toByteArray()))
                                .setExponent(ByteString.copyFrom(serverPublicCert.getExponent().toByteArray()))
                                .setSignature(ByteString.copyFrom(serverPublicCert.getSignature().toByteArray()))
                        ))
                .build());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetData.NetMessage message = (NetData.NetMessage) e.getMessage();
        if (message.hasNewIdentityRequest()) {
            processNewIdentityRequest(message.getNewIdentityRequest(), ctx);
        }
    }

    private void processNewIdentityRequest(NetData.NewIdentityRequest newIdentityRequest, ChannelHandlerContext ctx) {
        logger.info("Received new identity request");
        try {
            byte[] preMasterSecret = config.getSecurity().getServerPrivateCertificate().decrypt(newIdentityRequest.getPreMasterSecret().toByteArray());
            byte[] masterSecret = SecretGenerator.generate(preMasterSecret, SecretGenerator.MASTER_SECRET_LABEL, Bytes.concat(newIdentityRequest.getRandom().toByteArray(), serverRandom), SecretGenerator.MASTER_SECRET_LENGTH);

            // Generate a certificate pair for the client
            CertificatePair clientCertificates = new CertificateGenerator().generate(config.getSecurity().getServerPrivateCertificate());

            NetData.CertificateSet certificateData = NetData.CertificateSet.newBuilder()
                    .setPublicCertificate(NetData.Certificate.newBuilder()
                            .setId(clientCertificates.getPublicCert().getId())
                            .setModulus(ByteString.copyFrom(clientCertificates.getPublicCert().getModulus().toByteArray()))
                            .setExponent(ByteString.copyFrom(clientCertificates.getPublicCert().getExponent().toByteArray()))
                            .setSignature(ByteString.copyFrom(clientCertificates.getPublicCert().getSignature().toByteArray())))
                    .setPrivateExponent(ByteString.copyFrom(clientCertificates.getPrivateCert().getExponent().toByteArray()))
                    .build();

            byte[] encryptedCert = null;
            try {


                SecretKeySpec key = new SecretKeySpec(SecretGenerator.generate(masterSecret, SecretGenerator.KEY_EXPANSION, Bytes.concat(newIdentityRequest.getRandom().toByteArray(), serverRandom), IdentityConstants.SYMMETRIC_ENCRYPTION_KEY_LENGTH), IdentityConstants.SYMMETRIC_ENCRYPTION_ALGORITHM);
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
            serverHandler.channelAuthenticated(clientCertificates.getPublicCert(), ctx);
        } catch (BadEncryptedDataException e) {
            logger.error("Received invalid encrypted pre-master secret, ending connection attempt");
            ctx.getChannel().close();
        }


    }
}
