package org.glassfish.play.extension;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.container.EndpointRegistrationException;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import java.io.File;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Guillaume Bort
 */
public class PlayAppContainer implements ApplicationContainer {

    static final Logger logger = Logger.getLogger(PlayAppContainer.class.getName());

    final PlayContainer ctr;
    final RequestDispatcher dispatcher;
    final File appDir;
    final String contextPath;
    final File framework;
    ClassLoader applicationClassLoader;
    
    public PlayAppContainer(PlayContainer ctr, RequestDispatcher dispatcher, File appDir, String contextRoot, PlayContainerConfig config) {
        this.ctr = ctr;
        this.dispatcher = dispatcher;
        this.appDir = appDir;
        this.contextPath = contextRoot;
        this.framework = new File(config.getFrameworkPath());
    }

    @Override
    public Object getDescriptor() {
        return null;
    }
                       
    @Override
    public boolean start(ApplicationContext startupContext) throws Exception {
        if(!this.framework.exists() || !new File(this.framework, "play").exists()) {
            logger.log(Level.SEVERE, "Please configure the Play framework path before");
            return false;
        }
        // Build the classpath
        List<URL> jars = new ArrayList<URL>();
        for(File jar : new File(this.framework, "framework/lib").listFiles()) {
            if(jar.getName().endsWith(".jar")) {
                jars.add(jar.toURI().toURL());
            }
        }
        jars.add(new File(this.framework, "framework/play.jar").toURI().toURL());
        jars.add(new File(this.framework, "modules/grizzly/lib/play-grizzly.jar").toURI().toURL());
        for(File jar : new File(this.appDir, "lib").listFiles()) {
            if(jar.getName().endsWith(".jar")) {
                jars.add(jar.toURI().toURL());
            }
        }
        URL[] classpath = jars.toArray(new URL[jars.size()]);
        applicationClassLoader = new URLClassLoader(classpath, startupContext.getClassLoader());

        logger.info("Starting Play application from " + appDir + " to " + contextPath);

        GrizzlyAdapter adapter;
        ClassLoader originalCtxClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(applicationClassLoader);
            Class adapterClass = applicationClassLoader.loadClass("play.modules.grizzly.PlayGrizzlyAdapter");
            adapter = GrizzlyAdapter.class.cast(
                    adapterClass.getConstructor(File.class, String.class, String.class).newInstance(appDir, "glassfish", contextPath)
            );
        } finally {
            Thread.currentThread().setContextClassLoader(originalCtxClassLoader);
        }

        if(adapter != null) {
            adapter.start();
            dispatcher.registerEndpoint(contextPath, adapter, this);
            return true;
        }

        return false;
    }

    @Override
    public boolean stop(ApplicationContext stopContext) {
        try {
            dispatcher.unregisterEndpoint(contextPath);
            applicationClassLoader = null;
        } catch (EndpointRegistrationException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean suspend() {
        return false;
    }

    @Override
    public boolean resume() throws Exception {
        return false;
    }

    @Override
    public ClassLoader getClassLoader() {
        return applicationClassLoader;
    }
}
