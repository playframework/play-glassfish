package org.glassfish.play.extension;

import java.io.File;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.container.RequestDispatcher;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;


/**
 * @author Guillaume Bort
 */
@Service
public class PlayDeployer implements Deployer<PlayContainer, PlayAppContainer> {
    
    @Inject
    RequestDispatcher dispatcher;

    @Inject
    PlayContainerConfig config;

    @Override
    public MetaData getMetaData() {
        return null;
    }

    @Override
    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    @Override
    public boolean prepare(DeploymentContext context) {
        return false;
    }

    @Override
    public PlayAppContainer load(PlayContainer container, DeploymentContext context) {
        String contextRoot = context.getModuleProps().getProperty("context-root", context.getSourceDir().getName());
        if(!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }
        context.getModuleProps().setProperty("context-root", contextRoot);
        String playHome = context.getModuleProps().getProperty("play.home", config.getFrameworkPath());
        String playId = context.getModuleProps().getProperty("play.id", config.getFrameworkId());
        PlayAppContainer appCtr = new PlayAppContainer(container, dispatcher, context.getSourceDir(), contextRoot, new File(playHome), playId);
        return appCtr;
    }

    @Override
    public void unload(PlayAppContainer appContainer, DeploymentContext context) {
    }

    @Override
    public void clean(DeploymentContext context) {
    }

}
