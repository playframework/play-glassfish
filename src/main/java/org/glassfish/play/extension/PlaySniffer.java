package org.glassfish.play.extension;

import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.admin.config.ConfigParser;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import com.sun.enterprise.module.Module;

import java.util.logging.Logger;
import java.util.Map;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.logging.Level;

/**
 * @author Guillaume Bort
 */
@Service(name = "play")
public class PlaySniffer implements Sniffer {

    @Inject(optional = true)
    PlayContainerConfig config;

    @Inject
    ConfigParser configParser;
    
    @Inject
    Habitat habitat;

    @Override
    public boolean handles(ReadableArchive source, ClassLoader loader) {
        try {
            return source.exists("app") && source.exists("conf/application.conf");
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String[] getURLPatterns() {
        return new String[0];
    }

    @Override
    public Class<? extends Annotation>[] getAnnotationTypes() {
        Class<? extends Annotation>[] a = (Class<? extends Annotation>[]) Array.newInstance(Class.class, 0);
        return a;
    }

    @Override
    public String getModuleType() {
        return "play";
    }

    @Override
    public Module[] setup(String containerHome, Logger logger) throws IOException {
        if (config == null) {
            URL url = this.getClass().getClassLoader().getResource("init.xml");
            if (url != null) {
               configParser.parseContainerConfig(habitat, url, PlayContainerConfig.class);
            } else {
                Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot init Play container configuration");
            }
        }
        return null;
    }

    @Override
    public void tearDown() {
    }

    @Override
    public String[] getContainersNames() {
        String[] c = {PlayContainer.class.getName()};
        return c;
    }

    @Override
    public boolean isUserVisible() {
        return true;
    }

    @Override
    public Map<String, String> getDeploymentConfigurations(ReadableArchive source) throws IOException {
        return null;
    }

    @Override
    public String[] getIncompatibleSnifferTypes() {
        return new String[0];
    }
}
