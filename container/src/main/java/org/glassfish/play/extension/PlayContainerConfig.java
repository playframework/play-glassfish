package org.glassfish.play.extension;

import java.beans.PropertyVetoException;
import org.glassfish.api.admin.config.Container;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;

@Configured
public interface PlayContainerConfig extends Container {

    @Attribute
    public String getFrameworkPath();
    public void setFrameworkPath(String path) throws PropertyVetoException;

    @Attribute
    public String getFrameworkId();
    public void setFrameworkId(String id) throws PropertyVetoException;

}
