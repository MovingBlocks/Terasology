
package org.terasology.config.facade;

import org.terasology.config.BindsConfig;
import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.input.Input;

import java.util.Collection;
import java.util.List;

/**
 * Facade for {@link Config#getBinds()}
 */
public interface BindsConfiguration {

    boolean isBound(Input newInput);

    void setBinds(BindsConfig other);

    List<Input> getBinds(SimpleUri uri);

    BindsConfig getBindsConfig();

    boolean hasBinds(SimpleUri uri);

    void setBinds(SimpleUri bindUri, Input ... inputs);

    void setBinds(SimpleUri bindUri, Iterable<Input> inputs);

    Collection<Input> values();

}
