
package com.ubs.tunnelsql;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.sshd.common.forward.DefaultForwarderFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Kalidoss,Thiyagaraja
 */
public class TunnelSQL {
    private static final org.slf4j.Logger logger =  LoggerFactory.getLogger(TunnelSQL.class);

    public static void main(String[] args) {
        try {
            SshServer sshServer=SshServer.setUpDefaultServer();
            sshServer.setHost("0.0.0.0");
            sshServer.setPort(1022);
            sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
            sshServer.setPasswordAuthenticator((username,password,session)->{
                return true;
            });
            sshServer.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
            sshServer.setForwarderFactory(new DefaultForwarderFactory());                                 
            
            sshServer.start();
        } catch (IOException ex) {
           logger.error(ExceptionUtils.getStackTrace(ex));
        }

    }

}
