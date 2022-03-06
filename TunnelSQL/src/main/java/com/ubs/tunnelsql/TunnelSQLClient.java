package com.ubs.tunnelsql;

import com.ubs.tunnelsql.utils.CommonUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.exception.ExceptionUtils;


import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.PropertyResolverUtils;
import org.apache.sshd.common.forward.PortForwardingEventListener;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kalidoss, Thiyagaraja
 */
public class TunnelSQLClient {
    //-Dlogback.configurationFile=path/logback.xml
    private static final Logger logger =  LoggerFactory.getLogger(TunnelSQLClient.class);

    public static Properties configProp = new Properties();

    static {
        loadConfigProperties();
        checkConfigPropertiesValues();
    }

    public static void main(String[] args) {
        try {

            SshClient sshClient = SshClient.setUpDefaultClient();
            logger.debug("Defual HeartBeat Duration =" + sshClient.getSessionHeartbeatInterval().getSeconds());
            logger.debug("Update hearbeat Duration into 30 seconds");
            PropertyResolverUtils.updateProperty(sshClient, "heartbeat-interval", 30000l);
            sshClient.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
            sshClient.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
            sshClient.start();
            ClientSession clientSession = sshClient.connect(CommonUtil.TUNNEL_HOST_USERNAME, CommonUtil.TUNNEL_HOST_IP, CommonUtil.TUNNEL_HOST_PORT).verify(7l, TimeUnit.SECONDS).getSession();
            clientSession.addPasswordIdentity(CommonUtil.TUNNEL_HOST_PASSWORD);
            clientSession.auth().verify(5l, TimeUnit.SECONDS);

            clientSession.addPortForwardingEventListener(new PortForwardingEventListener() {
                @Override
                public void establishedDynamicTunnel(Session session, SshdSocketAddress local, SshdSocketAddress boundAddress, Throwable reason) throws IOException {
                    PortForwardingEventListener.super.establishedDynamicTunnel(session, local, boundAddress, reason); //To change body of generated methods, choose Tools | Templates.
                    logger.debug("Established Dynammic Tunnel");
                }

                @Override
                public void establishedExplicitTunnel(Session session, SshdSocketAddress local, SshdSocketAddress remote, boolean localForwarding, SshdSocketAddress boundAddress, Throwable reason) throws IOException {
                    PortForwardingEventListener.super.establishedExplicitTunnel(session, local, remote, localForwarding, boundAddress, reason); //To change body of generated methods, choose Tools | Templates.
                    logger.debug("Established Explicit Tunnel");
                }

            });

            SshdSocketAddress localAddress = new SshdSocketAddress("localhost", CommonUtil.FORWARD_PORT);
            SshdSocketAddress portForwardBinding = clientSession.startLocalPortForwarding(CommonUtil.BIND_PORT, localAddress);

        } catch (Exception ex) {
            logger.error(ExceptionUtils.getStackTrace(ex));
        }

    }

    public static void loadConfigProperties() {
        try {
            File configFile = new File("./config.prop");
            if (configFile.exists() == false) {
                configFile.createNewFile();
                
                configProp.setProperty(CommonUtil.PROPERTY_TUNNEL_HOST_IP, "");
                configProp.setProperty(CommonUtil.PROPERTY_TUNNEL_HOST_PORT,"-1");
                configProp.setProperty(CommonUtil.PROPERTY_TUNNEL_HOST_USERNAME, "test");
                configProp.setProperty(CommonUtil.PROPERTY_TUNNEL_HOST_PASSWORD, "test");
                configProp.setProperty(CommonUtil.PROPERTY_BIND_PORT, "1533");
                configProp.setProperty(CommonUtil.PROPERTY_FORWARD_PORT, "1433");
                try (FileOutputStream outputStream = new FileOutputStream(configFile)) {
                    configProp.store(outputStream, "Default Concig File");
                    outputStream.flush();
                }

            }
            FileInputStream file = new FileInputStream(configFile);
            configProp.load(file);

        } catch (Exception e) {
            logger.error("Config Property Cannot Load");
            logger.error(ExceptionUtils.getStackTrace(e));
            System.exit(0);
        }

    }

    public static void checkConfigPropertiesValues() {
        try {
            CommonUtil.TUNNEL_HOST_IP = configProp.getProperty(CommonUtil.PROPERTY_TUNNEL_HOST_IP);
            CommonUtil.TUNNEL_HOST_PORT = Integer.parseInt(configProp.getProperty(CommonUtil.PROPERTY_TUNNEL_HOST_PORT));
            CommonUtil.BIND_PORT = Integer.parseInt(configProp.getProperty(CommonUtil.PROPERTY_BIND_PORT));
            CommonUtil.FORWARD_PORT = Integer.parseInt(configProp.getProperty(CommonUtil.PROPERTY_FORWARD_PORT));

            CommonUtil.TUNNEL_HOST_USERNAME = configProp.getProperty(CommonUtil.PROPERTY_TUNNEL_HOST_USERNAME);
            CommonUtil.TUNNEL_HOST_PASSWORD = configProp.getProperty(CommonUtil.PROPERTY_TUNNEL_HOST_PASSWORD);

            if (CommonUtil.TUNNEL_HOST_IP != null && CommonUtil.TUNNEL_HOST_PORT > 0 && CommonUtil.BIND_PORT > 0 && CommonUtil.FORWARD_PORT > 0 && CommonUtil.TUNNEL_HOST_USERNAME != null && CommonUtil.TUNNEL_HOST_PASSWORD != null) {

            } else {
                logger.error("Config Properites are not good, Please input the correct values");
                System.exit(0);
            }
        } catch (Exception e) {
            logger.error("Cannot Read the Config properties");
            logger.error(ExceptionUtils.getStackTrace(e));
            System.exit(0);
        }

    }

}
