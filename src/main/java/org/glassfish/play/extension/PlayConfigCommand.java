package org.glassfish.play.extension;

import java.beans.PropertyVetoException;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

@Service(name="play-config")
public class PlayConfigCommand implements AdminCommand {

    @Param
    String frameworkPath;

    @Inject
    PlayContainerConfig config;

    public void execute(AdminCommandContext adminCommandContext) {
        try {
            ConfigSupport.apply(new SingleConfigCode<PlayContainerConfig>() {
                public Object run(PlayContainerConfig playContainerConfig) throws PropertyVetoException, TransactionFailure {
                    playContainerConfig.setFrameworkPath(frameworkPath);
                    return null;
                }
            }, config);
        } catch(TransactionFailure e) {
        }
    }
}
