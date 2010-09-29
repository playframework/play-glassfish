package org.glassfish.play.admingui.plugin;

import org.glassfish.api.admingui.ConsoleProvider;
import org.jvnet.hk2.annotations.Service;

import java.net.URL;

@Service
public class PlayConsolePlugin implements ConsoleProvider {

    @Override
    public URL getConfiguration() {
        return null;
    }
}
