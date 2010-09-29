package org.glassfish.play.extension;

import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Guillaume Bort
 */
@Service(name="org.glassfish.play.extension.PlayContainer")
public class PlayContainer implements Container {

    @Override
    public Class<? extends Deployer> getDeployer() {
        return PlayDeployer.class;
    }

    @Override
    public String getName() {
        return "play";
    }
}
