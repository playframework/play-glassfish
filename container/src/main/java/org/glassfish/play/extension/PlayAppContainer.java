package org.glassfish.play.extension;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.container.EndpointRegistrationException;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
    
    public PlayAppContainer(PlayContainer ctr, RequestDispatcher dispatcher, File appDir, String contextRoot, File frameworkPath) {
        this.ctr = ctr;
        this.dispatcher = dispatcher;
        this.appDir = appDir;
        this.contextPath = contextRoot;
        this.framework = frameworkPath;
    }

    @Override
    public Object getDescriptor() {
        return null;
    }
    
    // Finds eighter play.jar or play-x.y.z.jar
    private File resolvePlayJarFile(File playFrameworkFolder) {
        // just find the first play*.jar file
        for ( File file : playFrameworkFolder.listFiles()) {
            String name = file.getName();
            if ( name.startsWith("play") && name.endsWith(".jar")) {
                return file;
            }
        }
        return null;
    }
                       
    @Override
    public boolean start(ApplicationContext startupContext) throws Exception {
        if(!this.framework.exists() || !new File(this.framework, "play").exists()) {
            logger.log(Level.SEVERE, "Invalid path for Play framework: " + framework.getAbsolutePath());
            return false;
        }
        // Build the classpath
        List<URL> classpath = new ArrayList<URL>();
        
        // The default
        classpath.add(new File(this.appDir, "conf").toURI().toURL());
        
        // find the correct play.jar file
        File frameworkFolder = new File(this.framework, "framework/");
        File playJarFile = resolvePlayJarFile( frameworkFolder);
        if ( playJarFile == null ) {
            logger.log(Level.SEVERE, "Cannot find any play*.jar file inside " + frameworkFolder.getAbsolutePath());
            return false;
        } 
        classpath.add(playJarFile.toURI().toURL());
        
        // The application
        if(new File(this.appDir, "lib").exists()) {
            for(File jar : new File(this.appDir, "lib").listFiles()) {
                if(jar.getName().endsWith(".jar")) {
                    classpath.add(jar.toURI().toURL());
                }
            }
        }

        // The modules
        classpath.add(new File(this.framework, "modules/grizzly/lib/play-grizzly.jar").toURI().toURL());
        Properties applicationConf = new Properties();
        {
            FileInputStream fis = new FileInputStream(new File(this.appDir, "conf/application.conf"));
            applicationConf.load(fis);
            fis.close();
        }
        for(Object key : applicationConf.keySet()) {
            if(key.toString().startsWith("module.") || key.toString().startsWith("%glassfish.module.")) {
                String path = (String)applicationConf.get(key);
                File moduleRoot = new File(path.replace("${play.path}", this.framework.getAbsolutePath()));
                if(new File(moduleRoot, "lib").exists()) {
                    for(File jar : new File(moduleRoot, "lib").listFiles()) {
                        if(jar.getName().endsWith(".jar")) {
                            classpath.add(jar.toURI().toURL());
                        }
                    }
                }
            }
        }
        
        // The framework
        for(File jar : new File(this.framework, "framework/lib").listFiles()) {
            if(jar.getName().endsWith(".jar")) {
                classpath.add(jar.toURI().toURL());
            }
        }
        
        URL[] computedClasspath = classpath.toArray(new URL[classpath.size()]);
        applicationClassLoader = new URLClassLoader(computedClasspath, startupContext.getClassLoader());

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
