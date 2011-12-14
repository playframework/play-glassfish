package org.glassfish.play.extension;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.config.ConfigParser;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.annotations.Scoped;

@Service(name = "play-config")
@Scoped(PerLookup.class)
public class PlayConfigCommand implements AdminCommand {

    @Param
    String frameworkPath;
    
    @Inject(optional = true)
    PlayContainerConfig config;

    @Inject
    ConfigParser configParser;

    @Inject
    Habitat habitat;

    public void execute(AdminCommandContext adminCommandContext) {
        try {
            if (config == null) {
                URL url = this.getClass().getClassLoader().getResource("init.xml");
                if (url != null) {
                    try {
                        config = configParser.parseContainerConfig(habitat, url, PlayContainerConfig.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot init Play container configuration");
                }
            }
            ConfigSupport.apply(new SingleConfigCode<PlayContainerConfig>() {

                public Object run(PlayContainerConfig playContainerConfig) throws PropertyVetoException, TransactionFailure {
                    playContainerConfig.setFrameworkPath(frameworkPath);
                    return null;
                }
            }, config);
        } catch (TransactionFailure e) {
        }
    }
}
